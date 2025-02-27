package com.photomaker.camerashot.photocollage.instacolor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.extensions.invisible
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.language.LanguageActivity
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.model.LanguageModel
import com.photomaker.camerashot.photocollage.instacolor.helpers.CURRENT_LANGUAGE
import com.photomaker.camerashot.photocollage.instacolor.helpers.IS_SHOW_BACK
import com.photomaker.camerashot.photocollage.instacolor.helpers.IS_UNINSTALL
import com.photomaker.camerashot.photocollage.instacolor.sharepref.DataLocalManager
import com.photomaker.camerashot.photocollage.instacolor.BuildConfig
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivitySplashBinding
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

    private val notificationPer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    else arrayOf("")

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Dexter.withContext(this)
                .withPermissions(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        val isPermissionGranted = report.areAllPermissionsGranted()
                        val permissionStatus = if (isPermissionGranted) "allow" else "not_allow"

                        val bundle = Bundle().apply {
                            putString("permission_notification", permissionStatus)
                        }
                        FirebaseAnalytics.getInstance(this@SplashActivity)
                            .logEvent("permission_notification", bundle)
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>,
                        p1: PermissionToken
                    ) {
                        p1.continuePermissionRequest()
                    }

                }).check()
    }

    override fun setUp() {
        if (DataLocalManager.getLanguage(CURRENT_LANGUAGE) == null) {
            DataLocalManager.setLanguage(
                CURRENT_LANGUAGE,
                LanguageModel("English", "flag_language", getString(R.string.english), Locale.ENGLISH, true)
            )
        }

        if (!checkPer(notificationPer)) requestNotificationPermission()
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
                            loadBanner(getString(R.string.banner_all))

                            //load trước native language
                            AdsConfig.loadNativeLanguage(this@SplashActivity)
                            AdsConfig.loadNativeLanguageSelect(this@SplashActivity)

                            Admob.getInstance().loadSplashInterAds2(this@SplashActivity, getString(R.string.inter_splash), AdsConfig.getDelayShowInterSplash(), interCallback)
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

            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

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

    private fun loadBanner(strId: String) {
        if (haveNetworkConnection() && AdsConfig.isLoadFullAds() && AdsConfig.is_load_banner_all) {
            binding.rlBanner.visible()
            val config = BannerPlugin.Config()
            val cbFetchInterval = AdsConfig.cbFetchInterval
            config.defaultRefreshRateSec = cbFetchInterval
            config.defaultCBFetchIntervalSec = cbFetchInterval
            config.defaultAdUnitId = strId
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(this, binding.banner, binding.shimmer as ViewGroup, config)
        } else binding.rlBanner.invisible()
    }
}