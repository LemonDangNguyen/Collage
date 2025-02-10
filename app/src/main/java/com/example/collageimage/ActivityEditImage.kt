package com.example.collageimage

import android.app.Dialog
import android.content.ContentValues
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.CustomBg.CustomImageAdapter
import com.example.collageimage.CustomBg.CustomImageViewModel
import com.example.collageimage.Gradient.GradientAdapter
import com.example.collageimage.Gradient.GradientViewModel
import com.example.collageimage.StickerApp.Adapter.IconAdapter
import com.example.collageimage.StickerApp.Adapter.IconCategoryAdapter
import com.example.collageimage.StickerApp.Adapter.PhotoAdapter
import com.example.collageimage.StickerApp.model.StickerIcon
import com.example.collageimage.StickerApp.model.StickerPhoto
import com.example.collageimage.StickerApp.view.StickerIconView
import com.example.collageimage.StickerApp.view.StickerPhotoView
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.color.ColorAdapter
import com.example.collageimage.color.ColorItem
import com.example.collageimage.color.ColorItem2
import com.example.collageimage.color.ColorMode
import com.example.collageimage.color.ColorPenAdapter
import com.example.collageimage.color.OnColorClickListener
import com.example.collageimage.color.OnColorClickListener2
import com.example.collageimage.databinding.ActivityEditImageBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.example.collageimage.frame.FrameAdapter
import com.example.collageimage.frame.FrameItem
import com.example.collageimage.ratio.AspectRatioViewModel
import com.example.collageimage.ratio.adapter.FontAdapter
import com.example.collageimage.ratio.adapter.RatioAdapter
import com.example.collageimage.saveImage.SaveFromEditImage
import com.example.collageimage.extensions.showToast

import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class ActivityEditImage : BaseActivity<ActivityEditImageBinding>(ActivityEditImageBinding::inflate), OnColorClickListener, OnColorClickListener2 {

    private lateinit var frameAdapter: FrameAdapter
    private val viewModelRatio: AspectRatioViewModel by viewModels()
    private var currentColor: Int = 0xFFFFFFFF.toInt()
    private var currentColorMode: ColorMode = ColorMode.BORDER
    private val customImageViewModel: CustomImageViewModel by viewModels()
    private val customGradientViewModel: GradientViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var colorAdapterpen: ColorPenAdapter

    private lateinit var categoryAdapter: IconCategoryAdapter
    private lateinit var iconAdapter: IconAdapter
    private val stickerData = mutableMapOf<String, List<String>>()
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var fontAdapter: FontAdapter

    val colors = listOf(
        ColorItem("#F6F6F6"), ColorItem("#00BD4C"), ColorItem("#A4A4A4"),
        ColorItem("#805638"), ColorItem("#D0D0D0"), ColorItem("#0A0A0A"),
        ColorItem("#00C7AF"), ColorItem("#FF2768"), ColorItem("#AD28FF"),
        ColorItem("#FF8615"), ColorItem("#2EA7FF"), ColorItem("#007A5D"),
        ColorItem("#BA85FE"), ColorItem("#933EFF"), ColorItem("#350077"),
        ColorItem("#E8F403"), ColorItem("#F403D4")
    )
    val pencolors = listOf(

        ColorItem2("#FF005C"), ColorItem2("#FF007A"), ColorItem2("#9B00E4"), ColorItem2("#630285"),
        ColorItem2("#022785"), ColorItem2("#007CD7"), ColorItem2("#00A0E4"), ColorItem2("#00B88C"),
        ColorItem2("#00B8B8"), ColorItem2("#00A08D"), ColorItem2("#009F40"), ColorItem2("#82CC0A"),
        ColorItem2("#FFE500"), ColorItem2("#FFB800"), ColorItem2("#FF1F00"), ColorItem2("#64332C"),
        ColorItem2("#736A69"), ColorItem2("#425C58"), ColorItem2("#010101"), ColorItem2("#F403D4")
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            saveFlParentAsImage()
        } else {
            Toast.makeText(this, "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        nitview()
        val imagePath = intent.getStringExtra("selected_image_path")
        if (imagePath != null) {
            displayImage(imagePath)
        }
        loadimgCam()
        backfun()

        binding.tvSave.setOnClickListener {
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
        saveBitmapToGallery(bitmap, onDone = {
            if (it != "") {
                val intent = Intent(this, SaveFromEditImage::class.java)
                intent.putExtra("image_path", it)
                startActivity(intent)
            } else showToast("Failed to save image.", Gravity.CENTER)
        })
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, onDone: (String) -> Unit) {
        val filename = "IMG_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/CollageImage")
                }

                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    fos = resolver.openOutputStream(uri)
                    fos?.use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        it.flush()
                    }
                    onDone.invoke(uri.toString())
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/CollageImage"
                val file = File(imagesDir)
                if (!file.exists()) {
                    file.mkdir()
                }
                val image = File(file, filename)
                fos = FileOutputStream(image)
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, image.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                }
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.flush()
                }
                onDone.invoke(image.path)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun backfun() {
        binding.tvCancel.setOnClickListener {
            onBackPressed()
        }
    }

    fun loadimgCam() {
        val imageList = intent.getParcelableArrayListExtra<ImageModel>("IMG_FROM_CAM")
        val imageUri = imageList?.get(0)?.uri

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.edtimgView)
        }
    }

    private fun displayImage(imagePath: String) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + "=?"
        val selectionArgs = arrayOf(imagePath)
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            cursor.close()

            binding.edtimgView.setImageURI(imageUri)
        } else {
            Log.e("DEBUG", "Không tìm thấy ảnh")
        }
    }


    private fun nitview() {
        btntParentBottom()
    }

    private fun btntParentBottom() {

        binding.layoutParentTool.origin.setOnClickListener {
            binding.layoutParentTool.origin.visibility = View.GONE
            binding.layoutParentTool.fit.visibility = View.VISIBLE
            val edtImgView = binding.edtimgView
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val heightFor4to3Ratio = (screenWidth * 4) / 3

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                heightFor4to3Ratio
            )
            edtImgView.layoutParams = params
            edtImgView.scaleType = ImageView.ScaleType.CENTER_CROP

        }
        binding.layoutParentTool.fit.setOnClickListener {
            binding.layoutParentTool.origin.visibility = View.VISIBLE
            binding.layoutParentTool.fit.visibility = View.GONE
            val edtImgView = binding.edtimgView
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            edtImgView.layoutParams = params
            edtImgView.scaleType = ImageView.ScaleType.FIT_CENTER


        }

        binding.layoutParentTool.ratio.setOnClickListener {
            binding.layoutParentTool.root.visibility = View.GONE
            binding.layoutRatiooo.root.visibility = View.VISIBLE
            ratioFun()
        }

        binding.layoutParentTool.llChangeBG.setOnClickListener {
            binding.layoutBg.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            binding.layoutBg.seleccolor.setOnClickListener {
                openColorPickerDialog2()
            }
            bgFun()
        }
        binding.layoutParentTool.llChangeFrame.setOnClickListener {
            setupRecyclerView()
            binding.layoutFrame.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            frameFun()
        }
        binding.layoutParentTool.llChangeText.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//            addText()
//            binding.layoutAddText.root.visibility = View.VISIBLE
//            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFilter.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
            //      binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE

            addSticker()
        }
        binding.layoutParentTool.changeDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(true)
            binding.layoutParentTool.root.visibility = View.GONE
            binding.barDrawing.root.visibility = View.VISIBLE
            drawFun()
        }
        binding.layoutParentTool.addImage.setOnClickListener {
            binding.layoutAddImage.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            setupRecyclerView2()

            binding.layoutAddImage.icClose.setOnClickListener {
                binding.layoutAddImage.root.visibility = View.GONE
                binding.layoutParentTool.root.visibility = View.VISIBLE
            }
            binding.layoutAddImage.btnDoneAddImage.setOnClickListener {
                binding.layoutAddImage.root.visibility = View.GONE
                binding.layoutParentTool.root.visibility = View.VISIBLE
            }

        }
    }

    private fun addSticker() {
        loadStickerData()

        binding.barStickers.icClose.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.barStickers.btnDoneSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }

        categoryAdapter = IconCategoryAdapter(stickerData) { category ->
            updateStickers(category)
        }
        binding.barStickers.rcvStickerCategory.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(this@ActivityEditImage, LinearLayoutManager.HORIZONTAL, false)
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
            layoutManager = GridLayoutManager(this@ActivityEditImage, 4)
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


    private fun ratioFun() {
        binding.layoutRatiooo.ivClose.setOnClickListener {
            binding.layoutRatiooo.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutRatiooo.ivDone.setOnClickListener {
            binding.layoutRatiooo.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        setRatio()
    }

    private fun bgFun() {
        colorrecylayout()
        currentColorMode = ColorMode.BACKGROUND
        binding.layoutBg.ivClose.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.ivDone.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }

        // Xử lý click cho từng TextView
        binding.layoutBg.tvColor.setOnClickListener {
            updateTextViewStyle(binding.layoutBg.tvColor)
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            binding.layoutBg.rvColorln.visibility = View.VISIBLE
            binding.layoutBg.seleccolor.setOnClickListener {
                openColorPickerDialog2()
            }
        }

        binding.layoutBg.tvCustom.setOnClickListener {
            updateTextViewStyle(binding.layoutBg.tvCustom)
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.VISIBLE
            setBgCus()
        }

        binding.layoutBg.tvGradient.setOnClickListener {
            updateTextViewStyle(binding.layoutBg.tvGradient)
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            binding.layoutBg.rvGradient.visibility = View.VISIBLE
            setBgGradi()
        }

        binding.layoutBg.tvBlur.setOnClickListener {
          //  updateTextViewStyle(binding.layoutBg.tvBlur)
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTextViewStyle(selectedTextView: TextView) {
        resetTextViewStyles()
        selectedTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        selectedTextView.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.bg_border_tab)
    }

    private fun resetTextViewStyles() {
        binding.layoutBg.tvColor.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.black
            )
        )
        binding.layoutBg.tvColor.backgroundTintList = ContextCompat.getColorStateList(
            this,
            android.R.color.transparent
        )
        binding.layoutBg.tvCustom.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutBg.tvCustom.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
        binding.layoutBg.tvGradient.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutBg.tvGradient.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
        binding.layoutBg.tvBlur.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutBg.tvBlur.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
    }


    private fun frameFun() {
        binding.layoutFrame.ivRefresh.setOnClickListener {
            binding.framebg.background = null
            binding.layoutFrame.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }

        binding.layoutFrame.ivDone.setOnClickListener {
            binding.layoutFrame.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }

    }

    private fun drawFun() {
        colorrecyPenlayout()
        binding.barDrawing.btnCancelDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(false)
            binding.barDrawing.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.barDrawing.btnApplyDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(false)
            binding.barDrawing.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.barDrawing.btnUndo.setOnClickListener {
            binding.drawview.setUndo()
        }
        binding.barDrawing.btnRedo.setOnClickListener {
            binding.drawview.setRedo()
        }
        binding.barDrawing.sbBrushSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.barDrawing.tvBrushSizePercent.text = progress.toString()
                binding.drawview.setPenWidth(progress.toFloat())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.barDrawing.btnBrushMode.setOnClickListener {
            binding.barDrawing.btnBrushMode.setImageResource(R.drawable.ic_brush_mode_selected)
            binding.barDrawing.btnEraserMode.setImageResource(R.drawable.ic_eraser_mode)
            binding.drawview.setEraserMode(false)
        }
        binding.barDrawing.btnEraserMode.setOnClickListener {
            binding.barDrawing.btnBrushMode.setImageResource(R.drawable.ic_brush_mode)
            binding.barDrawing.btnEraserMode.setImageResource(R.drawable.ic_eraser_mode_selected)
            binding.drawview.setEraserMode(true)
        }

        binding.barDrawing.seleccolor.setOnClickListener {
            openColorPickerDialog()
        }
    }

    private fun openColorPickerDialog() {
        val colorPicker =
            AmbilWarnaDialog(this, currentColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    currentColor = color
                    binding.drawview.setPenColor(color)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
        colorPicker.show()
    }
    private fun openColorPickerDialog2() {
        val colorPicker =
            AmbilWarnaDialog(this, currentColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    currentColor = color
                    binding.edtimgView.setBackgroundColor(color)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
        colorPicker.show()
    }
    private fun setupRecyclerView() {
        val frames = (1..20).map { FrameItem("frames/frame_$it.webp") }

        frameAdapter = FrameAdapter(frames) { drawable ->
            binding.framebg.background = drawable
        }

        binding.layoutFrame.rvFrame.apply {
            layoutManager = GridLayoutManager(this@ActivityEditImage, 5)
            adapter = frameAdapter
        }
    }

    private fun setRatio() {
        val ratios = listOf(
            Triple("1:1", R.drawable.ic_ratio_1_1, 1f),
            Triple("4:5", R.drawable.ic_ratio_4_5, 4f / 5f),
            Triple("4:5", R.drawable.ic_ratio_5_4, 5f / 4f),
            Triple("3:4", R.drawable.ic_ratio_3_4, 3f / 4f),
            Triple("9:16", R.drawable.ic_ratio_9_16, 9f / 16f)
        )
        val adapter = RatioAdapter(ratios) { ratio ->
            viewModelRatio.setAspectRatio(ratio)
        }
        binding.layoutRatiooo.rvRatio.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.layoutRatiooo.rvRatio.adapter = adapter
        viewModelRatio.aspectRatio.observe(this)
        { ratio ->
            binding.ivBackground.setAspectRatio(ratio)
            updateViewAspectRatio(binding.edtimgView, ratio)
            updatebgFrame(binding.framebg, ratio)
        }
        viewModelRatio.backgroundColor.observe(this) { color ->
            binding.ivBackground.setBackgroundColor(color)
        }

    }

    private fun updateViewAspectRatio(imageView: ImageView, ratio: Float) {
        val layoutParams = imageView.layoutParams
        val parentWidth = imageView.width
        val height = (parentWidth / ratio).toInt()
        layoutParams.height = height
        imageView.layoutParams = layoutParams

    }

    private fun updatebgFrame(frameLayout: ImageView, ratio: Float) {
        val layoutParams = frameLayout.layoutParams
        val parentWidth = frameLayout.width
        val height = (parentWidth / ratio).toInt()
        //   updateViewAspectRatio(frameLayout, ratio)
        layoutParams.height = height
        frameLayout.layoutParams = layoutParams
    }

    fun setBgCus() {
        val adapter = CustomImageAdapter(emptyList()) { image ->
            binding.edtimgView.setBackgroundResource(image.resourceId)
        }
        binding.layoutBg.rvCustom.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutBg.rvCustom.adapter = adapter

        customImageViewModel.customImages.observe(this, { images ->
            adapter.updateImages(images)
        })
    }

    fun setBgGradi() {
        val adapter = GradientAdapter(emptyList()) { gradient ->
            binding.edtimgView.setBackgroundResource(gradient.resourceId)
        }
        binding.layoutBg.rvGradient.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutBg.rvGradient.adapter = adapter

        customGradientViewModel.selectedGradient.observe(this, { images ->
            adapter.updateGradients(images)
        })
    }

    private fun colorrecyPenlayout() {
        colorAdapterpen = ColorPenAdapter(pencolors, this)
        binding.barDrawing.rcvColorDraw.apply {
            layoutManager =
                LinearLayoutManager(this@ActivityEditImage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapterpen
        }
    }

    private fun colorrecylayout() {
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutBg.rvColor.apply {
            layoutManager =
                LinearLayoutManager(this@ActivityEditImage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
    }

    private fun colortextsticker() {
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutAddText.rvTextColor.apply {
            layoutManager =
                LinearLayoutManager(this@ActivityEditImage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
    }


    override fun onColorClick(color: ColorItem) {
        val colorInt = Color.parseColor(color.colorHex)
        when (currentColorMode) {
            ColorMode.BORDER -> {
            }

            ColorMode.BACKGROUND -> {
                binding.edtimgView.setBackgroundColor(colorInt)
            }
        }
    }

    override fun onColorClick2(color2: ColorItem2) {
        val colorInt = Color.parseColor(color2.colorHex2)
        binding.drawview.setPenColor(colorInt)
    }
    override fun onBackPressed() {
        val binding2 = DialogSaveBeforeClosingBinding.inflate(layoutInflater)
        val dialog2 = Dialog(this)
        dialog2.setContentView(binding2.root)
        val window = dialog2.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.setCanceledOnTouchOutside(true)
        dialog2.setCancelable(true)
        binding2.btnExit.setOnClickListener {
            dialog2.dismiss()

            finish()
            super.onBackPressed()
        }
        binding2.btnStay.setOnClickListener {
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
        dialog2.show()
    }

    private fun getImagesFromMediaStore(context: Context): List<String> {
        val imagePaths = mutableListOf<String>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                imagePaths.add(imagePath)
            }
            imagePaths.reverse()
        } ?: run {
            Toast.makeText(context, "No images found", Toast.LENGTH_SHORT).show()
        }
        return imagePaths
    }


    private fun addText() {
        binding.layoutAddText.ivClose.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE

        }
        binding.layoutAddText.ivDone.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE

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

    private fun setupRecyclerView2() {
        val listOfPaths: List<String> = getImagesFromMediaStore(this)
        val imageModels: MutableList<ImageModel> = listOfPaths.mapIndexed { index, path ->
            ImageModel(
                id = index.toLong(),
                dateTaken = System.currentTimeMillis(),
                fileName = File(path).name,
                filePath = path,
                album = "",
                uri = Uri.EMPTY,
                isCameraItem = false
            )
        }.toMutableList()
        photoAdapter = PhotoAdapter(
            context = this,
            images = imageModels,
            onItemSelected = { imageModel, isSelected ->

                if (isSelected) {
                    val photoPath = imageModel.filePath
                    val bitmap = BitmapFactory.decodeFile(photoPath) ?: return@PhotoAdapter

                    val scaledWidth = 480
                    val scaledHeight = scaledWidth * bitmap.height / bitmap.width

                    val stickerIcon = StickerPhoto(
                        x = 0f,
                        y = 0f,
                        rotation = 0f,
                        bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false),
                        scaleX = 1f,
                        scaleY = 1f
                    )
                    val stickerView = StickerPhotoView(this, null, stickerIcon).apply {
                        setImageBitmap(stickerIcon.bitmap)
                    }
                    binding.stickerContainerView.addView(stickerView)
                } else {
                }
            },
            onCameraClick = {
                val bottomSheet = BottomSheetDialogCameraSticker.newInstance()
                bottomSheet.onDone = { photoPath ->
                    val bitmap = BitmapFactory.decodeFile(photoPath)
                    if (bitmap != null) {
                        val scaledWidth = 480
                        val scaledHeight = scaledWidth * bitmap.height / bitmap.width
                        val stickerIcon = StickerPhoto(
                            x = 0f,
                            y = 0f,
                            rotation = 0f,
                            bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false),
                            scaleX = 1f,
                            scaleY = 1f
                        )
                        val stickerView = StickerPhotoView(this, null, stickerIcon).apply {
                            setImageBitmap(stickerIcon.bitmap)
                        }

                        binding.stickerContainerView.addView(stickerView)
                    }

                }
                bottomSheet.show(supportFragmentManager, "CameraBottomSheet")
            }
        )

        photoAdapter.addCameraItem()
        binding.layoutAddImage.rcvPhotoSrc.layoutManager = GridLayoutManager(this, 3)
        binding.layoutAddImage.rcvPhotoSrc.adapter = photoAdapter
    }
}