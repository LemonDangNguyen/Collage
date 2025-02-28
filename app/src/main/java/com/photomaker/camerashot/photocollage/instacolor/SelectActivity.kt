package com.photomaker.camerashot.photocollage.instacolor

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.dialog.DialogLoading
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.showToast
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.cbFetchInterval
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.hypersoft.puzzlelayouts.app.features.media.presentation.images.adapter.recyclerView.AdapterMediaImageDetail
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivitySelectBinding

class SelectActivity : BaseActivity<ActivitySelectBinding>(ActivitySelectBinding::inflate),
    OnAlbumSelectedListener, BottomSheetDialogCamera.OnImagesCapturedListener {

    private val images = mutableListOf<ImageModel>()
    private var selectedImages = mutableListOf<ImageModel>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private var loadingdialog: DialogLoading? = null
    private val mediaStoreMediaImages by lazy { MediaStoreMediaImages(contentResolver) }
    private val repositoryMediaImages by lazy { RepositoryMediaImages(mediaStoreMediaImages) }
    private val useCaseMediaImageDetail by lazy { UseCaseMediaImageDetail(repositoryMediaImages) }
    private val viewModelMediaImageDetail by viewModels<ViewModelMediaImageDetail> {
        ViewModelMediaImageDetailProvider(useCaseMediaImageDetail)
    }
    private val itemClick: ((Uri) -> Unit) = { viewModelMediaImageDetail.imageClick(it) }
    private val adapterEnhanceGalleryDetail by lazy { AdapterMediaImageDetail(itemClick) }

    private var albumName: String? = null

    override fun setUp() {
        binding.selectedImagesRecyclerView.itemAnimator = null

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showInterBack()
            }
        })

        binding.btnBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }
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
        loadBanner()
    }

    private fun showInterBack() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interBack != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_back) {
            Admob.getInstance().showInterAds(this@SelectActivity, AdsConfig.interBack, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interBack = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterBack(this@SelectActivity)
                }
            })
        } else {
            /*nếu không có kịch bản native_back thì finish() luôn*/
            /*ẩn tất cả các ads đang có trên màn hình(banner, native) để show dialog*/
            binding.banner.gone()
            showDialogBack(object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    /*hiện tất cả các ads đang có trên màn hình(banner, native) khi dialog ẩn đi*/
                    binding.banner.visible()
                }
            })
        }
    }

    private fun setUpListener() {
        binding.clearImgList.setOnUnDoubleClickListener {
            selectedImages.clear()
            selectedImagesAdapter.updateData(selectedImages)
            binding.selectedImagesRecyclerView.adapter = null
            binding.selectedImagesRecyclerView.adapter = selectedImagesAdapter
            updateSelectedCount()
            imageAdapter.updateSelection(selectedImages)
        }


        binding.nextSelect.setOnUnDoubleClickListener {
            if (selectedImages.size >= 2) {
                if (loadingdialog == null) loadingdialog = DialogLoading(this@SelectActivity)
                loadingdialog?.let {
                    if (!isFinishing && !isDestroyed) {
                        if (!it.isShowing) {
                            binding.banner.gone()
                            it.show()
                        }
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SelectActivity, HomeCollage::class.java)
                        intent.putParcelableArrayListExtra("SELECTED_IMAGES", ArrayList(selectedImages))
                        startActivity(intent)
                        finish()
                    }, 5000)

                    it.setOnDismissListener {
                        binding.banner.visible()
                    }
                    if (!isFinishing && !isDestroyed && !it.isShowing) {
                        binding.banner.gone()
                        it.show()
                    }
                }

            } else {
                showToast(getString(R.string.select_at_least_2_images), Gravity.CENTER)
            }

        }

        binding.btnAlbum.setOnUnDoubleClickListener {
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


    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.is_load_banner_all) {
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval /*cbFetchInterval lấy theo remote*/
            config.defaultCBFetchIntervalSec = cbFetchInterval

            config.defaultAdUnitId = getString(R.string.banner_all)
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }
}
