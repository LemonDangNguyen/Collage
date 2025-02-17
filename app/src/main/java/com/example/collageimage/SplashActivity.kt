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
                    AdsConfig.is_load_native_language = remoteConfig.getBoolean("is_load_native_language")
                    AdsConfig.is_load_native_language_select = remoteConfig.getBoolean("is_load_native_language_select")
                    AdsConfig.is_load_native_intro1 = remoteConfig.getBoolean("is_load_native_intro1")
                    AdsConfig.is_load_native_intro2 = remoteConfig.getBoolean("is_load_native_intro2")
                    AdsConfig.is_load_native_intro3 = remoteConfig.getBoolean("is_load_native_intro3")
                    AdsConfig.is_load_native_intro4 = remoteConfig.getBoolean("is_load_native_intro4")
                    AdsConfig.is_load_inter_intro = remoteConfig.getBoolean("is_load_inter_intro")
                    AdsConfig.is_load_native_permission = remoteConfig.getBoolean("is_load_native_permission")
                    AdsConfig.is_load_native_permission_storage = remoteConfig.getBoolean("is_load_native_permission_storage")
                    AdsConfig.is_load_native_permission_camera = remoteConfig.getBoolean("is_load_native_permission_camera")
                    AdsConfig.is_load_native_permission_notification = remoteConfig.getBoolean("is_load_native_permission_notification")
                    AdsConfig.is_load_banner_all = remoteConfig.getBoolean("is_load_banner_all")
                    AdsConfig.is_load_native_popup_permission = remoteConfig.getBoolean("is_load_native_popup_permission")
                    AdsConfig.is_load_native_home = remoteConfig.getBoolean("is_load_native_home")
                    AdsConfig.is_load_inter_home = remoteConfig.getBoolean("is_load_inter_home")
                    AdsConfig.is_load_native_item_template1 = remoteConfig.getBoolean("is_load_native_item_template1")
                    AdsConfig.is_load_native_item_template2 = remoteConfig.getBoolean("is_load_native_item_template2")
                    AdsConfig.is_load_native_item_template3 = remoteConfig.getBoolean("is_load_native_item_template3")
                    AdsConfig.is_load_inter_item_template = remoteConfig.getBoolean("is_load_inter_item_template")
                    AdsConfig.is_load_inter_save = remoteConfig.getBoolean("is_load_inter_save")
                    AdsConfig.is_load_inter_back = remoteConfig.getBoolean("is_load_inter_back")
                    AdsConfig.is_load_native_exit = remoteConfig.getBoolean("is_load_native_exit")
                    AdsConfig.is_load_native_back = remoteConfig.getBoolean("is_load_native_back")
                    AdsConfig.is_load_native_save = remoteConfig.getBoolean("is_load_native_save")
                    AdsConfig.is_load_native_loading = remoteConfig.getBoolean("is_load_native_loading")
                    AdsConfig.is_load_native_select_albums = remoteConfig.getBoolean("is_load_native_select_albums")
                    AdsConfig.is_load_native_select_image = remoteConfig.getBoolean("is_load_native_select_image")
                    AdsConfig.is_load_native_successfully = remoteConfig.getBoolean("is_load_native_successfully")
                    AdsConfig.is_load_native_setting = remoteConfig.getBoolean("is_load_native_setting")
                    AdsConfig.is_load_native_language_setting = remoteConfig.getBoolean("is_load_native_language_setting")
                    AdsConfig.interval_show_interstitial = remoteConfig.getLong("interval_show_interstitial").toInt()
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