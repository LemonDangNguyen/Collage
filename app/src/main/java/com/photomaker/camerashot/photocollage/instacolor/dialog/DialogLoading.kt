package com.photomaker.camerashot.photocollage.instacolor.dialog

import android.content.Context
import android.os.Bundle
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.base.BaseDialog
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.DialogLoading2Binding
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig

class DialogLoading(context: Context) : BaseDialog<DialogLoading2Binding>(DialogLoading2Binding::inflate, context) {

    var interCallback: AdCallback? = null
    private var nativeAds: NativeAd? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadNative()
    }

    fun loadNative() {
        try {
            if (AdsConfig.haveNetworkConnection(context)
                && ConsentHelper.getInstance(context).canRequestAds()
                && AdsConfig.isLoadFullAds()
                && AdsConfig.is_load_native_loading
            ) {
                mBinding.layoutNative.visible()
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
                                mBinding.frAds.removeAllViews()
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
                mBinding.layoutNative.gone()
                interCallback?.onNextAction()
            }
        } catch (e: Exception) {
            mBinding.layoutNative.gone()
            e.printStackTrace()
        }
    }

    private fun pushViewAds(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)

        mBinding.frAds.removeAllViews()
        mBinding.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
