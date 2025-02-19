package com.example.collageimage;

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import com.example.collageimage.adjust.AdjustMode
import com.example.collageimage.adjust.ImageAdjustmentViewModel
import com.example.collageimage.adjust.filter.FilterListener
import com.example.collageimage.adjust.filter.FilterViewAdapter
import com.example.collageimage.adjust.filter.PhotoEditor
import com.example.collageimage.adjust.filter.PhotoFilter
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.color.ColorAdapter
import com.example.collageimage.color.ColorItem
import com.example.collageimage.color.ColorItem2
import com.example.collageimage.color.ColorMode
import com.example.collageimage.color.ColorPenAdapter
import com.example.collageimage.color.OnColorClickListener
import com.example.collageimage.color.OnColorClickListener2
import com.example.collageimage.databinding.ActivityHomeCollageBinding
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.setOnUnDoubleClickListener
import com.example.collageimage.extensions.setUpDialog
import com.example.collageimage.frame.FrameAdapter
import com.example.collageimage.frame.FrameItem
import com.example.collageimage.ratio.AspectRatioViewModel
import com.example.collageimage.ratio.adapter.FontAdapter
import com.example.collageimage.ratio.adapter.RatioAdapter
import com.example.collageimage.saveImage.SaveFromEditImage
import com.example.selectpic.ddat.PuzzleUtils
import com.example.selectpic.ddat.RepoPuzzleUtils
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.UseCasePuzzleLayouts
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.example.collageimage.lib.AdapterPuzzleLayoutsPieces
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayouts
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayoutsProvider
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.utils.PuzzlePiece
import com.hypersoft.pzlayout.view.PuzzleView
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
import com.nmh.base_lib.callback.ICallBackCheck
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class HomeCollage : BaseActivity<ActivityHomeCollageBinding>(ActivityHomeCollageBinding::inflate), PuzzleView.OnPieceClick, PuzzleView.OnPieceSelectedListener,
    OnColorClickListener, FilterListener, OnColorClickListener2 {

    private val mediaStoreMediaImages by lazy { MediaStoreMediaImages(contentResolver) }
    private val useCaseMediaImageDetail by lazy {
        UseCaseMediaImageDetail(
            RepositoryMediaImages(
                mediaStoreMediaImages
            )
        )
    }
    private val viewModelMediaImageDetail by viewModels<ViewModelMediaImageDetail> {
        ViewModelMediaImageDetailProvider(
            useCaseMediaImageDetail
        )
    }
    private val viewModelPuzzleLayouts by viewModels<ViewModelPuzzleLayouts> {
        ViewModelPuzzleLayoutsProvider(
            UseCasePuzzleLayouts(RepoPuzzleUtils(PuzzleUtils()))
        )
    }
    private val customImageViewModel: CustomImageViewModel by viewModels()
    private val customGradientViewModel: GradientViewModel by viewModels()
    private var currentColorMode: ColorMode = ColorMode.BORDER
    private var currentAdjustMode: AdjustMode = AdjustMode.BRIGHTNESS
    private val mFilterViewAdapter = FilterViewAdapter(this)
    lateinit var mPhotoEditor: PhotoEditor
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
    val viewModel: ImageAdjustmentViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var frameAdapter: FrameAdapter

    private var currentColor: Int = 0xFFFFFFFF.toInt()
    private var mList: List<ImageModel> = mutableListOf()
    private val adapterPuzzleLayoutsPieces by lazy { AdapterPuzzleLayoutsPieces(itemClick) }
    private val viewModelRatio: AspectRatioViewModel by viewModels()
    private var colorAdapterpen: ColorPenAdapter? = null


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

    private fun btntParentBottom() {
        binding.layoutParentTool.llChangeLayout.setOnClickListener {
            binding.layoutLayout.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeBG.setOnClickListener {
            binding.layoutBg.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFrame.setOnClickListener {
            setupRecyclerView()
            binding.layoutFrame.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeText.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//            addText()
//            binding.layoutAddText.root.visibility = View.VISIBLE
//            binding.linearLayout.visibility = View.GONE

        }
        binding.layoutParentTool.llChangeFilter.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//            binding.barFilterAndAdjust.root.visibility = View.VISIBLE
//            binding.linearLayout.visibility = View.GONE
//            binding.layoutLayout.root.visibility = View.GONE
//            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
            layoutStickerFunc()
        }
        binding.layoutParentTool.changeDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(true)
            binding.barDrawing.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
            drawFun()
        }
        binding.layoutParentTool.addImage.setOnClickListener {
            binding.layoutAddImage.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
            setupRecyclerView2()
            binding.layoutAddImage.icClose.setOnClickListener {
                binding.layoutAddImage.root.visibility = View.GONE
                binding.layoutParentTool.root.visibility = View.VISIBLE
                binding.linearLayout.visibility = View.VISIBLE
            }
            binding.layoutAddImage.btnDoneAddImage.setOnClickListener {
                binding.layoutAddImage.root.visibility = View.GONE
                binding.layoutParentTool.root.visibility = View.VISIBLE
                binding.linearLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun layoutToolFunc() {
        binding.layoutLayout.ivDone.setOnClickListener {
            currentColorMode = ColorMode.BACKGROUND
            adapterPuzzleLayoutsPieces.confirmSelection()
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.layoutLayout.ivClose.setOnClickListener {
            currentColorMode = ColorMode.BACKGROUND
            adapterPuzzleLayoutsPieces.discardSelection()
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.layoutLayout.layout.setOnClickListener {
            binding.layoutLayout.tvLayout.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutLayout.ivLayout.setImageResource(R.drawable.ic_layout_selected)
            binding.layoutLayout.tvRatio.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivRatio.setImageResource(R.drawable.ic_ratio)
            binding.layoutLayout.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorder.setImageResource(R.drawable.ic_border)
            binding.layoutLayout.tvBorderColor.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorderColor.setImageResource(R.drawable.ic_border_color)

            binding.layoutLayout.layoutBorderColor.visibility = View.GONE
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.VISIBLE
            binding.layoutLayout.rvRatio.visibility = View.GONE
            binding.layoutLayout.layoutBorder.visibility = View.GONE

        }
        binding.layoutLayout.ratio.setOnClickListener {

            binding.layoutLayout.tvLayout.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivLayout.setImageResource(R.drawable.ic_layout)
            binding.layoutLayout.tvRatio.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutLayout.ivRatio.setImageResource(R.drawable.ic_ratio_selected)
            binding.layoutLayout.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorder.setImageResource(R.drawable.ic_border)
            binding.layoutLayout.tvBorderColor.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorderColor.setImageResource(R.drawable.ic_border_color)

            binding.layoutLayout.layoutBorderColor.visibility = View.GONE
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.GONE
            binding.layoutLayout.rvRatio.visibility = View.VISIBLE
            binding.layoutLayout.layoutBorder.visibility = View.GONE
            setRatio()
        }
        binding.layoutLayout.llBorder.setOnClickListener {

            binding.layoutLayout.tvLayout.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivLayout.setImageResource(R.drawable.ic_layout)
            binding.layoutLayout.tvRatio.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivRatio.setImageResource(R.drawable.ic_ratio)
            binding.layoutLayout.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutLayout.ivBorder.setImageResource(R.drawable.ic_border_selected)
            binding.layoutLayout.tvBorderColor.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorderColor.setImageResource(R.drawable.ic_border_color)

            binding.layoutLayout.layoutBorderColor.visibility = View.GONE
            binding.layoutLayout.ivClose.visibility = View.GONE
            binding.layoutLayout.ivRefresh.visibility = View.VISIBLE
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.GONE
            binding.layoutLayout.rvRatio.visibility = View.GONE
            binding.layoutLayout.layoutBorder.visibility = View.VISIBLE
            corner()
            padding()
        }
        binding.layoutLayout.llBorderColor.setOnClickListener {

            binding.layoutLayout.tvLayout.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivLayout.setImageResource(R.drawable.ic_layout)
            binding.layoutLayout.tvRatio.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivRatio.setImageResource(R.drawable.ic_ratio)
            binding.layoutLayout.tvBorder.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.layoutLayout.ivBorder.setImageResource(R.drawable.ic_border)
            binding.layoutLayout.tvBorderColor.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            binding.layoutLayout.ivBorderColor.setImageResource(R.drawable.ic_border_color_selected)

            currentColorMode = ColorMode.BORDER
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.GONE
            binding.layoutLayout.rvRatio.visibility = View.GONE
            binding.layoutLayout.layoutBorder.visibility = View.GONE
            binding.layoutLayout.layoutBorderColor.visibility = View.VISIBLE

            binding.layoutLayout.selectbodercolor.setOnClickListener {
                openColorPickerDialog()
            }
        }

    }

    private fun bgFun() {
        currentColorMode = ColorMode.BACKGROUND
        binding.layoutBg.seleccolor.setOnClickListener{
            openColorPickerDialog2()
        }
        colorrecylayout()

        binding.layoutBg.ivClose.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.layoutBg.ivDone.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }

        binding.layoutBg.tvColor.setOnClickListener {
            updateTextViewStyle(binding.layoutBg.tvColor)
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            binding.layoutBg.rvColorln.visibility = View.VISIBLE
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
           // updateTextViewStyle(binding.layoutBg.tvBlur)
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

    private fun layoutFrameFunc() {
        binding.layoutFrame.ivRefresh.setOnClickListener {
            binding.framebg.background = null
            binding.layoutFrame.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }

        binding.layoutFrame.ivDone.setOnClickListener {
            binding.layoutFrame.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        val frames = (1..20).map { FrameItem("frames/frame_$it.webp") }

        frameAdapter = FrameAdapter(frames) { drawable ->
            binding.framebg.background = drawable
        }

        binding.layoutFrame.rvFrame.apply {
            layoutManager = GridLayoutManager(this@HomeCollage, 5)
            adapter = frameAdapter
        }
    }

    private fun drawFun() {
        colorrecyPenlayout()
        binding.barDrawing.btnCancelDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(false)
            binding.barDrawing.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.barDrawing.btnApplyDraw.setOnClickListener {
            binding.drawview.setInteractionEnabled(false)
            binding.barDrawing.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
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
    private fun colorrecyPenlayout() {
        colorAdapterpen = ColorPenAdapter(pencolors, this)
        binding.barDrawing.rcvColorDraw.apply {
            layoutManager =
                LinearLayoutManager(this@HomeCollage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapterpen
        }
    }
    private fun layoutFilterandAdjustFunc() {
        binding.barFilterAndAdjust.btnDone.setOnClickListener {
            binding.barFilterAndAdjust.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
            binding.barFilterAndAdjust.layoutAdjustfunc.root.visibility = View.GONE
            binding.barFilterAndAdjust.layoutFilterControl.visibility = View.GONE
        }
        binding.barFilterAndAdjust.btnRedoFilter.setOnClickListener {
            binding.barFilterAndAdjust.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
            binding.barFilterAndAdjust.layoutAdjustfunc.root.visibility = View.GONE
        }
        binding.barFilterAndAdjust.tabAdjust.setOnClickListener {
            binding.barFilterAndAdjust.layoutAdjustfunc.root.visibility = View.VISIBLE
        }

        binding.barFilterAndAdjust.layoutAdjustfunc.icBrightness.setOnClickListener {
            Toast.makeText(this, "Brightness", Toast.LENGTH_SHORT).show()
            currentAdjustMode = AdjustMode.BRIGHTNESS
            binding.barFilterAndAdjust.sbAdjust.max = 100
            binding.barFilterAndAdjust.sbAdjust.progress = 50
            initListener2()
        }

    }

    private fun initListener2() = binding.apply {
        binding.barFilterAndAdjust.sbAdjust.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.barFilterAndAdjust.tvFilterPercent.text = progress.toString()
                    when (currentAdjustMode) {
                        AdjustMode.BRIGHTNESS -> {
                            val brightnessValue = (progress - 50) / 50f
                            viewModel.brightness.value = brightnessValue
                            viewModel.updateFilter()
                        }

                        AdjustMode.CONTRAST -> TODO()
                        AdjustMode.SATURATION -> TODO()
                        AdjustMode.HIGHTLIGHT -> TODO()
                        AdjustMode.SHADOW -> TODO()
                        AdjustMode.WARMTH -> TODO()
                        AdjustMode.VIGNETTE -> TODO()
                        AdjustMode.HUE -> TODO()
                        AdjustMode.TINT -> TODO()
                        AdjustMode.FADE -> TODO()
                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun filterrcl() {
        // binding.barFilterAndAdjust.rcvFilter.adapter = FilterViewAdapter(this)
        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.barFilterAndAdjust.rcvFilters.layoutManager = llmFilters
        binding.barFilterAndAdjust.rcvFilters.adapter = mFilterViewAdapter
    }

    private fun layoutStickerFunc() {
        addSticker()
        binding.barStickers.icClose.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.barStickers.btnDoneSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
    }

    private fun openColorPickerDialog() {
        val colorPicker =
            AmbilWarnaDialog(this, currentColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    currentColor = color
                    binding.puzzleView.setBorderColor(color)
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
                    binding.puzzleView.setBackgroundColor(color)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
        colorPicker.show()
    }

    private fun colorrecylayout() {
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutLayout.rvColor.apply {
            layoutManager =
                LinearLayoutManager(this@HomeCollage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutBg.rvColor.apply {
            layoutManager =
                LinearLayoutManager(this@HomeCollage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
    }


    private fun initListener() = binding.apply {
        layoutLayout.sbBorder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.puzzleView.setPiecePadding(progress.toFloat())
                    binding.layoutLayout.tvRecentpd.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        layoutLayout.sbCorner.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.puzzleView.setPieceRadian(progress.toFloat())
                    binding.layoutLayout.tvRecent.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        layoutLayout.sbBorderSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.puzzleView.setBorderWidth(progress.toFloat())
                    binding.layoutLayout.tvValueBorderSize.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initRecyclerView() {
        binding.layoutLayout.rcvListPuzzleLayouts.adapter = adapterPuzzleLayoutsPieces
    }

    private val itemClick: ((PuzzleLayout, theme: Int) -> Unit) = { _, theme ->
        viewModelPuzzleLayouts.getPuzzleLayout(1, mList.size, theme)

    }

    private fun initObservers() {
        viewModelMediaImageDetail.clickedImagesLiveData.observe(this) {
            mList = it
            if (mList.isEmpty()) {
                Toast.makeText(this, "No images", Toast.LENGTH_SHORT).show()
            } else {
                fetchLayouts(it)
                checkImageSizeAndSetLayouts(it)
            }
        }
        viewModelPuzzleLayouts.puzzleLayoutLiveData.observe(this) { list ->
            initView(list)
        }
        viewModelPuzzleLayouts.puzzleLayoutsLiveData.observe(this) { list ->
            adapterPuzzleLayoutsPieces.setPuzzleLayouts(list)
        }
    }

    @SuppressLint("Recycle")
    private fun initView(list: PuzzleLayout) = binding.puzzleView.apply {
        val context: Context = this.context
//        val ta = context.obtainStyledAttributes(
//            null,
//            com.hypersoft.pzlayout.R.styleable.PuzzleView
//        )
        setPuzzleLayout(list)
        isTouchEnable = true
        selectedLineColor = ContextCompat.getColor(context, R.color.black)
        setHandleBarColor(ContextCompat.getColor(context, R.color.black))
        setAnimateDuration(700)
        piecePadding = 10f
        setOnPieceClickListener(this@HomeCollage)
        setOnPieceSelectedListener(this@HomeCollage)
        post { loadPhotoFromRes(list) }
    }

    private fun loadPhotoFromRes(list: PuzzleLayout) {
        val count = minOf(mList.size, list.areaCount)
        val pieces = mutableListOf<Bitmap>()

        for (i in 0 until count) {
            val target: CustomTarget<Bitmap> = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
                    pieces.add(bitmap)
                    if (pieces.size == count) {
                        val remainingPieces =
                            if (mList.size < list.areaCount) List(list.areaCount) { pieces[it % count] } else pieces
                        binding.puzzleView.addPieces(remainingPieces)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Toast.makeText(this@HomeCollage, "Failed to load image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            Glide.with(this).asBitmap().load(mList[i].uri).into(target)
        }
    }

    private fun fetchLayouts(images: List<ImageModel>) {
        viewModelPuzzleLayouts.getPuzzleLayouts(images.size)
    }

    private fun checkImageSizeAndSetLayouts(images: List<ImageModel>) {
        val size = images.size
        if (size in 1..9) {
            val layoutImages = if (size == 1) listOf(images[0], images[0]) else images
            viewModelPuzzleLayouts.getPuzzleLayout(1, layoutImages.size, 0)
        }
    }

    private fun mirror() = handlePuzzleAction { binding.puzzleView.mirrorPiece() }
    private fun flip() = handlePuzzleAction { binding.puzzleView.flipPiece() }
    private fun rotate() = handlePuzzleAction { binding.puzzleView.rotatePiece() }
    private fun swap() = handlePuzzleAction {
        showToast(getString(R.string.select_other_img), Gravity.CENTER)
        binding.puzzleView.enableSwapMode(true)
    }
    //private fun alpha() = handlePuzzleAction { binding.puzzleView.alphaPiece() }
    private fun zoomPlus() = handlePuzzleAction { binding.puzzleView.zoomInPiece() }
    private fun zoomMinus() = handlePuzzleAction { binding.puzzleView.zoomOutPiece() }
    private fun left() = handlePuzzleAction { binding.puzzleView.moveLeft() }
    private fun right() = handlePuzzleAction { binding.puzzleView.moveRight() }
    private fun up() = handlePuzzleAction { binding.puzzleView.moveUp() }
    private fun down() = handlePuzzleAction { binding.puzzleView.moveDown() }

    private fun corner() = binding.layoutLayout.apply {
        sbCorner.visibility = View.VISIBLE
        sbCorner.max = 100
        sbCorner.progress = binding.puzzleView.getPieceRadian().toInt()
    }

    private fun padding() = binding.layoutLayout.apply {
        sbBorder.visibility = View.VISIBLE
        sbBorder.max = 100
        sbBorder.progress = binding.puzzleView.getPiecePadding().toInt()
    }

    private fun handlePuzzleAction(action: () -> Unit) {
        if (binding.puzzleView.handlingPiecePosition != -1) {
            action()
        } else {
            Toast.makeText(this, R.string.selectsingleimage, Toast.LENGTH_SHORT).show()
        }
    }
//11
override fun onPieceClick() {

    if (binding.layoutEditImage.root.visibility == View.VISIBLE) {
        binding.layoutEditImage.root.visibility = View.GONE
    } else {
        binding.layoutEditImage.root.visibility = View.VISIBLE
    }
    binding.layoutEditImage.ivDone.setOnClickListener {
        binding.layoutEditImage.root.visibility = View.GONE
    }
    binding.layoutEditImage.ivClose.setOnClickListener {
        binding.layoutEditImage.root.visibility = View.GONE
    }

    binding.layoutEditImage.pmirror.setOnClickListener { mirror() }
    binding.layoutEditImage.pflip.setOnClickListener { flip() }
    binding.layoutEditImage.protate.setOnClickListener { rotate() }
    binding.layoutEditImage.pswap.setOnClickListener{
        swap()
    }
    binding.layoutEditImage.pchange.setOnClickListener {
        showToast("Comming Soon!", Gravity.CENTER)
    }
    binding.layoutEditImage.pcrop.setOnClickListener {
        showToast("Comming Soon!", Gravity.CENTER)
    }
    binding.layoutEditImage.pfilter.setOnClickListener {
        showToast("Comming Soon!", Gravity.CENTER)
    }
}

    override fun onSwapGetPositions(pos1: Int, pos2: Int) {}
    override fun onPieceSelected(piece: PuzzlePiece?, position: Int) {

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
        binding.layoutLayout.rvRatio.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.layoutLayout.rvRatio.adapter = adapter
        viewModelRatio.aspectRatio.observe(this)
        { ratio ->
            binding.ivBackground.setAspectRatio(ratio)
            updatePuzzleViewAspectRatio(binding.puzzleView, ratio)
            updatebgFrame(binding.framebg, ratio)
        }
        viewModelRatio.backgroundColor.observe(this) { color ->
            binding.ivBackground.setBackgroundColor(color)
        }

    }

    private fun updatePuzzleViewAspectRatio(puzzleView: PuzzleView, ratio: Float) {
        val layoutParams = puzzleView.layoutParams
        val parentWidth = puzzleView.width
        val height = (parentWidth / ratio).toInt()
        layoutParams.height = height
        puzzleView.layoutParams = layoutParams

    }

    private fun updatebgFrame(frameLayout: FrameLayout, ratio: Float) {
        val layoutParams = frameLayout.layoutParams
        val parentWidth = frameLayout.width
        val height = (parentWidth / ratio).toInt()
        layoutParams.height = height
        frameLayout.layoutParams = layoutParams

    }

    override fun onFilterSelected(photoFilter: PhotoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter)
    }

    fun setBgCus() {
        val adapter = CustomImageAdapter(emptyList()) { image ->
            binding.puzzleView.setBackgroundImage(image.resourceId)
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
            binding.puzzleView.setBackgroundImage(gradient.resourceId)
        }
        binding.layoutBg.rvGradient.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutBg.rvGradient.adapter = adapter

        customGradientViewModel.selectedGradient.observe(this, { images ->
            adapter.updateGradients(images)
        })
    }

    override fun onColorClick(color: ColorItem) {
        val colorInt = Color.parseColor(color.colorHex)
        when (currentColorMode) {
            ColorMode.BORDER -> {
                binding.puzzleView.setBorderColor(colorInt)
            }

            ColorMode.BACKGROUND -> {
                binding.puzzleView.setBackgroundColor(colorInt)
            }
        }
    }

    override fun onColorClick2(color2: ColorItem2) {
        val colorInt = Color.parseColor(color2.colorHex2)
        binding.drawview.setPenColor(colorInt)
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
            layoutManager = LinearLayoutManager(this@HomeCollage, LinearLayoutManager.HORIZONTAL, false)
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
            layoutManager = GridLayoutManager(this@HomeCollage, 4)
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


    private fun colortextsticker() {
        colorAdapter = ColorAdapter(colors, this)
        binding.layoutAddText.rvTextColor.apply {
            layoutManager =
                LinearLayoutManager(this@HomeCollage, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }
    }
    private fun addText() {
        binding.layoutAddText.ivClose.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE


        }
        binding.layoutAddText.ivDone.setOnClickListener {
            binding.layoutAddText.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE

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


    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialogBackCollage()
            }
        })
        val selectedImages: ArrayList<ImageModel>? =
            intent.getParcelableArrayListExtra("SELECTED_IMAGES")
        AdsConfig.loadInterSave(this@HomeCollage)
        selectedImages?.let {
            mList = it
            fetchLayouts(it)
            checkImageSizeAndSetLayouts(it)
        } ?: Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
        loadBanner()
        initObservers()
        initRecyclerView()
        initListener()
        btntParentBottom()
        layoutToolFunc()
        bgFun()
        layoutFrameFunc()
        colorrecylayout()
        layoutFilterandAdjustFunc()
        filterrcl()
        initListener2()
        binding.puzzleView.setLineSize(10)
        binding.tvSave.setOnUnDoubleClickListener {
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
        binding.btnBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun showDialogBackCollage() {
        val bindingDialog = DialogSaveBeforeClosingBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this@HomeCollage, R.style.SheetDialog).create()
        dialog.setUpDialog(bindingDialog.root, true)
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
    }

    private fun showNativedialog(bindingDialog: DialogSaveBeforeClosingBinding) {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() &&AdsConfig.is_load_native_save && AdsConfig.isLoadFullAds()
            && AdsConfig.is_load_native_save) {
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

    private fun saveFlParentAsImage() {
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
    private fun showInterBack() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interBack != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_back) {
            Admob.getInstance().showInterAds(this@HomeCollage, AdsConfig.interBack, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()

                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interBack = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterBack(this@HomeCollage)
                }
            })
        } else {finish()

        }
    }

    private fun showInterSave(intent: Intent) {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interSave != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_save) {
            Admob.getInstance().showInterAds(this@HomeCollage, AdsConfig.interSave, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startActivity(intent)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interSave = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterSave(this@HomeCollage)
                }
            })
        } else {
            startActivity(intent)
        }
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
}