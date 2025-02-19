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
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySelectImageEditBinding
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.databinding.DialogLoading2Binding
import com.example.collageimage.dialog.DialogLoading
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.setOnUnDoubleClickListener
import com.example.collageimage.extensions.visible
import com.example.collageimage.utils.AdsConfig
import com.example.collageimage.utils.AdsConfig.cbFetchInterval
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck

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

        loadBanner()

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
           // binding.rlNative.gone()
            showDialogBack(object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    /*hiện tất cả các ads đang có trên màn hình(banner, native) khi dialog ẩn đi*/
                //    binding.rlNative.visible()
                }
            })
        }
    }
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
                        //loading dialog hể
                        loadingdialog = DialogLoading(this).apply {
                            interCallback = object : AdCallback() {
                                override fun onNextAction() {
                                    super.onNextAction()
                                    val intent = Intent(this@ActivitySelectImageEdit, ActivityEditImage::class.java)
                                    intent.putExtra("selected_image_path", image.filePath)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
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

    override fun onImagesCaptured(images: ArrayList<ImageModel>) {
        images.forEach { image ->
            val intent = Intent(this, ActivityEditImage::class.java).apply {
                putExtra("selected_image_path", image.filePath)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()) {
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval /*cbFetchInterval lấy theo remote*/
            config.defaultCBFetchIntervalSec = cbFetchInterval

            if (true /*thêm biến check remote, thường là switch_banner_collapse*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.CollapsibleBottom
            } else if (true /*thêm biến check remote, thường là banner_all*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            } else {
                binding.banner.gone()
                return
            }
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }
}
