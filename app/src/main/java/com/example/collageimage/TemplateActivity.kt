package com.example.collageimage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.view_template.TemplateModel
import com.example.collageimage.view_template.TemplateViewModel
import com.example.collageimage.view_template.ViewTemplateAdapter

class TemplateActivity : BaseActivity() {
    private val binding by lazy { ActivityTemplateBinding.inflate(layoutInflater) }
    private lateinit var viewTemplateAdapter: ViewTemplateAdapter
    private val templateViewModel: TemplateViewModel by viewModels()
    private var selectedPathIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val imageId = intent.getIntExtra("imageId", -1)
        if (imageId != -1) {
            templateViewModel.loadTemplates()
            val template = templateViewModel.getTemplateById(imageId)
            template?.let {
                setupTemplate(it)
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupTemplate(template: TemplateModel) {
        val layout = layoutInflater.inflate(R.layout.template_main, null)
        if (binding.viewTempalte is ViewGroup) {
            val viewGroup = binding.viewTempalte as ViewGroup
            viewGroup.removeAllViews()

            viewTemplateAdapter = ViewTemplateAdapter(this)
            viewTemplateAdapter.setBackgroundDrawable(template.backgroundImageResId)
            template.stringPaths.forEachIndexed { index, path ->
                viewTemplateAdapter.setPath(index, path)
            }
            viewGroup.addView(viewTemplateAdapter)
            viewTemplateAdapter.setOnPathClickListener { pathIndex ->
                selectedPathIndex = pathIndex
                val intent = Intent(this, SelectImageTemplate::class.java)
                intent.putExtra("selected_path", pathIndex)
                selectImageLauncher.launch(intent)
            }
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImagePath = result.data?.getStringExtra("selected_image_path")
            selectedImagePath?.let {
                val selectedBitmap = BitmapFactory.decodeFile(it)
                templateViewModel.setSelectedImage(selectedBitmap)
                viewTemplateAdapter.setSelectedImage(selectedBitmap, selectedPathIndex)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            val selectedImagePath = data?.getStringExtra("selected_image_path")
            selectedImagePath?.let {
                val selectedBitmap = BitmapFactory.decodeFile(it)
                templateViewModel.setSelectedImage(selectedBitmap)
                viewTemplateAdapter.setSelectedImage(selectedBitmap, selectedPathIndex)
            }
        }
    }
}
