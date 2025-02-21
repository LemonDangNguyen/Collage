package com.example.collageimage

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.setUpDialog
import com.example.collageimage.ratio.adapter.FontAdapter
import com.example.collageimage.saveImage.SaveFromEditImage
import com.example.collageimage.view_template.TemplateModel
import com.example.collageimage.view_template.TemplateViewModel
import com.example.collageimage.view_template.ViewTemplateAdapter
import com.example.collageimage.extensions.showToast
import com.example.collageimage.extensions.visible
import com.example.collageimage.utils.AdsConfig
import com.example.collageimage.utils.AdsConfig.cbFetchInterval
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
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



    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialogBackSave()
            }
        })
        AdsConfig.loadInterSave(this@TemplateActivity)
        loadBanner()
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

    private fun saveFlParentAsImage() {
        val bitmap = getBitmapFromView(binding.flParent)
        saveBitmapToGallery(bitmap, onDone = {
            if (it != "") {
                val intent = Intent(this, SaveFromEditImage::class.java)
                intent.putExtra("image_path", it)
                intent.putExtra("extra_text", "TemplateActivity")
                showInterSave(intent)
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

            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//            binding.layoutAddText.root.visibility = View.VISIBLE
//            binding.lnBottomBar.visibility = View.GONE
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

    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()) {
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval /*cbFetchInterval lấy theo remote*/
            config.defaultCBFetchIntervalSec = cbFetchInterval

            if (true /*thêm biến check remote, thường là switch_banner_collapse*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.CollapsibleBottom
            } else if (true /*thêm biến check remote, thường là banner_all*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            } else {
                binding.banner.gone()
                return
            }
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }


    private fun showDialogBackSave() {

        val bindingDialog = DialogSaveBeforeClosingBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this@TemplateActivity, R.style.SheetDialog).create()
        dialog.setUpDialog(bindingDialog.root, true)
        binding.banner.gone()
        dialog.setCancelable(false)
        bindingDialog.root.layoutParams.width = (93.33f * w).toInt()
        showNativedialog(bindingDialog)

        bindingDialog.btnExit.setOnClickListener {
            dialog.dismiss()
            showInterBack()
        }
        bindingDialog.btnStay.setOnClickListener {
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
                val bitmap = getBitmapFromView(binding.flParent)
                saveBitmapToGallery(bitmap, onDone = {
                    if (it != "") {
                        val intent = Intent(this, SaveFromEditImage::class.java)
                        intent.putExtra("image_path", it)
                        showInterSave(intent)
                    } else {
                        showToast("Failed to save image.", Gravity.CENTER)
                    }
                })
            }
        }
        dialog.setOnDismissListener {
            binding.banner.visible()
        }
    }
    private fun showInterBack() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interBack != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_back) {
            Admob.getInstance().showInterAds(this@TemplateActivity, AdsConfig.interBack, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()

                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interBack = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterBack(this@TemplateActivity)
                }
            })
        } else {finish()

        }
    }

    private fun showInterSave(intent: Intent) {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interSave != null && AdsConfig.checkTimeShowInter()
             && AdsConfig.is_load_inter_save) {
            Admob.getInstance().showInterAds(this@TemplateActivity, AdsConfig.interSave, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startActivity(intent)
                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interSave = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterSave(this@TemplateActivity)
                }
            })
        } else {
            startActivity(intent)
        }
    }
    private fun showNativedialog(bindingDialog: DialogSaveBeforeClosingBinding) {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()&& AdsConfig.isLoadFullAds()&&AdsConfig.is_load_native_save) {
            bindingDialog.layoutNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAdsdialog(bindingDialog, it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_all),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAdsdialog(bindingDialog, nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            bindingDialog.frAds.removeAllViews()
                        }
                    }
                )
            }
        } else  bindingDialog.layoutNative.gone()
    }
    private fun pushViewAdsdialog(bindingDialog: DialogSaveBeforeClosingBinding, nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (!AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)

        bindingDialog.frAds.removeAllViews()
        bindingDialog.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }

}

