package com.nmh.base.project.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.util.Util.isOnMainThread
import com.example.collageimage.R
import com.google.gson.Gson
import com.nmh.base.project.model.LanguageModel
import com.nmh.base.project.helpers.URL_ASSETS
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun AppCompatActivity.hideNavigationBar() {
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    window.navigationBarColor = Color.parseColor("#01ffffff")
    window.statusBarColor = Color.TRANSPARENT
    window?.decorView?.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
            or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
}

fun Context.isFileExist(folder: String): Boolean {
    val file = File(folder)
    return file.exists()
}

fun Context.createFileFromUrl(urlString: String, destinationFile: File): Boolean {
    try {
        val connection = URL(urlString).openConnection()
        connection.connect()

        // Tạo luồng đầu vào từ URL
        val inputStream = connection.getInputStream()

        // Tạo luồng đầu ra đến file đích
        val outputStream = FileOutputStream(destinationFile)

        // Đọc dữ liệu từ luồng đầu vào và ghi vào luồng đầu ra
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1)
            outputStream.write(buffer, 0, bytesRead)

        // Đóng các luồng
        inputStream.close()
        outputStream.close()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

suspend fun AppCompatActivity.getUriFileFromAsset(nameFile: String, uriFile: String): Uri? {
    return suspendCoroutine { continuation ->
        try {
            val videoFile = File(cacheDir, nameFile)
            if (!videoFile.exists()) {
                val inputStream: InputStream = assets.open(uriFile.replace(URL_ASSETS, ""))
                val outputStream = FileOutputStream(videoFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                continuation.resume(Uri.fromFile(videoFile))
            } else continuation.resume(Uri.fromFile(videoFile))
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(null)
        }
    }
}

fun Any?.toJson(): String = Gson().toJson(this)

fun AppCompatActivity.changeLanguage(context: Context, language: LanguageModel?): Context {
    language?.let {
        try {
            Locale.setDefault(language.locale)
            return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                this.setLocale(language.locale)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                this.setLocale(Locale.ENGLISH)
            })
        }
    }
    return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
        this.setLocale(Locale.ENGLISH)
    })
}

fun AppCompatActivity.getTempFile(child: String): File? {
    val folder = File(cacheDir, child)
    if (!folder.exists()) {
        if (!folder.mkdir()) {
            showToast(getString(R.string.unknown_error_occurred), Gravity.CENTER)
            return null
        }
    }

    return folder
}

fun AppCompatActivity.startIntent(nameActivity: String, isFinish: Boolean) {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.component = ComponentName(this, nameActivity)
    startActivity(
        intent,
        ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            .toBundle()
    )
    if (isFinish) this.finish()
}

fun AppCompatActivity.startIntent(intent: Intent, isFinish: Boolean) {
    startActivity(
        intent,
        ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            .toBundle()
    )
    if (isFinish) this.finish()
}

fun AppCompatActivity.setAnimExit() {
    overridePendingTransition(R.anim.slide_in_left_small, R.anim.slide_out_right)
}

fun AppCompatActivity.showToast(msg: String, gravity: Int) {
    val toast: Toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    toast.setGravity(gravity, 0, 0)
    toast.show()
}

fun AppCompatActivity.hideKeyboardMain() {
    if (isOnMainThread()) hideKeyboardSync()
    else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun AppCompatActivity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun AppCompatActivity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun AppCompatActivity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.takePersistableUriPermission(uri: Uri, flag: Int) {
    grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val flags = flag and Intent.FLAG_GRANT_READ_URI_PERMISSION
    contentResolver.takePersistableUriPermission(uri, flags)
}

fun AppCompatActivity.createDocumentIntent(types: Array<String>, allowedMultiple: Boolean): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = if (types.isNotEmpty()) {
            putExtra(Intent.EXTRA_MIME_TYPES, types)
            types[0]
        } else "*/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowedMultiple)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

fun AppCompatActivity.openSettingPermission(action: String) {
    val intent = Intent(action)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

private var finish = 0
fun AppCompatActivity.onBackPressed(isAsk: Boolean) {
    if (isAsk) {
        if (finish != 0) {
            finish = 0
            finish()
            setAnimExit()
        } else {
            Toast.makeText(this, R.string.finish, Toast.LENGTH_SHORT).show()
            finish++
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    finish = 0
                }
            }, 1000)
        }
    } else finish()
}

fun AppCompatActivity.setStatusBarTransparent(context: Context, isVisible: Boolean, colorStatus: Int, colorNavi: Int) {
    val decorView = window.decorView
    window.statusBarColor = colorStatus
    window.navigationBarColor = colorNavi

    var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    if (isVisible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        decorView.systemUiVisibility = flags
    } else {
//        window.statusBarColor = Color.TRANSPARENT
//        window.navigationBarColor = Color.TRANSPARENT
//        if (Build.VERSION.SDK_INT >= 30)s
//            WindowCompat.setDecorFitsSystemWindows(window, false)
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//            decorView.systemUiVisibility = flags
//        } else if (Build.VERSION.SDK_INT in 21..26) {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_HIDE_NAVIGATION
//            actionBar?.hide()
//        }

        /**
         * display above lock screen
         */
        var flag = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        val manager =
            applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            manager.requestDismissKeyguard(this, null)
        } else {
            flag = flag or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }
        /**
         * set navigation bar transparent
         */
        decorView.systemUiVisibility = flags
        window.addFlags(flag)
        window.navigationBarColor = Color.parseColor("#01ffffff")
        window.statusBarColor = Color.TRANSPARENT

        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).let {
            @SuppressLint("InvalidWakeLockTag")
            val wOn = it.newWakeLock(PowerManager.FULL_WAKE_LOCK, "tag")
            wOn.acquire(5 * 1000L)
        }
    }
}

inline fun <T : Any> AppCompatActivity.collectFlowOn(
    stateFlow: StateFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            stateFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}

inline fun <T : Any> AppCompatActivity.collectFlowOn(
    sharedFlow: SharedFlow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline onResult: (t: T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            sharedFlow.collect {
                onResult.invoke(it)
            }
        }
    }
}
