package com.example.collageimage.saveImage

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import com.example.collageimage.BaseActivity
import com.example.collageimage.MainActivity
import com.example.collageimage.databinding.ActivitySaveFromEditImageBinding

class SaveFromEditImage : BaseActivity() {

    private val binding by lazy { ActivitySaveFromEditImageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Nhận đường dẫn hình ảnh từ Intent
        val imagePath = intent.getStringExtra("image_path")
        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            binding.ivImage.setImageBitmap(bitmap)
        }

        binding.btGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.btShare.setOnClickListener {
            shareImage(imagePath)
        }
    }

    private fun shareImage(imagePath: String?) {
        val uri = Uri.parse(imagePath)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }
}
