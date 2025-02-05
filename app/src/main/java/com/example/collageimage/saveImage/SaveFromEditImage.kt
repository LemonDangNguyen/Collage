package com.example.collageimage.saveImage

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.collageimage.BuildConfig
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySaveFromEditImageBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.nlbn.ads.util.AppOpenManager

class SaveFromEditImage : BaseActivity<ActivitySaveFromEditImageBinding>(ActivitySaveFromEditImageBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("image_path")
        imagePath?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = Uri.parse(it)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.ivImage.setImageBitmap(bitmap)
                }
            } else {
                val bitmap = BitmapFactory.decodeFile(it)
                binding.ivImage.setImageBitmap(bitmap)
            }
        }

        binding.btGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btShare.setOnClickListener {
            imagePath?.let { shareImageFromPictures(this, it) }
        }
    }

    override fun setUp() {

    }
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun shareImageFromPictures(context: Context, imagePath: String) {
        try {
            val uri = Uri.parse(imagePath)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                putExtra(Intent.EXTRA_STREAM, uri)
//                var shareMessage = context.getString(R.string.app_name)
//                shareMessage += "\nhttps://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
//                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            AppOpenManager.getInstance().disableAppResumeWithActivity(context::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
