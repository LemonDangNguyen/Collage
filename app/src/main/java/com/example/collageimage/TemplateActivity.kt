package com.example.collageimage

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collageimage.StickerApp.Adapter.IconAdapter
import com.example.collageimage.StickerApp.Adapter.IconCategoryAdapter
import com.example.collageimage.StickerApp.model.StickerIcon
import com.example.collageimage.StickerApp.view.StickerIconView
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.color.ColorAdapter
import com.example.collageimage.color.ColorItem
import com.example.collageimage.color.OnColorClickListener
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.ratio.adapter.FontAdapter
import com.example.collageimage.saveImage.SaveFromEditImage
import com.example.collageimage.view_template.TemplateModel
import com.example.collageimage.view_template.TemplateViewModel
import com.example.collageimage.view_template.ViewTemplateAdapter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class TemplateActivity : BaseActivity<ActivityTemplateBinding>(ActivityTemplateBinding::inflate), OnColorClickListener {

    private lateinit var viewTemplateAdapter: ViewTemplateAdapter
    private val templateViewModel: TemplateViewModel by viewModels()
    private var selectedPathIndex: Int = -1
    private lateinit var categoryAdapter: IconCategoryAdapter
    private lateinit var iconAdapter: IconAdapter
    private lateinit var fontAdapter: FontAdapter
    private lateinit var colorAdapter: ColorAdapter
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            saveFlParentAsImage()
        } else {
            Toast.makeText(this, "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
        }
    }
    private val stickerData = mutableMapOf<String, List<String>>()
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImagePath = result.data?.getStringExtra("selected_image_path")
                selectedImagePath?.let {
                    val selectedBitmap = BitmapFactory.decodeFile(it)
                    if (selectedPathIndex != -1) {
                        templateViewModel.setSelectedImage(selectedBitmap)
                        viewTemplateAdapter.setSelectedImage(selectedBitmap, selectedPathIndex)
                    }
                }
            }
        }

    val colors = listOf(
        ColorItem("#F6F6F6"), ColorItem("#00BD4C"), ColorItem("#A4A4A4"),
        ColorItem("#805638"), ColorItem("#D0D0D0"), ColorItem("#0A0A0A"),
        ColorItem("#00C7AF"), ColorItem("#FF2768"), ColorItem("#AD28FF"),
        ColorItem("#FF8615"), ColorItem("#2EA7FF"), ColorItem("#007A5D"),
        ColorItem("#BA85FE"), ColorItem("#933EFF"), ColorItem("#350077"),
        ColorItem("#E8F403"), ColorItem("#F403D4")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewTemplateAdapter = binding.viewTemplate
        val imageId = intent.getIntExtra("imageId", -1)
        if (imageId != -1) {
            templateViewModel.loadTemplates()
            lifecycleScope.launch {
                val template = templateViewModel.getTemplateById(imageId)
                template?.let {
                    setupTemplate(it)
                }
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnChangeImage.setOnClickListener {
            if (selectedPathIndex != -1) {
                openSelectImage(selectedPathIndex)
            } else {
                showToast("Vui lòng chọn một hình ảnh trước khi thay đổi!")
            }
        }

        initview()

        binding.btnSave.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        saveFlParentAsImage()
                    }

                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                        Toast.makeText(
                            this,
                            "Permission needed to save images.",
                            Toast.LENGTH_SHORT
                        ).show()
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }

                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            } else {
                saveFlParentAsImage()
            }
        }
    }

    override fun setUp() {

    }

    private fun saveFlParentAsImage() {
        val bitmap = getBitmapFromView(binding.flParent)
        val saved = saveBitmapToGallery(bitmap)
        if (saved) {
            // Lưu ảnh thành công, mở Activity SaveFromEditImage và truyền ảnh
            val intent = Intent(this, SaveFromEditImage::class.java)
            // Lưu ảnh vào một tệp tạm thời để truyền
            val file = File(cacheDir, "saved_image.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }

            // Truyền đường dẫn của tệp ảnh tới Activity mới
            intent.putExtra("image_path", file.absolutePath)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        val filename = "IMG_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/CollageImage")
                }
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir =
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM)
                        .toString() + "/CollageImage"
                val file = java.io.File(imagesDir)
                if (!file.exists()) {
                    file.mkdir()
                }
                val image = java.io.File(file, filename)
                fos = java.io.FileOutputStream(image)
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, image.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                }
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.flush()
            }
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun setupTemplate(template: TemplateModel) {
        viewTemplateAdapter.setBackgroundDrawable(template.backgroundImageResId)
        template.stringPaths.forEachIndexed { index, path ->
            viewTemplateAdapter.setPath(index, path)
        }


        viewTemplateAdapter.setOnPathClickListener { pathIndex ->
            selectedPathIndex = pathIndex
            if (viewTemplateAdapter.isPathEmpty(pathIndex)) {
                openSelectImage(pathIndex)
            }
        }
        viewTemplateAdapter.setOnBitmapClickListener { bitmapIndex ->
            selectedPathIndex = bitmapIndex
        }
    }

    private fun openSelectImage(pathIndex: Int) {
        val intent = Intent(this, SelectImageTemplate::class.java).apply {
            putExtra("selected_path", pathIndex)
        }
        selectImageLauncher.launch(intent)
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    private fun initview() {
        addSticker()
        addText()
    }


    private fun addSticker() {
        loadStickerData()
        binding.btnSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.lnBottomBar.visibility = View.INVISIBLE
        }
        binding.barStickers.icClose.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.lnBottomBar.visibility = View.VISIBLE
        }
        binding.barStickers.btnDoneSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.lnBottomBar.visibility = View.VISIBLE
        }

        categoryAdapter = IconCategoryAdapter(stickerData) { category ->
            updateStickers(category)
        }
        binding.barStickers.rcvStickerCategory.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(this@TemplateActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        iconAdapter = IconAdapter(emptyList())
        iconAdapter.onStickerClick = { stickerPath ->
            val inputStream = assets.open(stickerPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val stickerIcon = StickerIcon(
                x = 0f,
                y = 0f,
                rotation = 0f,
                bitmap = bitmap,
                scaleX = 1f,
                scaleY = 1f
            )
            val stickerView = StickerIconView(this, null, stickerIcon).apply {
                setImageBitmap(bitmap)
            }
            binding.stickerContainerView.addView(stickerView)
        }
        binding.barStickers.rcvStickers.apply {
            adapter = iconAdapter
            layoutManager = GridLayoutManager(this@TemplateActivity, 4)
        }

        if (stickerData.isNotEmpty()) {
            val firstCategory = stickerData.keys.first()
            updateStickers(firstCategory)
            categoryAdapter.setSelectedCategory(firstCategory)
        }
    }


    private fun loadStickerData() {
        val assetManager = assets
        val stickerFolder = "sticker"
        val folders = assetManager.list(stickerFolder) ?: emptyArray()

        for (folder in folders) {
            val filePaths = assetManager.list("$stickerFolder/$folder")?.filter {
                it.endsWith(".webp")
            }?.map {
                "$stickerFolder/$folder/$it"
            } ?: emptyList()
            stickerData[folder] = filePaths
        }
    }

    private fun updateStickers(category: String) {
        val stickers = stickerData[category] ?: emptyList()
        iconAdapter.updateData(stickers)
    }

    private fun colortextsticker() {
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutAddText.rvTextColor.apply {
            layoutManager =
                LinearLayoutManager(this@TemplateActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
    }
    private fun addText() {


        binding.btnText.setOnClickListener {
            binding.layoutAddText.root.visibility = View.VISIBLE
            binding.lnBottomBar.visibility = View.GONE
        }

        binding.layoutAddText.ivClose.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.lnBottomBar.visibility = View.VISIBLE

        }
        binding.layoutAddText.ivDone.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.lnBottomBar.visibility = View.VISIBLE

        }

        binding.layoutAddText.tvFont.setOnClickListener {
            updateTextViewStyle2(binding.layoutAddText.tvFont)
            binding.layoutAddText.llColor.visibility = View.GONE
            binding.layoutAddText.rvTextColor.visibility = View.GONE
            binding.layoutAddText.rvFont.visibility = View.VISIBLE
        }

        binding.layoutAddText.tvColor.setOnClickListener {
            colortextsticker()
            updateTextViewStyle2(binding.layoutAddText.tvColor)
            binding.layoutAddText.llColor.visibility = View.VISIBLE
            binding.layoutAddText.rvTextColor.visibility = View.VISIBLE
            binding.layoutAddText.rvFont.visibility = View.GONE
        }

        val fontList = getFontsFromAssets()
        binding.layoutAddText.rvFont.layoutManager = GridLayoutManager(this, 2)
        fontAdapter = FontAdapter(fontList, this) { fontName ->
            Toast.makeText(this, "Selected font: $fontName", Toast.LENGTH_SHORT).show()
        }
        binding.layoutAddText.rvFont.adapter = fontAdapter

        binding.layoutAddText.tvText.setOnClickListener {
            binding.layoutAddText.tvText.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.layoutAddText.tvText.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorPrimary)
            binding.layoutAddText.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvLabel.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent))
            binding.layoutAddText.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvBorder.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
        }
        binding.layoutAddText.tvLabel.setOnClickListener {
            binding.layoutAddText.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.layoutAddText.tvLabel.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorPrimary)
            binding.layoutAddText.tvText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvText.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
            binding.layoutAddText.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvBorder.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
        }
        binding.layoutAddText.tvBorder.setOnClickListener {
            binding.layoutAddText.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.layoutAddText.tvBorder.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorPrimary)

            binding.layoutAddText.tvText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvText.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)

            binding.layoutAddText.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvLabel.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
        }


    }

    private fun updateTextViewStyle2(selectedTextView: TextView) {
        resetTextViewStyles2()
        selectedTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        selectedTextView.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.bg_border_tab)
    }
    private fun resetTextViewStyles2() {
        binding.layoutAddText.tvColor.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.black
            )
        )
        binding.layoutAddText.tvColor.backgroundTintList = ContextCompat.getColorStateList(
            this,
            android.R.color.transparent
        )
        binding.layoutAddText.tvFont.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutAddText.tvFont.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
        binding.layoutAddText.tvColor.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutAddText.tvColor.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
        binding.layoutAddText.tvAddText.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutAddText.tvAddText.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
    }


    private fun getFontsFromAssets(): List<String> {
        val fontList = mutableListOf<String>()
        try {
            val fonts = assets.list("font")
            if (fonts != null) {
                fontList.addAll(fonts)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fontList
    }

    override fun onColorClick(color: ColorItem) {
        val colorInt = Color.parseColor(color.colorHex)
        Toast.makeText(this, "Selected color: $colorInt", Toast.LENGTH_SHORT).show()
    }
}

