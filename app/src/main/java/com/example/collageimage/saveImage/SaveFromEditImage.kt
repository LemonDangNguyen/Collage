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
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.example.collageimage.ActivitySelectImageEdit
import com.example.collageimage.BuildConfig
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.SelectActivity
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySaveFromEditImageBinding
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.setOnUnDoubleClickListener
import com.example.collageimage.extensions.visible
import com.example.collageimage.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck

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

        val extraText = intent.getStringExtra("extra_text")


        binding.btGoHome.text = if (extraText == "TemplateActivity") {
            "Try Other Template"
        } else {
            "Edit Other Image"
        }

        binding.btGoHome.setOnClickListener {
            val intent = when (extraText) {
                "HomeCollage" -> Intent(this, SelectActivity::class.java)
                "TemplateActivity" -> Intent(this, MainActivity::class.java).apply {
                    putExtra("navigate_to_template", true)
                }
                "ActivityEditImage" -> Intent(this, ActivitySelectImageEdit::class.java)
                else -> null
            }

            intent?.let {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(it)
                finish()
            }
        }

        binding.btShare.setOnClickListener {
            imagePath?.let { shareImageFromPictures(this, it) }
        }

    }

    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showInterBack()
            }
        })
        binding.btnHome.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }
        showNative()
    }
    private fun showInterBack() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interBack != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_back) {
            Admob.getInstance().showInterAds(this@SaveFromEditImage, AdsConfig.interBack, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    finish()
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interBack = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterBack(this@SaveFromEditImage)
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


    private fun showNative() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_native_save) {
            binding.rlNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_all),
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
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (!AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)

        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
