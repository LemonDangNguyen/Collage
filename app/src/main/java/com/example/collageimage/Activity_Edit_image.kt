package com.example.collageimage

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.CustomBg.CustomImageAdapter
import com.example.collageimage.Gradient.GradientAdapter
import com.example.collageimage.adjust.filter.PhotoFilter
import com.example.collageimage.color.ColorMode
import com.example.collageimage.databinding.ActivityEditImageBinding
import com.example.collageimage.frame.FrameAdapter
import com.example.collageimage.frame.FrameItem
import com.example.collageimage.ratio.adapter.RatioAdapter
import com.hypersoft.pzlayout.view.PuzzleView

class Activity_Edit_image : BaseActivity() {

    private val binding by lazy { ActivityEditImageBinding.inflate(layoutInflater) }
    private lateinit var frameAdapter: FrameAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        nitview()
        val imagePath = intent.getStringExtra("selected_image_path")
        if (imagePath != null) {
            displayImage(imagePath)
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
//        binding.layoutParentTool.llChangeLayout.setOnClickListener {
//            binding.layoutLayout.root.visibility = View.VISIBLE
//            binding.layoutParentTool.root.visibility = View.GONE
//
//        }
        binding.layoutParentTool.llChangeBG.setOnClickListener {
            binding.layoutBg.root.visibility = View.VISIBLE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFrame.setOnClickListener {
            setupRecyclerView()
            binding.layoutFrame.root.visibility = View.VISIBLE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeText.setOnClickListener {
            binding.layoutAddText.root.visibility = View.VISIBLE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeFilter.setOnClickListener {
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.llChangeSticker.setOnClickListener {
            binding.barStickers.root.visibility = View.VISIBLE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.changeDraw.setOnClickListener {
            binding.barDrawing.root.visibility = View.VISIBLE
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
        binding.layoutParentTool.addImage.setOnClickListener {
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.GONE
        }
    }

    private fun layoutToolFunc() {
        binding.layoutLayout.ivDone.setOnClickListener {
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE

        }
        binding.layoutLayout.ivClose.setOnClickListener {
            binding.layoutLayout.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
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

        }
        binding.layoutLayout.llBorderColor.setOnClickListener {
          //  currentColorMode = ColorMode.BORDER
            binding.layoutLayout.rcvListPuzzleLayouts.visibility = View.GONE
            binding.layoutLayout.rvRatio.visibility = View.GONE
            binding.layoutLayout.layoutBorder.visibility = View.GONE
            binding.layoutLayout.layoutBorderColor.visibility = View.VISIBLE

            binding.layoutLayout.selectbodercolor.setOnClickListener {
             //   openColorPickerDialog()
            }
        }


    }


    private fun layoutBgFunc() {
        binding.layoutBg.ivClose.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.ivDone.setOnClickListener {
            binding.layoutBg.root.visibility = View.GONE
            binding.layoutParentTool.root.visibility = View.VISIBLE
        }
        binding.layoutBg.seleccolor.setOnClickListener {
           // openColorPickerDialog2()
        }
        //currentColorMode = ColorMode.BACKGROUND
        binding.layoutBg.tvColor.setOnClickListener {
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
          //  currentColorMode = ColorMode.BACKGROUND
            binding.layoutBg.seleccolor.setOnClickListener {
           //     openColorPickerDialog2()
            }
            binding.layoutBg.rvColorln.visibility = View.VISIBLE
        }

        binding.layoutBg.tvCustom.setOnClickListener {
            binding.layoutBg.rvGradient.visibility = View.GONE
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.VISIBLE

        }
        binding.layoutBg.tvGradient.setOnClickListener {
            binding.layoutBg.rvColorln.visibility = View.GONE
            binding.layoutBg.rvcolorcustom.visibility = View.GONE
            binding.layoutBg.rvGradient.visibility = View.VISIBLE

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

        }
    }

    private fun setupRecyclerView() {
        val frames = (1..20).map { FrameItem("frames/frame_$it.webp") }
        frameAdapter = FrameAdapter(frames) { drawable ->
            val frameLayout = findViewById<FrameLayout>(R.id.framebg)
            frameLayout.background = drawable
        }
//        binding.layoutFrame.rvFrame.apply {
//            layoutManager = GridLayoutManager(this@HomeCollage, 5)
//            adapter = frameAdapter
//        }
    }

    private fun setRatio() {
//        val ratios = listOf(
//            Triple("1:1", R.drawable.ic_ratio_1_1, 1f),
//            Triple("4:5", R.drawable.ic_ratio_4_5, 4f / 5f),
//            Triple("4:5", R.drawable.ic_ratio_5_4, 5f / 4f),
//            Triple("3:4", R.drawable.ic_ratio_3_4, 3f / 4f),
//            Triple("9:16", R.drawable.ic_ratio_9_16, 9f / 16f)
//        )
//        val adapter = RatioAdapter(ratios) { ratio ->
//            viewModelRatio.setAspectRatio(ratio)
//        }
//        binding.layoutLayout.rvRatio.layoutManager =
//            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
//        binding.layoutLayout.rvRatio.adapter = adapter
//        viewModelRatio.aspectRatio.observe(this)
//        { ratio ->
//            binding.ivBackground.setAspectRatio(ratio)
//            updatePuzzleViewAspectRatio(binding.puzzleView, ratio)
//            updatebgFrame(binding.framebg, ratio)
//        }
//        viewModelRatio.backgroundColor.observe(this) { color ->
//            binding.ivBackground.setBackgroundColor(color)
//        }

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

//    override fun onFilterSelected(photoFilter: PhotoFilter) {
//        mPhotoEditor.setFilterEffect(photoFilter)
//    }
}