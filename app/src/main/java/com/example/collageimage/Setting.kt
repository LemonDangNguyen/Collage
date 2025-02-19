package com.example.collageimage


import android.content.Intent
import android.os.Bundle

import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySettingBinding
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.visible
import com.example.collageimage.language.LanguageActivity
import com.example.collageimage.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.sharepref.DataLocalManager
import com.nmh.base.project.utils.ActionUtils
import com.nmh.base.project.utils.UtilsRate
import com.nmh.base_lib.callback.ICallBackCheck


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
            AppOpenManager.getInstance().disableAppResumeWithActivity(Setting::class.java)
            UtilsRate.showRate(this, false, object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    if (isCheck) binding.btnRate.gone()
                }
            })
        }
        binding.btnShare.setOnClickListener {
            ActionUtils.shareApp(this)
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