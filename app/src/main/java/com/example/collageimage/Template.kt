package com.example.collageimage

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.collageimage.databinding.ActivityTemplateBinding

class Template : AppCompatActivity() {
    private val binding by lazy { ActivityTemplateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Nhận imageId từ Intent
        val imageId = intent.getIntExtra("imageId", 0)

        if (imageId != 0) {  // Kiểm tra imageId hợp lệ
            val layoutResId = getLayoutForImage(imageId)  // Lấy layout tương ứng

            layoutResId?.let {
                // Inflate layout tương ứng
                val layout = layoutInflater.inflate(it, null)

                // Kiểm tra xem viewTempalte có phải là ViewGroup không
                if (binding.viewTempalte is ViewGroup) {
                    val viewGroup = binding.viewTempalte as ViewGroup

                    // Xóa tất cả các views hiện tại trong viewTempalte
                    viewGroup.removeAllViews()

                    // Thêm layout mới vào viewTempalte
                    viewGroup.addView(layout)
                }
            }
        }
    }

    // Hàm ánh xạ imageId với layout tương ứng
    private fun getLayoutForImage(imageId: Int): Int? {
        return when (imageId) {
            R.drawable.templatee01 -> R.layout.template_01  // Tương ứng với template_01
          //  R.drawable.templatee02 -> R.layout.template_02  // Tương ứng với template_02
        //    R.drawable.templatee03 -> R.layout.template_03  // Tương ứng với template_03
            R.drawable.templatee05 -> R.layout.template_05
            else -> null  // Nếu không có layout tương ứng, trả về null
        }
    }
}
