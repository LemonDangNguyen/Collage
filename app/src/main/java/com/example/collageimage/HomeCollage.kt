package com.example.collageimage;

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
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
import com.example.collageimage.adjust.AdjustMode
import com.example.collageimage.adjust.ImageAdjustmentViewModel
import com.example.collageimage.adjust.filter.FilterListener
import com.example.collageimage.adjust.filter.FilterViewAdapter
import com.example.collageimage.adjust.filter.PhotoEditor
import com.example.collageimage.adjust.filter.PhotoFilter
import com.example.collageimage.color.ColorAdapter
import com.example.collageimage.color.ColorItem
import com.example.collageimage.color.ColorItem2
import com.example.collageimage.color.ColorMode
import com.example.collageimage.color.OnColorClickListener
import com.example.collageimage.databinding.ActivityHomeCollageBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.frame.FrameAdapter
import com.example.collageimage.frame.FrameItem
import com.example.collageimage.ratio.AspectRatioViewModel
import com.example.collageimage.ratio.adapter.RatioAdapter
import com.example.selectpic.ddat.PuzzleUtils
import com.example.selectpic.ddat.RepoPuzzleUtils
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.UseCasePuzzleLayouts
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.adapter.AdapterPuzzleLayoutsPieces
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayouts
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayoutsProvider
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.utils.PuzzlePiece
import com.hypersoft.pzlayout.view.PuzzleView
import yuku.ambilwarna.AmbilWarnaDialog

class HomeCollage : BaseActivity(), PuzzleView.OnPieceClick, PuzzleView.OnPieceSelectedListener,
    OnColorClickListener, FilterListener {

    private val binding by lazy { ActivityHomeCollageBinding.inflate(layoutInflater) }
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
    val colors = listOf(
        ColorItem("#F6F6F6"), ColorItem("#00BD4C"), ColorItem("#A4A4A4"),
        ColorItem("#805638"), ColorItem("#D0D0D0"), ColorItem("#0A0A0A"),
        ColorItem("#00C7AF"), ColorItem("#FF2768"), ColorItem("#AD28FF"),
        ColorItem("#FF8615"), ColorItem("#2EA7FF"), ColorItem("#007A5D"),
        ColorItem("#BA85FE"), ColorItem("#933EFF"), ColorItem("#350077"),
        ColorItem("#E8F403"), ColorItem("#F403D4")
    )

    val viewModel: ImageAdjustmentViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var frameAdapter: FrameAdapter



    private var currentColor: Int = 0xFFFFFFFF.toInt()
    private var mList: List<ImageModel> = mutableListOf()
    private val adapterPuzzleLayoutsPieces by lazy { AdapterPuzzleLayoutsPieces(itemClick) }
    private val viewModelRatio: AspectRatioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val selectedImages: ArrayList<ImageModel>? =
            intent.getParcelableArrayListExtra("SELECTED_IMAGES")
        selectedImages?.let {
            mList = it
            fetchLayouts(it)
            checkImageSizeAndSetLayouts(it)
        } ?: Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
        setupListeners()
        initObservers()
        initRecyclerView()
        initListener()
        btntParentBottom()
        layoutToolFunc()
        layoutBgFunc()
        layoutFrameFunc()
        colorrecylayout()
        layoutStickerFunc()
        layoutFilterandAdjustFunc()
        filterrcl()
        initListener2()
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
            binding.layoutAddText.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFilter.setOnClickListener {
            binding.barFilterAndAdjust.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.changeDraw.setOnClickListener {
            binding.barDrawing.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.addImage.setOnClickListener {
            binding.barAddImage.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.GONE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
    }

    private fun layoutToolFunc() {
        binding.layoutLayout.ivDone.setOnClickListener {
            adapterPuzzleLayoutsPieces.confirmSelection()
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.layoutLayout.ivClose.setOnClickListener {
            adapterPuzzleLayoutsPieces.discardSelection()
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
        }
        binding.layoutLayout.layout.setOnClickListener {
            binding.layoutLayout.layoutBorderColor.visibility = View.GONE
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.VISIBLE
            binding.layoutLayout.rvRatio.visibility = View.GONE
            binding.layoutLayout.layoutBorder.visibility = View.GONE

        }
        binding.layoutLayout.ratio.setOnClickListener {
            binding.layoutLayout.layoutBorderColor.visibility = View.GONE
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.GONE
            binding.layoutLayout.rvRatio.visibility = View.VISIBLE
            binding.layoutLayout.layoutBorder.visibility = View.GONE
            setRatio()
        }
        binding.layoutLayout.llBorder.setOnClickListener {
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


    private fun layoutBgFunc() {
        binding.layoutBg.ivClose.setOnClickListener {
            binding.linearLayout.visibility = View.VISIBLE
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.ivDone.setOnClickListener {
            binding.linearLayout.visibility = View.VISIBLE
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.seleccolor.setOnClickListener {
            openColorPickerDialog2()
        }
        currentColorMode = ColorMode.BACKGROUND
        binding.layoutBg.tvColor.setOnClickListener {
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            currentColorMode = ColorMode.BACKGROUND
            binding.layoutBg.seleccolor.setOnClickListener {
                openColorPickerDialog2()
            }
            binding.layoutBg.rvColorln.visibility = View.VISIBLE
        }

        binding.layoutBg.tvCustom.setOnClickListener {
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.VISIBLE
            setBgCus()
        }
        binding.layoutBg.tvGradient.setOnClickListener {
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            binding.layoutBg.rvGradient.visibility = View.VISIBLE
            setBgGradi()
        }
        binding.layoutBg.tvBlur.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }
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
            val frameLayout = findViewById<FrameLayout>(R.id.framebg)
            frameLayout.background = drawable
        }
        binding.layoutFrame.rvFrame.apply {
            layoutManager = GridLayoutManager(this@HomeCollage, 5)
            adapter = frameAdapter
        }
    }

    private fun layoutAddTextFunc() {

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

    private fun filterrcl(){
       // binding.barFilterAndAdjust.rcvFilter.adapter = FilterViewAdapter(this)
        val llmFilters = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
        binding.barFilterAndAdjust.rcvFilters.layoutManager = llmFilters
        binding.barFilterAndAdjust.rcvFilters.adapter = mFilterViewAdapter
    }
    private fun layoutStickerFunc() {
        binding.barStickers.btnDismissStickerPicker.setOnClickListener {
            binding.barStickers.root.visibility = View.GONE
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

    private fun setupListeners() = binding.apply {
//        listOf(pmirror to ::mirror, pflip to ::flip, protate to ::rotate,
//            pzoomplus to ::zoomPlus, pzoomminus to ::zoomMinus,
//            pleft to ::left, pright to ::right, pup to ::up, pdown to ::down).forEach { (view, action) ->
//            view.setOnClickListener { action() }
//        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
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

    override fun onPieceClick() {}
    override fun onSwapGetPositions(pos1: Int, pos2: Int) {}
    override fun onPieceSelected(piece: PuzzlePiece?, position: Int) {}
    override fun onBackPressed() {

        val binding2 = DialogExitBinding.inflate(layoutInflater)
        val dialog2 = Dialog(this)
        dialog2.setContentView(binding2.root)
        val window = dialog2.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.setCanceledOnTouchOutside(false)
        dialog2.setCancelable(false)
        binding2.btnExit.setOnClickListener {
            dialog2.dismiss()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            super.onBackPressed()
        }
        binding2.btnStay.setOnClickListener {
            dialog2.dismiss()
        }
        dialog2.show()
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
        binding.layoutBg.rvCustom.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutBg.rvCustom.adapter = adapter

        customImageViewModel.customImages.observe(this, { images ->
            adapter.updateImages(images)
        })
    }

    fun  setBgGradi() {
        val adapter = GradientAdapter(emptyList()) { gradient ->
            binding.puzzleView.setBackgroundImage(gradient.resourceId)
        }
        binding.layoutBg.rvGradient.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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

}