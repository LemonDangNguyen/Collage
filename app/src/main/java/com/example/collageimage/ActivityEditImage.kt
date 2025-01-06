package com.example.collageimage

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.CustomBg.CustomImageAdapter
import com.example.collageimage.CustomBg.CustomImageViewModel
import com.example.collageimage.Gradient.GradientAdapter
import com.example.collageimage.Gradient.GradientViewModel
import com.example.collageimage.color.ColorAdapter
import com.example.collageimage.color.ColorItem
import com.example.collageimage.color.ColorMode
import com.example.collageimage.color.OnColorClickListener
import com.example.collageimage.databinding.ActivityEditImageBinding
import com.example.collageimage.frame.FrameAdapter
import com.example.collageimage.frame.FrameItem
import com.example.collageimage.ratio.AspectRatioViewModel
import com.example.collageimage.ratio.adapter.RatioAdapter
import yuku.ambilwarna.AmbilWarnaDialog


class ActivityEditImage : BaseActivity(), OnColorClickListener {

    private val binding by lazy { ActivityEditImageBinding.inflate(layoutInflater) }
    private lateinit var frameAdapter: FrameAdapter
    private val viewModelRatio: AspectRatioViewModel by viewModels()
    private var currentColor: Int = 0xFFFFFFFF.toInt()
    private var currentColorMode: ColorMode = ColorMode.BORDER
    private val customImageViewModel: CustomImageViewModel by viewModels()
    private val customGradientViewModel: GradientViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
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
        nitview()
        val imagePath = intent.getStringExtra("selected_image_path")
        if (imagePath != null) {
            displayImage(imagePath)
        }
        loadimgCam()
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
        val imageUri = Uri.parse(imagePath)
        binding.edtimgView.setImageURI(imageUri)
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
            bgFun()
        }
        binding.layoutParentTool.llChangeFrame.setOnClickListener {
            setupRecyclerView()
            binding.layoutFrame.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
            frameFun()
        }
        binding.layoutParentTool.llChangeText.setOnClickListener {
            binding.layoutAddText.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFilter.setOnClickListener {
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.changeDraw.setOnClickListener {
            binding.barDrawing.root.visibility = View.VISIBLE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.addImage.setOnClickListener {
            binding.layoutParentTool.root.visibility = View.GONE
        }
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
        binding.layoutBg.ivClose.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.ivDone.setOnClickListener {
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

    private fun updatebgFrame(frameLayout: FrameLayout, ratio: Float) {
        val layoutParams = frameLayout.layoutParams
        val parentWidth = frameLayout.width
        val height = (parentWidth / ratio).toInt()
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

    private fun colorrecylayout() {

        colorAdapter = ColorAdapter(colors, this)
        binding.layoutBg.rvColor.apply {
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

}