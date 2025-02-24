package com.photomaker.camerashot.photocollage.instacolor.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater

import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.DialogLoading2Binding


class DialogLoading(context: Context) : Dialog(context) {

    private var binding: DialogLoading2Binding =
        DialogLoading2Binding.inflate(LayoutInflater.from(context))
    var interCallback: AdCallback? = null
    private var nativeAds: NativeAd? = null

    init {
        setContentView(binding.root)
        setCancelable(false)
        binding.root.layoutParams.width = (NMHApp.w * 100).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadNative()
        val window = window
        window?.setLayout(
            (NMHApp.w * 100).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(android.view.Gravity.CENTER)
    }

    fun loadNative() {
        try {
            if (AdsConfig.haveNetworkConnection(context)
                && ConsentHelper.getInstance(context).canRequestAds()
                && AdsConfig.isLoadFullAds()
                && AdsConfig.is_load_native_loading
            ) {
                binding.layoutNative.visible()
                nativeAds?.let {
                    pushViewAds(it)
                } ?: run {
                    Admob.getInstance().loadNativeAd(
                        context,
                        context.getString(R.string.native_all),
                        object : NativeCallback() {
                            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                                nativeAds = nativeAd
                                pushViewAds(nativeAd)
                                interCallback?.onNextAction()
                            }

                            override fun onAdFailedToLoad() {
                                binding.frAds.removeAllViews()
                                interCallback?.onNextAction()
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                nativeAds = null
                            }
                        }
                    )
                }
            } else {
                binding.layoutNative.gone()
                interCallback?.onNextAction()
            }
        } catch (e: Exception) {
            binding.layoutNative.gone()
            e.printStackTrace()
        }
    }

    private fun pushViewAds(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)

        binding.layoutNative.visible()
        binding.frAds.removeAllViews()
        binding.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
