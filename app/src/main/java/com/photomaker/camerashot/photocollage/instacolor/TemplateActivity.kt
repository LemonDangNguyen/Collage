package com.photomaker.camerashot.photocollage.instacolor

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
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
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.adapter.IconAdapter
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.adapter.IconCategoryAdapter
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerIcon
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.view.StickerIconView
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.color.ColorAdapter
import com.photomaker.camerashot.photocollage.instacolor.color.ColorItem
import com.photomaker.camerashot.photocollage.instacolor.color.OnColorClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.setUpDialog
import com.photomaker.camerashot.photocollage.instacolor.ratio.adapter.FontAdapter
import com.photomaker.camerashot.photocollage.instacolor.saveImage.SaveFromEditImage
import com.photomaker.camerashot.photocollage.instacolor.view_template.TemplateModel
import com.photomaker.camerashot.photocollage.instacolor.view_template.TemplateViewModel
import com.photomaker.camerashot.photocollage.instacolor.extensions.showToast
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.cbFetchInterval
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivityTemplateBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.DialogSaveBeforeClosingBinding
import com.photomaker.camerashot.photocollage.instacolor.extensions.invisible
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class TemplateActivity : BaseActivity<ActivityTemplateBinding>(ActivityTemplateBinding::inflate), OnColorClickListener {

    private val templateViewModel: TemplateViewModel by viewModels()

    private var selectedPathIndex: Int = -1

    private var categoryAdapter: IconCategoryAdapter? = null
    private var iconAdapter: IconAdapter? = null

    private val stickerData = mutableMapOf<String, List<String>>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) saveFlParentAsImage()
        else Toast.makeText(this, "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImagePath = result.data?.getStringExtra("selected_image_path")
                selectedImagePath?.let {
                    val selectedBitmap = BitmapFactory.decodeFile(it)
                    if (selectedPathIndex != -1) {
                        templateViewModel.setSelectedImage(selectedBitmap)
                        binding.viewTemplate.setSelectedImage(selectedBitmap, selectedPathIndex)
                    }
                }
            }
        }

    private val colors = listOf(
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

        val imageId = intent.getIntExtra("imageId", -1)
        if (imageId != -1) {
            templateViewModel.loadTemplates()
            lifecycleScope.launch {
                templateViewModel.getTemplateById(imageId)?.let {
                    setupTemplate(it)
                }
            }
        }
        binding.btnBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnChangeImage.setOnUnDoubleClickListener {
            if (selectedPathIndex != -1) openSelectImage(selectedPathIndex)
            else showToast(getString(R.string.select_image_to_change), Gravity.CENTER)
        }

        addSticker()
        addText()

        binding.btnSave.setOnUnDoubleClickListener { actionSave() }
    }

    private fun saveFlParentAsImage() {
        saveBitmapToGallery(getBitmapFromView(binding.flParent), onDone = {
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
        var fos: OutputStream?
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
        binding.viewTemplate.setBackgroundDrawable(template.backgroundImageResId)
        template.stringPaths.forEachIndexed { index, path ->
            binding.viewTemplate.setPath(index, path)
        }

        binding.viewTemplate.setOnPathClickListener { pathIndex ->
            selectedPathIndex = pathIndex
            if (binding.viewTemplate.isPathEmpty(pathIndex)) openSelectImage(pathIndex)
        }
        binding.viewTemplate.setOnBitmapClickListener { bitmapIndex ->
            selectedPathIndex = bitmapIndex
        }
    }

    private fun openSelectImage(pathIndex: Int) {
        val intent = Intent(this, SelectImageTemplate::class.java).apply {
            putExtra("selected_path", pathIndex)
        }
        selectImageLauncher.launch(intent)
    }

    private fun addSticker() {
        loadStickerData()
        binding.btnSticker.setOnUnDoubleClickListener {
            binding.barStickers.root.visible()
            binding.lnBottomBar.invisible()
        }
        binding.barStickers.icClose.setOnUnDoubleClickListener {
            binding.barStickers.root.gone()
            binding.lnBottomBar.visible()
        }
        binding.barStickers.btnDoneSticker.setOnUnDoubleClickListener {
            binding.barStickers.root.gone()
            binding.lnBottomBar.visible()
        }

        categoryAdapter = IconCategoryAdapter(stickerData) { category ->
            updateStickers(category)
        }
        binding.barStickers.rcvStickerCategory.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(this@TemplateActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        iconAdapter = IconAdapter(emptyList())
        iconAdapter?.onStickerClick = { stickerPath ->
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
            categoryAdapter?.setSelectedCategory(firstCategory)
        }
    }

    private fun loadStickerData() {
        val stickerFolder = "sticker"
        val folders = assets.list(stickerFolder) ?: emptyArray()

        for (folder in folders) {
            val filePaths = assets.list("$stickerFolder/$folder")?.filter {
                it.endsWith(".webp")
            }?.map {
                "$stickerFolder/$folder/$it"
            } ?: emptyList()
            stickerData[folder] = filePaths
        }
    }

    private fun updateStickers(category: String) {
        val stickers = stickerData[category] ?: emptyList()
        iconAdapter?.updateData(stickers)
    }

    private fun colortextsticker() {
        binding.layoutAddText.rvTextColor.apply {
            layoutManager = LinearLayoutManager(this@TemplateActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = ColorAdapter(colors, this@TemplateActivity)
        }
    }
    private fun addText() {
        binding.btnText.setOnUnDoubleClickListener {
            showToast(getString(R.string.commingsoon), Gravity.CENTER)
        }

        binding.layoutAddText.ivClose.setOnUnDoubleClickListener {
            binding.layoutAddText.root.gone()
            binding.lnBottomBar.visible()
        }
        binding.layoutAddText.ivDone.setOnUnDoubleClickListener {
            binding.layoutAddText.root.gone()
            binding.lnBottomBar.visible()
        }

        binding.layoutAddText.tvFont.setOnUnDoubleClickListener {
            updateTextViewStyle2(binding.layoutAddText.tvFont)
            binding.layoutAddText.llColor.gone()
            binding.layoutAddText.rvTextColor.gone()
            binding.layoutAddText.rvFont.visible()
        }

        binding.layoutAddText.tvColor.setOnUnDoubleClickListener {
            colortextsticker()
            updateTextViewStyle2(binding.layoutAddText.tvColor)
            binding.layoutAddText.llColor.visible()
            binding.layoutAddText.rvTextColor.visible()
            binding.layoutAddText.rvFont.gone()
        }

        binding.layoutAddText.rvFont.layoutManager = GridLayoutManager(this, 2)
        binding.layoutAddText.rvFont.adapter = FontAdapter(getFontsFromAssets(), this) { fontName ->
            Toast.makeText(this, "Selected font: $fontName", Toast.LENGTH_SHORT).show()
        }

        binding.layoutAddText.tvText.setOnUnDoubleClickListener {
            binding.layoutAddText.tvText.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.layoutAddText.tvText.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorPrimary)
            binding.layoutAddText.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvLabel.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent))
            binding.layoutAddText.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvBorder.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
        }
        binding.layoutAddText.tvLabel.setOnUnDoubleClickListener {
            binding.layoutAddText.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.layoutAddText.tvLabel.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorPrimary)
            binding.layoutAddText.tvText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvText.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
            binding.layoutAddText.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutAddText.tvBorder.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
        }
        binding.layoutAddText.tvBorder.setOnUnDoubleClickListener {
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
        binding.layoutAddText.tvColor.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.layoutAddText.tvColor.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
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
            if (fonts != null) fontList.addAll(fonts)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return fontList
    }

    override fun onColorClick(color: ColorItem) {
        Toast.makeText(this, "Selected color: ${Color.parseColor(color.colorHex)}", Toast.LENGTH_SHORT).show()
    }

    private fun actionSave() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when {
                checkPer(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) -> saveFlParentAsImage()

                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    Toast.makeText(
                        this,
                        "Permission needed to save images.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }

                else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else saveFlParentAsImage()
    }

    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_banner_all) {
            binding.banner.visible()
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval
            config.defaultCBFetchIntervalSec = cbFetchInterval
            config.defaultAdUnitId = getString(R.string.banner_all)
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }


    private fun showDialogBackSave() {
        val bindingDialog = DialogSaveBeforeClosingBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this@TemplateActivity, R.style.SheetDialog).create()
        dialog.setUpDialog(bindingDialog.root, false)

        binding.banner.gone()
        bindingDialog.root.layoutParams.width = (93.33f * w).toInt()

        showNativeDialog(bindingDialog)

        bindingDialog.btnExit.setOnUnDoubleClickListener {
            dialog.dismiss()
            showInterBack()
        }
        bindingDialog.btnStay.setOnUnDoubleClickListener { actionSave() }

        dialog.setOnDismissListener { binding.banner.visible() }
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
        } else finish()
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
        } else startActivity(intent)
    }

    private fun showNativeDialog(bindingDialog: DialogSaveBeforeClosingBinding) {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_save) {
            bindingDialog.layoutNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAdsDialog(bindingDialog, it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_all),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAdsDialog(bindingDialog, nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            bindingDialog.frAds.removeAllViews()
                        }
                    }
                )
            }
        } else  bindingDialog.layoutNative.gone()
    }
    private fun pushViewAdsDialog(bindingDialog: DialogSaveBeforeClosingBinding, nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (!AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)

        bindingDialog.frAds.removeAllViews()
        bindingDialog.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }

}

