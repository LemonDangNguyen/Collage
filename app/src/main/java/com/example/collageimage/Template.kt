package com.example.collageimage

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.view_template.ViewTemplateMain
import com.example.collageimage.R

class Template : BaseActivity() {
    private val binding by lazy { ActivityTemplateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        val imageId = intent.getIntExtra("imageId", R.drawable.templatee01)


        val layout = layoutInflater.inflate(R.layout.template_05, null)

        if (binding.viewTempalte is ViewGroup) {
            val viewGroup = binding.viewTempalte as ViewGroup
            viewGroup.removeAllViews()
            val viewTemplateMain = ViewTemplateMain(this)

            val drawableResId = getDrawableResId(imageId)
            viewTemplateMain.setBackgroundDrawable(drawableResId)

            viewGroup.addView(viewTemplateMain)
        }
    }

    private fun getDrawableResId(imageId: Int): Int {
        return when (imageId) {
            R.drawable.templatee01 -> R.drawable.template_01
//            R.drawable.templatee02 -> R.drawable.template_02
//            R.drawable.templatee03 -> R.drawable.template_03
//            R.drawable.templatee04 -> R.drawable.template_04
            R.drawable.templatee05 -> R.drawable.template_05
//            R.drawable.templatee06 -> R.drawable.template_06
//            R.drawable.templatee07 -> R.drawable.template_07
//            R.drawable.templatee08 -> R.drawable.template_08
//            R.drawable.templatee09 -> R.drawable.template_09
//            R.drawable.templatee10 -> R.drawable.template_10
            R.drawable.templatee11 -> R.drawable.template_11
//            R.drawable.templatee12 -> R.drawable.template_12
//            R.drawable.templatee13 -> R.drawable.template_13
//            R.drawable.templatee14 -> R.drawable.template_14
//            R.drawable.templatee15 -> R.drawable.template_15
//            R.drawable.templatee16 -> R.drawable.template_16
//            R.drawable.templatee17 -> R.drawable.template_17
//            R.drawable.templatee18 -> R.drawable.template_18
//            R.drawable.templatee19 -> R.drawable.template_19
//            R.drawable.templatee20 -> R.drawable.template_20
//            R.drawable.templatee21 -> R.drawable.template_21
            R.drawable.templatee22 -> R.drawable.template_22
//            R.drawable.templatee23 -> R.drawable.template_23
//            R.drawable.templatee24 -> R.drawable.template_24
            R.drawable.templatee25 -> R.drawable.template_25
//            R.drawable.templatee26 -> R.drawable.template_26
//            R.drawable.templatee27 -> R.drawable.template_27
            R.drawable.templatee28 -> R.drawable.template_28
//            R.drawable.templatee29 -> R.drawable.template_29
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
