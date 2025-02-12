package com.example.collageimage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySplashBinding
import com.example.collageimage.extensions.invisible
import com.example.collageimage.extensions.visible
import com.example.collageimage.language.LanguageActivity
import com.example.collageimage.utils.AdsConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base.project.model.LanguageModel
import com.nmh.base.project.helpers.CURRENT_LANGUAGE
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.helpers.IS_UNINSTALL
import com.nmh.base.project.sharepref.DataLocalManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    private var interCallback: AdCallback? = null
    private var dataUninstall = ""

    override fun setUp() {
        if (DataLocalManager.getLanguage(CURRENT_LANGUAGE) == null) {
            DataLocalManager.setLanguage(
                CURRENT_LANGUAGE,
                LanguageModel("English", "flag_language", Locale.ENGLISH, true)
            )
        }

        if (haveNetworkConnection()) {
            CoroutineScope(Dispatchers.IO).launch {
                val remote = async { loadRemoteConfig() }
                if (remote.await())
                    withContext(Dispatchers.Main) {
                        interCallback = object : AdCallback() {
                            override fun onNextAction() {
                                super.onNextAction()
                                startActivity()
                            }
                        }

                        val consentHelper = ConsentHelper.getInstance(this@SplashActivity)
                        if (!consentHelper.canLoadAndShowAds()) consentHelper.reset()

                        consentHelper.obtainConsentAndShow(this@SplashActivity) {
                            if (dataUninstall == IS_UNINSTALL)
                                loadBanner(getString(R.string.banner_splash_uninstall), false)
                            else loadBanner(getString(R.string.banner_all), true)

                            if (dataUninstall == IS_UNINSTALL) AdsConfig.loadNativeKeepUser(this@SplashActivity)
                            else {
                                //load trước native language
                                AdsConfig.loadNativeLanguage(this@SplashActivity)
                                AdsConfig.loadNativeLanguageSelect(this@SplashActivity)
                            }

                            if (dataUninstall == IS_UNINSTALL) {
                                if (AdsConfig.isLoadFullAds() /* thêm điều kiện remote nữa*/) {
                                    Admob.getInstance().loadSplashInterAds2(this@SplashActivity, getString(R.string.inter_splash_uninstall), AdsConfig.getDelayShowInterSplash(), interCallback)
                                } else startActivity()
                            } else Admob.getInstance().loadSplashInterAds2(this@SplashActivity, getString(R.string.inter_splash), AdsConfig.getDelayShowInterSplash(), interCallback)
                        }
                    }
            }
        } else Handler(Looper.getMainLooper()).postDelayed({ startActivity() }, 1500)
    }

    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallback, AdsConfig.getDelayShowInterSplash().toInt())
    }

    private suspend fun loadRemoteConfig(): Boolean {
        return suspendCoroutine { continuation ->

            val configSetting = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.Minimum_Fetch)
                .build()

            val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
                setConfigSettingsAsync(configSetting)
            }

            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults) /*file này lấy trên firebase*/

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //chỗ này sẽ get value key remote (làm và lấy theo trên link excel trên nhóm a Huy gửi, nhân bản ra sheet riêng)
                }
                continuation.resume(true)
            }
        }
    }

    private fun startActivity() {
        DataLocalManager.setBoolean(IS_SHOW_BACK, false)
        startIntent(Intent(this, LanguageActivity::class.java), true)
    }

    private fun loadBanner(strId: String, isRemote: Boolean) {
        if (haveNetworkConnection() && AdsConfig.isLoadFullAds() && isRemote /* -->điều kiện remote ads*/ ) {
            binding.rlBanner.visible()
            val config = BannerPlugin.Config()
            val cbFetchInterval = AdsConfig.cbFetchInterval /*cbFetchInterval lấy theo remote*/
            config.defaultRefreshRateSec = cbFetchInterval
            config.defaultCBFetchIntervalSec = cbFetchInterval
            config.defaultAdUnitId = strId
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(this, binding.banner, binding.shimmer as ViewGroup, config)
        } else binding.rlBanner.invisible()
    }
}