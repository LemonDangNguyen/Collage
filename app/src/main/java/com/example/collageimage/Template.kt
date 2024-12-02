package com.example.collageimage

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.view_template.ViewTemplateMain

class Template : BaseActivity() {
    private val binding by lazy { ActivityTemplateBinding.inflate(layoutInflater) }
    private lateinit var viewTemplateMain: ViewTemplateMain
    private var selectedPathIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        val imageId = intent.getIntExtra("imageId", R.drawable.templatee01)

        val layout = layoutInflater.inflate(R.layout.template_main, null)

        if (binding.viewTempalte is ViewGroup) {
            val viewGroup = binding.viewTempalte as ViewGroup
            viewGroup.removeAllViews()
            viewTemplateMain = ViewTemplateMain(this)

            val drawableResId = getDrawableResId(imageId)
            viewTemplateMain.setBackgroundDrawable(drawableResId)

            viewGroup.addView(viewTemplateMain)
        }

        val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImagePath = result.data?.getStringExtra("selected_image_path")
                selectedImagePath?.let {
                    val selectedBitmap = BitmapFactory.decodeFile(it)
                    if (selectedPathIndex != -1) {
                        viewTemplateMain.setSelectedImage(selectedBitmap, selectedPathIndex)
                    }
                }
            }
        }

        // Mở SelectImageTemplate khi người dùng click vào một path
        viewTemplateMain.setOnPathClickListener { pathIndex ->
            selectedPathIndex = pathIndex // save index của path đã chọn
            val intent = Intent(this, SelectImageTemplate::class.java)
            intent.putExtra("selected_path", pathIndex)
            selectImageLauncher.launch(intent)
        }





    }

    private fun getDrawableResId(imageId: Int): Int {
        return when (imageId) {
            R.drawable.templatee01 -> R.drawable.template_01
            R.drawable.templatee05 -> R.drawable.template_05
            R.drawable.templatee22 -> R.drawable.template_22
            R.drawable.templatee25 -> R.drawable.template_25
            R.drawable.templatee28 -> R.drawable.template_28
            R.drawable.templatee30 -> R.drawable.template_30
            else -> R.drawable.template_05
        }
    }

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
            finish()
            super.onBackPressed()
        }

        binding2.btnStay.setOnClickListener {
            dialog2.dismiss()
        }
        dialog2.show()
    }
}
