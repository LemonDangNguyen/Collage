package com.example.collageimage

import android.annotation.SuppressLint
import android.content.Context
import com.example.collageimage.sharepref.DataLocalManager
import com.google.firebase.FirebaseApp
import com.nlbn.ads.util.Adjust
import com.nlbn.ads.util.AdsApplication

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NMHApp : AdsApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context

        @SuppressLint("StaticFieldLeak")
        var w = 0f
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)

        DataLocalManager.init(applicationContext)
        ctx = applicationContext
        w = resources.displayMetrics.widthPixels / 100f

//        AppOpenManager.getInstance().setResumeCallback(object : AdCallback(){
//            override fun onAdLoaded() {
//                super.onAdLoaded()
//            }
//
//            override fun onAdClosed() {
//                super.onAdClosed()
//                AdsConfig.lastTimeShowInter = System.currentTimeMillis()
//            }
//        })
    }

    override fun enableAdsResume(): Boolean = false //true

    override fun getKeyRemoteIntervalShowInterstitial(): String = ""

    override fun getListTestDeviceId(): MutableList<String>?  = null

    override fun getResumeAdId(): String = getString(R.string.appopen_resume)

    override fun buildDebug(): Boolean  = BuildConfig.DEBUG

    override fun enableAdjustTracking(): Boolean = true

    override fun getAdjustToken(): String = getString(R.string.adjust_token)

    override fun logRevenueAdjustWithCustomEvent(p0: Double, p1: String?) {
        Adjust.getInstance().logRevenueWithCustomEvent("token event", p0, p1)
    }
}