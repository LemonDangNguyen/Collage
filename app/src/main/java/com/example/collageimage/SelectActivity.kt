package com.example.collageimage;
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collageimage.databinding.ActivitySelectBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.hypersoft.puzzlelayouts.app.features.media.presentation.images.adapter.recyclerView.AdapterMediaImageDetail

@Suppress("DEPRECATION")
class SelectActivity : BaseActivity() {
    private val binding by lazy { ActivitySelectBinding.inflate(layoutInflater) }
    private val images = mutableListOf<ImageModel>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private lateinit var selectedImages: MutableList<ImageModel>
    private lateinit var imageAdapter: ImageAdapter
    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val mediaStoreMediaImages by lazy { MediaStoreMediaImages(contentResolver) }
    private val repositoryMediaImages by lazy { RepositoryMediaImages(mediaStoreMediaImages) }
    private val useCaseMediaImageDetail by lazy { UseCaseMediaImageDetail(repositoryMediaImages) }
    private val viewModelMediaImageDetail by viewModels<ViewModelMediaImageDetail> { ViewModelMediaImageDetailProvider(useCaseMediaImageDetail) }
    private val itemClick: ((Uri) -> Unit) = { viewModelMediaImageDetail.imageClick(it) }
    private val adapterEnhanceGalleryDetail by lazy { AdapterMediaImageDetail(itemClick) }
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                loadImages()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    private var albumName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        selectedImages = mutableListOf()

       
        selectedImagesAdapter = SelectedImagesAdapter(this, selectedImages) { image ->
            selectedImages.remove(image) // Xóa ảnh khi bấm nút xóa
            selectedImagesAdapter.notifyDataSetChanged() // Cập nhật lại RecyclerView
        }

        imageAdapter = ImageAdapter(this, images) { image, isSelected ->
            if (isSelected) {
                if (!selectedImages.contains(image)) {
                    if (selectedImages.size < 9) {
                        selectedImages.add(image)
                        updateSelectedAdapters()
                    } else {
                        Toast.makeText(this, "You can select up to 9 images only", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                selectedImages.remove(image)
                updateSelectedAdapters()
            }
        }

        // Đảm bảo rằng RecyclerView đã được thiết lập sau khi khởi tạo adapter
        binding.selectedImagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.selectedImagesRecyclerView.adapter = selectedImagesAdapter

        // Kiểm tra và nhận các ảnh đã chọn từ Intent
        val selectedImagesFromIntent = intent.getParcelableArrayListExtra<ImageModel>("SELECTED_IMAGES")
        if (selectedImagesFromIntent != null) {
            selectedImages.addAll(selectedImagesFromIntent)
            updateSelectedAdapters()
        }

        albumName = intent.getStringExtra("ALBUM_NAME")
        setupRecyclerViews()

        // Kiểm tra quyền và tải ảnh
        if (hasStoragePermissions()) {
            loadImages()
        } else {
            permissionLauncher.launch(storagePermissions)
        }

        setUpListener()
        initObservers()
    }


    private fun setUpListener(){
        binding.clearImgList.setOnClickListener {
            selectedImages.clear()
            selectedImagesAdapter.notifyDataSetChanged()
            updateSelectedCount()
            imageAdapter.updateSelection(selectedImages)
        }
        binding.nextSelect.setOnClickListener {
            if(selectedImages.size >= 3){
                val intent = Intent(this, HomeCollage::class.java)
                intent.putParcelableArrayListExtra("SELECTED_IMAGES", ArrayList(selectedImages))
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Please select at least 3 images", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnAlbum.setOnClickListener {
            val intent = Intent(this, SelectAlbum::class.java)
            intent.putParcelableArrayListExtra("SELECTED_IMAGES", ArrayList(selectedImages))
            startActivity(intent)
            finish()
        }
    }
    private fun initObservers() {
        viewModelMediaImageDetail.imagesLiveData.observe(this) {
            adapterEnhanceGalleryDetail.submitList(it)
        }
        viewModelMediaImageDetail.clickedImagesLiveData.observe(this) { clickedImagess ->
            selectedImages = clickedImagess as MutableList<ImageModel>
        }
    }
    private fun setupRecyclerViews() {
        imageAdapter = ImageAdapter(this, images) { image, isSelected ->
            if (isSelected) {
                if (!selectedImages.contains(image)) {
                    if (selectedImages.size < 9) {
                        selectedImages.add(image)
                        updateSelectedAdapters()
                    } else {
                        Toast.makeText(this, "You can select up to 9 images only", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                selectedImages.remove(image)
                updateSelectedAdapters()
            }
        }
        binding.allImagesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SelectActivity, 3)
            adapter = imageAdapter
        }
        selectedImagesAdapter = SelectedImagesAdapter(this, selectedImages) { imageToRemove ->
            selectedImages.remove(imageToRemove)
            selectedImagesAdapter.notifyDataSetChanged()
            imageAdapter.updateSelection(selectedImages)
            selectedImagesAdapter.updateData(selectedImages)
            updateSelectedCount()
        }
        binding.selectedImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SelectActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedImagesAdapter
        }
    }
    private fun loadImages() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val (selection, selectionArgs) = if (albumName == "All Images" || albumName == null) {
            null to null
        } else {
            "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?" to arrayOf(albumName)
        }
        contentResolver.query(uri, projection, selection, selectionArgs, "${MediaStore.Images.Media.DATE_TAKEN} DESC")?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            images.clear()
            while (cursor.moveToNext()) {
                images.add(
                    ImageModel(
                        id = cursor.getLong(idIndex),
                        dateTaken = cursor.getLong(dateIndex),
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
            imageAdapter.notifyDataSetChanged()
        } ?: run {
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
        }
    }
    private fun hasStoragePermissions() = storagePermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelectedAdapters() {
        selectedImagesAdapter.notifyDataSetChanged()
        imageAdapter.updateSelection(selectedImages)
        selectedImagesAdapter.updateData(selectedImages)
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        binding.textViewCountItem.text = selectedImages.size.toString()
    }
     override fun onBackPressed(){

         val binding2 = DialogExitBinding.inflate(layoutInflater)
         val dialog2 = Dialog(this)
         dialog2.setContentView(binding2.root)
         val window = dialog2.window
         window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
         window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
         dialog2.setCanceledOnTouchOutside(false)
         dialog2.setCancelable(false)
         binding2.btnExit.setOnClickListener{
             dialog2.dismiss()
             val intent = Intent(this, MainActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
             finish()
             super.onBackPressed()
         }
         binding2.btnStay.setOnClickListener{
             dialog2.dismiss()
         }
         dialog2.show()
    }
}
