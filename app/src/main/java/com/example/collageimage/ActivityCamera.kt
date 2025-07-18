package com.example.collageimage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.collageimage.databinding.ActivityCameraBinding
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import java.io.File
class ActivityCamera : BaseActivity() {

    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private lateinit var cameraView: CameraView
    private val photoList = mutableListOf<String>()
    private val maxPhotos = 9

    private val cameraListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val photoFile = File(getExternalFilesDir(null), "photo_${photoList.size + 1}.jpg")
            result.toFile(photoFile) { file ->
                file?.let {
                    photoList.add(it.absolutePath)
                    updateThumbnail(it.absolutePath)
                    binding.tvTotalImage.text = "${photoList.size}"
                    binding.tvTotalImage.visibility = View.VISIBLE
                    binding.ivDone.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        cameraView = binding.cameraView
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(cameraListener)
        binding.ivClose.setOnClickListener { finish() }
        binding.ivFlash.setOnClickListener { toggleFlash() }
        binding.ivCapture.setOnClickListener { takePicture() }
        binding.ivDone.setOnClickListener {
            if (photoList.isNotEmpty()) {
                val intent = Intent(this, SelectActivity::class.java)
                val selectedImages = photoList.map { ImageModel(
                    id = System.currentTimeMillis(),
                    dateTaken = System.currentTimeMillis(),
                    fileName = File(it).name,
                    filePath = it,
                    album = "Camera",
                    uri = Uri.parse("file://$it"),
                    isCameraItem = true
                ) }
                intent.putParcelableArrayListExtra("SELECTED_IMAGES", ArrayList(selectedImages))
                startActivity(intent)
            } else {
                Toast.makeText(this, "No images captured", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        cameraView.open()
    }
    override fun onStop() {
        super.onStop()
        cameraView.close()
    }
    private fun takePicture() {
        if (photoList.size >= maxPhotos) {
            Toast.makeText(this, "Đã đạt giới hạn $maxPhotos ảnh", Toast.LENGTH_SHORT).show()
            return
        }
        cameraView.takePictureSnapshot()
    }

    private fun updateThumbnail(photoPath: String) {
        binding.ivThumb.setImageURI(android.net.Uri.parse("file://$photoPath"))
    }

    private fun toggleFlash() {
        when (cameraView.flash) {
            Flash.OFF -> {
                cameraView.flash = Flash.TORCH
                binding.ivFlash.setImageResource(R.drawable.ic_flash)
            }
            Flash.TORCH -> {
                cameraView.flash = Flash.OFF
                binding.ivFlash.setImageResource(R.drawable.ic_no_flash)
            }
            else -> {
                cameraView.flash = Flash.OFF
                binding.ivFlash.setImageResource(R.drawable.ic_no_flash)
            }
        }
    }
}

