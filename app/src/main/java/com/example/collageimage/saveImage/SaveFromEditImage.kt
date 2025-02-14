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
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.DialogSaveBeforeClosingBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.visible
import com.example.collageimage.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper

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
        showNative()
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


    private fun showNative() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() /*thêm điều kiện remote*/) {
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
