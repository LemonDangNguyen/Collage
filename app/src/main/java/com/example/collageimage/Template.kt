package com.example.collageimage

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.databinding.DialogExitBinding

class Template : BaseActivity() {
    private val binding by lazy { ActivityTemplateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        // Nhận imageId từ Intent
        val imageId = intent.getIntExtra("imageId", 0)

        if (imageId != 0) {
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
            // R.drawable.templatee02 -> R.layout.template_02  // Tương ứng với template_02
            // R.drawable.templatee03 -> R.layout.template_03  // Tương ứng với template_03
            R.drawable.templatee05 -> R.layout.template_05
            else -> null  // Nếu không có layout tương ứng, trả về null
        }
    }

    override fun onBackPressed(){

        val binding2 = DialogExitBinding.inflate(layoutInflater)
        val dialog2 = Dialog(this)
        dialog2.setContentView(binding2.root)
        val window = dialog2.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.setCanceledOnTouchOutside(false)
        dialog2.setCancelable(false)
        binding2.btnExit.setOnClickListener{
            dialog2.dismiss()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            super.onBackPressed()
        }
        binding2.btnStay.setOnClickListener{
            dialog2.dismiss()
        }
        dialog2.show()
    }
}
