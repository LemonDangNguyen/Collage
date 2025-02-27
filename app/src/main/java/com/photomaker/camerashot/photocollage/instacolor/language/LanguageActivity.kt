package com.photomaker.camerashot.photocollage.instacolor.language
import android.view.Gravity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.base.UiState
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.invisible
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.showToast
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.onboarding.OnBoardingActivity
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackItem
import com.photomaker.camerashot.photocollage.instacolor.helpers.CURRENT_LANGUAGE
import com.photomaker.camerashot.photocollage.instacolor.helpers.IS_SHOW_BACK
import com.photomaker.camerashot.photocollage.instacolor.model.LanguageModel
import com.photomaker.camerashot.photocollage.instacolor.sharepref.DataLocalManager
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivityLanguageAcyivityBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeTopFullAdsBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.NativeBotHorizontalMediaLeftLoadingBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.NativeButtonBotLoadingBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.NativeTopFullAsdLoadingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageAcyivityBinding>(ActivityLanguageAcyivityBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    @Inject
    lateinit var langAdapter: LanguageAdapter
    private val viewModel: LanguageActivityViewModel by viewModels()
    private var lang: LanguageModel? = null

    override fun setUp() {
        onBackPressedDispatcher.addCallback(this@LanguageActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        if (!AdsConfig.isLoadFullAds() || DataLocalManager.getBoolean(IS_SHOW_BACK, false)) {
            if (DataLocalManager.getBoolean(IS_SHOW_BACK, false)) {
                val vLoading = NativeBotHorizontalMediaLeftLoadingBinding.inflate(layoutInflater)
                binding.frAds.removeAllViews()
                binding.frAds.addView(vLoading.root)

                DataLocalManager.getLanguage(CURRENT_LANGUAGE)?.let { lang = it }

                binding.ivBack.visible()
                binding.ivTick.visible()
                showNativeLanguageSetting()
            } else {
                val vLoading = NativeButtonBotLoadingBinding.inflate(layoutInflater)
                binding.frAds.removeAllViews()
                binding.frAds.addView(vLoading.root)

                binding.ivBack.invisible()
                binding.ivTick.invisible()

                showNativeLanguage()
            }
        } else {
            val vLoadingAds = NativeTopFullAsdLoadingBinding.inflate(layoutInflater)
            binding.frAds.removeAllViews()
            binding.frAds.addView(vLoadingAds.root)

            binding.ivBack.invisible()
            binding.ivTick.invisible()

            showNativeLanguage()
        }

        langAdapter.callBack = object : ICallBackItem {
            override fun callBack(ob: Any?, position: Int) {
                if (!binding.ivTick.isVisible) showNativeLanguageSelect()
                lang = ob as LanguageModel
            }
        }

        binding.rcvLanguage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvLanguage.adapter = langAdapter

        binding.ivBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivTick.setOnUnDoubleClickListener {
            lang?.let {
                DataLocalManager.setLanguage(CURRENT_LANGUAGE, it)
                startIntent(OnBoardingActivity::class.java.name, true)
                finishAffinity()
            } ?: run { showToast(getString(R.string.you_need_pick_a_language), Gravity.CENTER) }
        }

        lifecycleScope.launch {
            viewModel.getAllLanguage()
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateLanguage.collect {
                    when (it) {
                        is UiState.Loading -> {}
                        is UiState.Error -> {}
                        is UiState.Success -> {
                            if (it.data.isNotEmpty()) langAdapter.setData(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun showNativeLanguageSetting() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_native_language_setting) {
            binding.layoutNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAdsSetting(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_all), object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        pushViewAdsSetting(nativeAd)
                    }

                    override fun onAdFailedToLoad() {
                        super.onAdFailedToLoad()
                        binding.frAds.removeAllViews()
                    }
                })
            }
        } else binding.layoutNative.gone()
    }

    private fun pushViewAdsSetting(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)

        binding.frAds.removeAllViews()
        binding.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }

    private fun showNativeLanguage() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_native_language) {
            binding.layoutNative.visible()
            AdsConfig.nativeLanguage?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_language),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAds(nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            binding.frAds.removeAllViews()
                        }
                    }
                )
            }
        } else binding.layoutNative.gone()
    }

    private fun showNativeLanguageSelect() {
        binding.ivTick.visible()
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds() && AdsConfig.is_load_native_language_select) {
            binding.layoutNative.visible()
            AdsConfig.nativeLanguageSelect?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this, getString(R.string.native_language_select),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAds(nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            binding.frAds.removeAllViews()
                        }
                    }
                )
            }
        } else binding.layoutNative.gone()
    }

    private fun pushViewAds(nativeAd: NativeAd) {
        val adView: ViewBinding
        if (!AdsConfig.isLoadFullAds()) adView = AdsNativeBotBinding.inflate(layoutInflater)
        else adView = AdsNativeTopFullAdsBinding.inflate(layoutInflater)

        binding.frAds.removeAllViews()
        binding.frAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root as NativeAdView)
    }
}
