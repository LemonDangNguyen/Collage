package com.example.collageimage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySplashBinding
import com.example.collageimage.language.LanguageActivity
import com.nmh.base.project.helpers.CURRENT_LANGUAGE
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.model.LanguageModel
import com.nmh.base.project.sharepref.DataLocalManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

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
                if (remote.await()) {
                    withContext(Dispatchers.Main) {
                        startActivity()
                    }
                }
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ startActivity() }, 1500)
        }
    }

    private suspend fun loadRemoteConfig(): Boolean {
        return suspendCoroutine { continuation ->
            continuation.resume(true)
        }
    }

    private fun startActivity() {
        DataLocalManager.setBoolean(IS_SHOW_BACK, false)
        startIntent(Intent(this, LanguageActivity::class.java), true)
    }
}
