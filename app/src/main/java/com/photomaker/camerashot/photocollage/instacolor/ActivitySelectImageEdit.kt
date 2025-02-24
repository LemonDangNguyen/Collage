package com.photomaker.camerashot.photocollage.instacolor

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.photomaker.camerashot.photocollage.instacolor.BottomSheetDialogCamera
import com.photomaker.camerashot.photocollage.instacolor.ImageAdapter
import com.photomaker.camerashot.photocollage.instacolor.ImageModel
import com.photomaker.camerashot.photocollage.instacolor.OnAlbumSelectedListener
import com.photomaker.camerashot.photocollage.instacolor.SelectAlbumBottomSheet
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity

import com.photomaker.camerashot.photocollage.instacolor.dialog.DialogLoading
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.ActivityEditImage
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivitySelectImageEditBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.DialogLoading2Binding

class ActivitySelectImageEdit : BaseActivity<ActivitySelectImageEditBinding>(ActivitySelectImageEditBinding::inflate),
    OnAlbumSelectedListener,
    BottomSheetDialogCamera.OnImagesCapturedListener {

    private val images = mutableListOf<ImageModel>()
    private lateinit var imageAdapter: ImageAdapter
    private var selectedPathIndex: Int = -1
    private var albumName: String? = null
    private lateinit var loadingdialog: DialogLoading
    private lateinit var dialogLoadingBinding: DialogLoading2Binding
    private lateinit var dialogLoading: Dialog


    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showInterBack()
            }
        })

        showNativeADS()

        binding.btnBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }

        selectedPathIndex = intent.getIntExtra("selected_path", -1)

        dialogLoadingBinding = DialogLoading2Binding.inflate(layoutInflater)
        dialogLoading = Dialog(this).apply {
            setCancelable(false)
            setContentView(dialogLoadingBinding.root)
        }
        loadImages()
        setUpListener()
    }
    private fun showInterBack() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interBack != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_back) {
            Admob.getInstance().showInterAds(this@ActivitySelectImageEdit, AdsConfig.interBack, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interBack = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterBack(this@ActivitySelectImageEdit)
                }
            })
        } else {
            /*nếu không có kịch bản native_back thì finish() luôn*/
            /*ẩn tất cả các ads đang có trên màn hình(banner, native) để show dialog*/
            binding.rlNative.gone()
            showDialogBack(object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    /*hiện tất cả các ads đang có trên màn hình(banner, native) khi dialog ẩn đi*/
                    binding.rlNative.visible()
                }
            })
        }
    }
    private fun setUpListener() {
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
                        loadingdialog = DialogLoading(this).apply {
                            interCallback = object : AdCallback() {
                                override fun onNextAction() {
                                    super.onNextAction()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        val intent = Intent(this@ActivitySelectImageEdit, ActivityEditImage::class.java)
                                        intent.putExtra("selected_image_path", image.filePath)
                                        startActivity(intent)
                                        finish()
                                    }, 3000)
                                }
                            }
                        }
                        binding.rlNative.gone()
                        loadingdialog.show()
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

    @SuppressLint("SuspiciousIndentation")
    override fun onImagesCaptured(images: ArrayList<ImageModel>) {
        images.forEach { image ->
            val intent = Intent(this, ActivityEditImage::class.java)
                intent.putExtra("selected_image_path", image.filePath)
                startActivity(intent)
                finish()
        }
    }

    private fun showNativeADS() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()&& AdsConfig.is_load_native_select_image) {
            binding.rlNative.visible()
            AdsConfig.nativeLanguageSelect?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_language_select),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAds(nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            binding.frNativeAds.removeAllViews()
                        }
                    }
                )
            }
        } else binding.rlNative.gone()
    }

    private fun pushViewAds(nativeAd: NativeAd) {
        val adView: ViewBinding
        if (!AdsConfig.isLoadFullAds()) adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)
        else adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)
        binding.rlNative.visible()
        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root as NativeAdView)
    }

}
