package com.example.collageimage

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collageimage.MainActivity
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySelectBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.databinding.DialogLoading2Binding
import com.example.collageimage.databinding.DialogLoadingBinding
import com.example.collageimage.extensions.showToast
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.hypersoft.puzzlelayouts.app.features.media.presentation.images.adapter.recyclerView.AdapterMediaImageDetail

class SelectActivity : BaseActivity<ActivitySelectBinding>(ActivitySelectBinding::inflate),
    OnAlbumSelectedListener, BottomSheetDialogCamera.OnImagesCapturedListener {

    private val images = mutableListOf<ImageModel>()
    private var selectedImages = mutableListOf<ImageModel>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val mediaStoreMediaImages by lazy { MediaStoreMediaImages(contentResolver) }
    private val repositoryMediaImages by lazy { RepositoryMediaImages(mediaStoreMediaImages) }
    private val useCaseMediaImageDetail by lazy { UseCaseMediaImageDetail(repositoryMediaImages) }
    private val viewModelMediaImageDetail by viewModels<ViewModelMediaImageDetail> {
        ViewModelMediaImageDetailProvider(
            useCaseMediaImageDetail
        )
    }
    private val itemClick: ((Uri) -> Unit) = { viewModelMediaImageDetail.imageClick(it) }
    private val adapterEnhanceGalleryDetail by lazy { AdapterMediaImageDetail(itemClick) }

    private var albumName: String? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Khởi tạo SelectedImagesAdapter
        selectedImagesAdapter = SelectedImagesAdapter(this, selectedImages) { imageToRemove ->
            selectedImages.remove(imageToRemove)
            selectedImagesAdapter.notifyDataSetChanged()
            imageAdapter.updateSelection(selectedImages)
            selectedImagesAdapter.updateData(selectedImages)
            updateSelectedCount()
        }

        val selectedImagesFromIntent =
            intent.getParcelableArrayListExtra<ImageModel>("IMG_FROM_CAM")

        if (selectedImagesFromIntent != null) {
            selectedImages.addAll(selectedImagesFromIntent)
            selectedImagesAdapter.notifyDataSetChanged()
            updateSelectedAdapters()
            updateSelectedCount()
        }

        setupRecyclerViews()
        loadImages()

        setUpListener()
        initObservers()
    }

    override fun setUp() {

    }

    private fun setUpListener() {
        binding.clearImgList.setOnClickListener {
            selectedImages.clear()
            selectedImagesAdapter.notifyDataSetChanged()
            updateSelectedCount()
            imageAdapter.updateSelection(selectedImages)
        }
        val dialogLoadingBinding = DialogLoading2Binding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            setCancelable(false)
            setContentView(dialogLoadingBinding.root)
        }

        binding.nextSelect.setOnClickListener {
            if (selectedImages.size >= 3) {
                dialog.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    val intent = Intent(this, HomeCollage::class.java)
                    intent.putParcelableArrayListExtra("SELECTED_IMAGES", ArrayList(selectedImages))
                    startActivity(intent)
                    finish()
                }, 5000)
            } else {
                showToast(getString(R.string.select_at_least_3_images), Gravity.CENTER);


            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnAlbum.setOnClickListener {
            val bottomSheet = SelectAlbumBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun initObservers() {
        viewModelMediaImageDetail.imagesLiveData.observe(this) {
            adapterEnhanceGalleryDetail.submitList(it)
        }

        viewModelMediaImageDetail.clickedImagesLiveData.observe(this) { clickedImages ->
            selectedImages = clickedImages as MutableList<ImageModel>
        }
    }

    private fun setupRecyclerViews() {
        imageAdapter = ImageAdapter(
            this,
            images,
            onItemSelected = { image, isSelected ->
                if (isSelected) {
                    if (!selectedImages.contains(image)) {
                        if (selectedImages.size < 9) {
                            selectedImages.add(image)
                            updateSelectedAdapters()
                        } else {
                            Toast.makeText(this, "Maximum is 9 images only", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    selectedImages.remove(image)
                    updateSelectedAdapters()
                }
            },
            onCameraClick = { showCameraBottomSheet() }
        )

        binding.allImagesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SelectActivity, 3)
            adapter = imageAdapter
        }

        binding.selectedImagesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@SelectActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
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

        val (selection, selectionArgs) = if (albumName == "All Images" || albumName == null) {
            null to null
        } else {
            "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?" to arrayOf(albumName)
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
            val dateModifiedIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val albumIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            images.clear()
            while (cursor.moveToNext()) {
                val date = if (!cursor.isNull(dateModifiedIndex)) {
                    cursor.getLong(dateModifiedIndex)
                } else {
                    cursor.getLong(dateTakenIndex)
                }

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
            imageAdapter.addCameraItem()
            imageAdapter.notifyDataSetChanged()
        } ?: run {
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelectedAdapters() {
        if (::imageAdapter.isInitialized) {
            imageAdapter.updateSelection(selectedImages)
        }
        selectedImagesAdapter.updateData(selectedImages)
        selectedImagesAdapter.notifyDataSetChanged()
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        binding.textViewCountItem.text = selectedImages.size.toString()
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

    override fun onAlbumSelected(albumName: String) {
        this.albumName = albumName
        images.clear()
        imageAdapter.notifyDataSetChanged()
        loadImages()
    }

    private fun showCameraBottomSheet() {
        val bottomSheet = BottomSheetDialogCamera.newInstance(getCurrentActivityName())
        bottomSheet.setTargetFragment(null, 0)
        bottomSheet.show(supportFragmentManager, "BottomSheetCamera")
        supportFragmentManager.setFragmentResultListener(
            "cameraRequestKey",
            this
        ) { requestKey, bundle ->
            val selectedImagesFromCamera = bundle.getParcelableArrayList<ImageModel>("IMG_FROM_CAM")
            selectedImagesFromCamera?.let {
                selectedImages.addAll(it)
                selectedImagesAdapter.notifyDataSetChanged()
                imageAdapter.updateSelection(selectedImages)
                updateSelectedAdapters()
                updateSelectedCount()
            }
        }
    }

    private fun getCurrentActivityName(): String {
        return when (this::class.java.simpleName) {
            "SelectActivity" -> "SelectActivity"
            "ActivitySelectImageEdit" -> "ActivitySelectImageEdit"
            else -> "Unknown"
        }
    }


    override fun onImagesCaptured(images: ArrayList<ImageModel>) {
        selectedImages.addAll(images)
        selectedImagesAdapter.notifyDataSetChanged()
        imageAdapter.updateSelection(selectedImages)
        updateSelectedAdapters()
        updateSelectedCount()
    }
}
