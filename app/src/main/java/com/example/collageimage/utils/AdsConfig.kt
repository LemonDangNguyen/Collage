package com.example.collageimage.utils

import android.content.Context
import android.net.ConnectivityManager
import com.example.collageimage.R
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
object AdsConfig {

    var lastTimeShowInter = 0L

    var is_load_native_language = true
    var is_load_native_language_select = true
    var is_load_native_intro1 = true
    var is_load_native_intro2 = true
    var is_load_native_intro3 = true
    var is_load_native_intro4 = true
    var is_load_inter_intro = true
    var is_load_native_permission = true
    var is_load_native_permission_storage = true
    var is_load_native_permission_camera = true
    var is_load_native_permission_notification = true
    var is_load_banner_all = true
    var is_load_native_popup_permission = true
    var is_load_native_home = true
    var is_load_inter_home = true
    var is_load_native_item_template1 = true
    var is_load_native_item_template2 = true
    var is_load_native_item_template3 = true
    var is_load_inter_item_template = true
    var is_load_inter_save = true
    var is_load_inter_back = true
    var is_load_native_exit = true
    var is_load_native_back = true
    var is_load_native_save = true
    var is_load_native_loading = true
    var is_load_native_select_albums = true
    var is_load_native_select_image = true
    var is_load_native_successfully = true
    var is_load_native_setting = true
    var is_load_native_language_setting = true

    var isLoadBannerSPlash = false

    var nativeHome: NativeAd? = null
    var nativeExitApp: NativeAd? = null
    var nativeBackHome: NativeAd? = null
    var nativeAll: NativeAd? = null
    var nativePermission: NativeAd? = null
    var nativeLanguage: NativeAd? = null
    var nativeLanguageSelect: NativeAd? = null
    var nativeKeepUser: NativeAd? = null
    var nativeSurveyUser: NativeAd? = null

    var interBack: InterstitialAd? = null
    var interHome: InterstitialAd? = null
    var interSave: InterstitialAd? = null
    var inter_item_template: InterstitialAd? = null

    var cbFetchInterval = 15
    var interval_show_interstitial = 15
    var is_delay_show_inter_splash = 3L

    fun loadInterBack(context: Context) {
        if (ConsentHelper.getInstance(context).canRequestAds() && interBack == null
            && haveNetworkConnection(context) && is_load_inter_back  ) {
            Admob.getInstance().loadInterAds(context, context.getString(R.string.inter_back),
                object : AdCallback() {
                    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        interBack = interstitialAd
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        interBack = null
                    }
                })
        }
    }

    fun loadInterHome(context: Context) {
        if (ConsentHelper.getInstance(context).canRequestAds() && interHome == null
            && haveNetworkConnection(context) && is_load_inter_home) {
            Admob.getInstance().loadInterAds(context, context.getString(R.string.inter_home),
                object : AdCallback() {
                    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        interHome = interstitialAd
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        interHome = null
                    }
                })
        }
    }

    fun loadInterSave(context: Context) {
        if (ConsentHelper.getInstance(context).canRequestAds() && interSave == null
            && haveNetworkConnection(context) && is_load_inter_save) {
            Admob.getInstance().loadInterAds(context, context.getString(R.string.inter_back),
                object : AdCallback() {
                    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        interSave = interstitialAd
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        interSave = null
                    }
                })
        }
    }
    fun loadInterItemTemplate(context: Context) {
        if (ConsentHelper.getInstance(context).canRequestAds() && inter_item_template == null
            && haveNetworkConnection(context) && is_load_inter_item_template) {
            Admob.getInstance().loadInterAds(context, context.getString(R.string.inter_item_template),
                object : AdCallback() {
                    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        inter_item_template = interstitialAd
                    }
                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        inter_item_template = null
                    }
                })
        }
    }


    fun loadNativeHome(context: Context) {
        if (haveNetworkConnection(context) && ConsentHelper.getInstance(context).canRequestAds()
            && nativeHome == null && is_load_native_home) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_home),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeHome = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativeHome = null
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        nativeHome = null
                        loadNativeHome(context)
                    }
                }
            )
        }
    }

    fun loadNativeBackHome(context: Context) {
        if (haveNetworkConnection(context) && ConsentHelper.getInstance(context).canRequestAds()
            && nativeBackHome == null /* thêm điều kiện remote nữa*/) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_back_home),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeBackHome = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        super.onAdFailedToLoad()
                        nativeBackHome = null
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        nativeBackHome = null
                    }
                }
            )
        }
    }

    fun loadNativeExitApp(context: Context) {
        if (haveNetworkConnection(context)
            && ConsentHelper.getInstance(context).canRequestAds()
            && nativeExitApp == null
            && is_load_native_exit) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_exit),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeExitApp = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        super.onAdFailedToLoad()
                        nativeExitApp = null
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        nativeExitApp = null
                    }
                }
            )
        }
    }

    fun loadNativeAll(context: Context) {
        if (haveNetworkConnection(context) && ConsentHelper.getInstance(context).canRequestAds()
            && nativeAll == null /* thêm điều kiện remote nữa*/) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_all),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeAll = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativeAll = null
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        nativeAll = null
                    }
                }
            )
        }
    }

    fun loadNativePermission(context: Context) {
        if (haveNetworkConnection(context) && nativePermission == null && is_load_native_permission) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_permission),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativePermission = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativePermission = null
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        nativePermission = null
                    }
                }
            )
        }
    }

    fun loadNativeKeepUser(context: Context) {
        if (haveNetworkConnection(context)
            && nativeKeepUser == null /* thêm điều kiện remote nữa*/) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_keep_user),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeKeepUser = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativeKeepUser = null
                    }
                }
            )
        }
    }

    fun loadNativeLanguage(context: Context) {
        if (haveNetworkConnection(context)
            && ConsentHelper.getInstance(context).canRequestAds()
            && nativeLanguage == null
            && AdsConfig.is_load_native_language) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_language),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeLanguage = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativeLanguage = null
                    }
                }
            )
        }
    }

    fun loadNativeLanguageSelect(context: Context) {
        if (haveNetworkConnection(context) && ConsentHelper.getInstance(context).canRequestAds()
            && nativeLanguageSelect == null && AdsConfig.is_load_native_language_select) {
            Admob.getInstance().loadNativeAd(context, context.getString(R.string.native_language_select),
                object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        nativeLanguageSelect = nativeAd
                    }

                    override fun onAdFailedToLoad() {
                        nativeLanguageSelect = null
                    }
                }
            )
        }
    }

    fun haveNetworkConnection(context: Context): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals("WIFI", ignoreCase = true))
                if (ni.isConnected) haveConnectedWifi = true
            if (ni.typeName.equals("MOBILE", ignoreCase = true))
                if (ni.isConnected) haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
    }

    fun checkTimeShowInter(): Boolean =
        System.currentTimeMillis() - lastTimeShowInter > if (isLoadFullAds()) interval_show_interstitial * 1000 else 20000
    /*interval_show_interstitial chỉ app dụng cho bản full ads, bản normal mặc đinh 20s*/

    fun getDelayShowInterSplash() = if(isLoadFullAds()) is_delay_show_inter_splash * 1000L else 3000L

   // fun isLoadFullAds(): Boolean = Admob.getInstance().isLoadFullAds // nomal
    fun isLoadFullAds(): Boolean = true //full ads
}