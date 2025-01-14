package com.example.collageimage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.collageimage.databinding.ActivityTemplateBinding
import com.example.collageimage.view_template.TemplateModel
import com.example.collageimage.view_template.TemplateViewModel
import com.example.collageimage.view_template.ViewTemplateAdapter
import kotlinx.coroutines.launch

class TemplateActivity : BaseActivity() {
    private lateinit var binding: ActivityTemplateBinding
    private lateinit var viewTemplateAdapter: ViewTemplateAdapter
    private val templateViewModel: TemplateViewModel by viewModels()
    private var selectedPathIndex: Int = -1
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImagePath = result.data?.getStringExtra("selected_image_path")
            selectedImagePath?.let {
                val selectedBitmap = BitmapFactory.decodeFile(it)
                templateViewModel.setSelectedImage(selectedBitmap)
                if (selectedPathIndex != -1) {
                    viewTemplateAdapter.setSelectedImage(selectedBitmap, selectedPathIndex)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewTemplateAdapter = binding.viewTempalte

        val imageId = intent.getIntExtra("imageId", -1)
        if (imageId != -1) {
            templateViewModel.loadTemplates()
            lifecycleScope.launch {
                val template = templateViewModel.getTemplateById(imageId)
                template?.let {
                    setupTemplate(it)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnChangeImage.setOnClickListener {
            val intent = Intent(this, SelectImageTemplate::class.java)
            selectImageLauncher.launch(intent)
        }
    }

    private fun setupTemplate(template: TemplateModel) {
        viewTemplateAdapter.setBackgroundDrawable(template.backgroundImageResId)
        template.stringPaths.forEachIndexed { index, path ->
            viewTemplateAdapter.setPath(index, path)
        }
        viewTemplateAdapter.setOnPathClickListener { pathIndex ->
            selectedPathIndex = pathIndex
            val intent = Intent(this, SelectImageTemplate::class.java).apply {
                putExtra("selected_path", pathIndex)
            }
            selectImageLauncher.launch(intent)
        }
    }
}

