package com.example.collageimage

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySelectImageEditBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.databinding.DialogLoading2Binding

class ActivitySelectImageEdit : BaseActivity<ActivitySelectImageEditBinding>(ActivitySelectImageEditBinding::inflate),
    OnAlbumSelectedListener,
    BottomSheetDialogCamera.OnImagesCapturedListener {

    private val images = mutableListOf<ImageModel>()
    private lateinit var imageAdapter: ImageAdapter
    private var selectedPathIndex: Int = -1
    private var albumName: String? = null

    private lateinit var dialogLoadingBinding: DialogLoading2Binding
    private lateinit var dialogLoading: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        selectedPathIndex = intent.getIntExtra("selected_path", -1)

        dialogLoadingBinding = DialogLoading2Binding.inflate(layoutInflater)
        dialogLoading = Dialog(this).apply {
            setCancelable(false)
            setContentView(dialogLoadingBinding.root)
        }
        loadImages()
        setUpListener()
    }

    override fun setUp() {}

    private fun setUpListener() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnAlbum.setOnClickListener {
            val bottomSheet = SelectAlbumBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun loadImages() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (!albumName.isNullOrEmpty() && albumName != "All Images") {
            selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
            selectionArgs = arrayOf(albumName!!)
        }

        contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            images.clear()

            while (cursor.moveToNext()) {
                val date = cursor.getLong(dateModifiedIndex)
                    .takeIf { it != 0L }
                    ?: cursor.getLong(dateTakenIndex)

                images.add(
                    ImageModel(
                        id = cursor.getLong(idIndex),
                        dateTaken = date,
                        fileName = cursor.getString(nameIndex),
                        filePath = cursor.getString(pathIndex),
                        album = cursor.getString(albumIndex),
                        selected = false,
                        uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(idIndex).toString()
                        ),
                        isCameraItem = false
                    )
                )
            }
            imageAdapter = ImageAdapter(
                this,
                images,
                onItemSelected = { image, isSelected ->
                    if (isSelected) {
                        dialogLoading.show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this, ActivityEditImage::class.java).apply {
                                putExtra("selected_image_path", image.filePath)
                            }
                            startActivity(intent)
                            finish()
                            dialogLoading.dismiss()
                        }, 5000)
                    }
                },
                onCameraClick = {
                    showCameraBottomSheet()
                }
            )

            binding.allImagesRecyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.allImagesRecyclerView.adapter = imageAdapter
            imageAdapter.addCameraItem()
            imageAdapter.notifyDataSetChanged()
        } ?: run {
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCameraBottomSheet() {
        val bottomSheet = BottomSheetDialogCamera.newInstance("ActivitySelectImageEdit")
        bottomSheet.show(supportFragmentManager, "BottomSheetCamera")
    }

    override fun onAlbumSelected(albumName: String) {
        this.albumName = albumName
        images.clear()
        imageAdapter.notifyDataSetChanged()
        loadImages()
    }

    override fun onImagesCaptured(images: ArrayList<ImageModel>) {
        images.forEach { image ->
            val intent = Intent(this, ActivityEditImage::class.java).apply {
                putExtra("selected_image_path", image.filePath)
            }
            startActivity(intent)
            finish()
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
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            super.onBackPressed()
        }

        binding2.btnStay.setOnClickListener {
            dialog2.dismiss()
        }

        dialog2.show()
    }
}
