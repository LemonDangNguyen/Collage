package com.photomaker.camerashot.photocollage.instacolor


import android.content.Intent
import android.os.Bundle

import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity

import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.language.LanguageActivity
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.helpers.IS_SHOW_BACK
import com.photomaker.camerashot.photocollage.instacolor.sharepref.DataLocalManager
import com.photomaker.camerashot.photocollage.instacolor.utils.ActionUtils
import com.photomaker.camerashot.photocollage.instacolor.utils.UtilsRate
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivitySettingBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding


class Setting : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnLanguage.setOnClickListener {
            DataLocalManager.setBoolean(IS_SHOW_BACK, true)
            startIntent(Intent(this, LanguageActivity::class.java), false)
        }

        binding.btnRate.setOnClickListener {
            binding.rlNative.gone()
            AppOpenManager.getInstance().disableAppResumeWithActivity(Setting::class.java)
            UtilsRate.showRate(this, false, object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    if (isCheck) binding.btnRate.gone()
                    binding.rlNative.visible()
                }
            })
        }

        binding.btnShare.setOnClickListener {
            binding.rlNative.gone()
            ActionUtils.shareApp(this)
            binding.rlNative.visible()
        }

        binding.btnFeedback.setOnClickListener {
            ActionUtils.sendFeedback(this)
        }
        binding.btnPolicy.setOnClickListener {
            ActionUtils.openPolicy(this)
        }
    }

    override fun setUp() {
        showNative()
    }
    private fun showNative() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_native_language_setting) {
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