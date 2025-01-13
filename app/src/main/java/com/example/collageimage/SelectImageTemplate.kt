package com.example.collageimage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.collageimage.databinding.ActivitySelectImageTemplateBinding

class SelectImageTemplate : BaseActivity(), BottomSheetDialogCamera.OnImagesCapturedListener  {
    private val binding by lazy { ActivitySelectImageTemplateBinding.inflate(layoutInflater) }
    private val images = mutableListOf<ImageModel>()
    private lateinit var imageAdapter: ImageAdapter
    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var selectedPathIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        selectedPathIndex = intent.getIntExtra("selected_path", -1)

        if (hasStoragePermissions()) {
            loadImages()
        } else {
            permissionLauncher.launch(storagePermissions)
        }

        setUpListener()
    }

    private fun setUpListener() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadImages() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED, // Thêm DATE_MODIFIED vào projection
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        contentResolver.query(uri, projection, null, null, "${MediaStore.Images.Media.DATE_MODIFIED} DESC")?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED) // Truy xuất DATE_MODIFIED
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            images.clear()

            while (cursor.moveToNext()) {
                val date = cursor.getLong(dateModifiedIndex) ?: cursor.getLong(dateTakenIndex)

                images.add(
                    ImageModel(
                        id = cursor.getLong(idIndex),
                        dateTaken = date, // Sử dụng DATE_MODIFIED nếu có, nếu không thì DATE_TAKEN
                        fileName = cursor.getString(nameIndex),
                        filePath = cursor.getString(pathIndex),
                        album = cursor.getString(albumIndex),
                        selected = false,
                        uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(idIndex).toString()
                        )
                    )
                )
            }
            imageAdapter = ImageAdapter(this, images, onItemSelected = { image, isSelected ->
                if (isSelected) {
                    val intent = Intent(this, TemplateActivity::class.java).apply {
                        putExtra("selected_image_path", image.filePath)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }, onCameraClick = { showCameraBottomSheet() })

            binding.allImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.allImagesRecyclerView.adapter = imageAdapter
        } ?: run {
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasStoragePermissions() = storagePermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                loadImages()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    private fun showCameraBottomSheet() {
        val bottomSheet = BottomSheetDialogCamera.newInstance("ActivitySelectImageEdit")
        bottomSheet.show(supportFragmentManager, "BottomSheetCamera")
    }

    override fun onImagesCaptured(images: ArrayList<ImageModel>) {
        TODO("Not yet implemented")
    }
}
