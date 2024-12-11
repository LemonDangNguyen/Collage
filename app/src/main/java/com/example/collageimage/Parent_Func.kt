package com.example.collageimage

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.collageimage.databinding.LayoutParentFuncBinding

class Parent_Func : AppCompatActivity() {

    // Kết nối layout với ViewBinding
    private val binding by lazy { LayoutParentFuncBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gán layout cho Activity
        setContentView(binding.root)

        // Xử lý sự kiện bấm vào các item
        binding.llChangeLayout.setOnClickListener { openLayout("layout_tool") }
        binding.llChangeBG.setOnClickListener { openLayout("layout_bg") }
        binding.llChangeFrame.setOnClickListener { openLayout("layout_frame") }
        binding.llChangeText.setOnClickListener { openLayout("layout_add_text") }
        binding.llChangeFilter.setOnClickListener { openLayout("layout_filter_and_adjust") }
        binding.llChangeSticker.setOnClickListener { openLayout("layout_stickers") }
        binding.changeDraw.setOnClickListener { openLayout("layout_drawing") }
        //binding.addImage.setOnClickListener { openLayout("layout_add_image") }
    }

    // Hàm mở layout tương ứng với tên layout
    private fun openLayout(layoutName: String) {
        when (layoutName) {
            "layout_tool" -> setContentView(R.layout.layout_tool)
            "layout_bg" -> setContentView(R.layout.layout_bg)
            "layout_frame" -> setContentView(R.layout.layout_frame)
            "layout_add_text" -> setContentView(R.layout.layout_add_text)
            "layout_filter_and_adjust" -> setContentView(R.layout.layout_filter_and_adjust)
            "layout_stickers" -> setContentView(R.layout.layout_stickers)
            "layout_drawing" -> setContentView(R.layout.layout_drawing)
           // "layout_add_image" -> setContentView(R.layout.layout_add_image)
        }
    }
}
