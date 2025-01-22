package com.nmh.base.project.utils

import android.app.ActivityManager
import android.app.ActivityOptions
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.collageimage.R
import java.io.*
import java.net.URLEncoder
import java.util.*

object Utils {
    const val res = android.R.id.content
    private val enter: Int by lazy { R.anim.slide_in_right }
    private val exit: Int by lazy { R.anim.slide_out_left }
    private val popEnter: Int by lazy { R.anim.slide_in_left_small }
    private val popExit: Int by lazy { R.anim.slide_out_right }
    var isShowRateDialog = false

    fun hideKeyboard(context: Context, view: View?) {
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    fun showSoftKeyboard(context: Context, view: View) {
        if (view.requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun replaceFragment(
        manager: FragmentManager,
        fragment: Fragment,
        isAdd: Boolean,
        addBackStack: Boolean,
        isAnimation: Boolean
    ) {
        try {
            val fragmentTransaction = manager.beginTransaction()
            if (isAnimation) fragmentTransaction.setCustomAnimations(enter, exit, popEnter, popExit)
            if (addBackStack) fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
            if (isAdd) fragmentTransaction.add(res, fragment, fragment.javaClass.simpleName)
            else fragmentTransaction.replace(res, fragment, fragment.javaClass.simpleName)

            fragmentTransaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createBackground(
        colorArr: IntArray,
        border: Int,
        stroke: Int,
        colorStroke: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = border.toFloat()
            if (stroke != -1) setStroke(stroke, colorStroke)

            if (colorArr.size >= 2) {
                colors = colorArr
                gradientType = GradientDrawable.LINEAR_GRADIENT
            } else setColor(colorArr[0])
        }
    }

    fun createColorState(colorOn: Int, colorOff: Int): ColorStateList {
        val states =
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val colors = intArrayOf(colorOn, colorOff)
        return ColorStateList(states, colors)
    }

    fun formatTime(duration: Long): Array<String> {
        val millis = duration % 1000
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        return if (hours > 0) arrayOf(
                "%02d".format(millis / 10),
                "%02d".format(seconds),
                "%02d".format(minutes),
                "%02d".format(hours)
            )
            else arrayOf("%02d".format(millis / 10), "%02d".format(seconds), "%02d".format(minutes))
    }

    @IntRange(from = 0, to = 3)
    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) result = 2
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) result = 1
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) result = 3
        return result
    }

    fun checkScreenOn(context: Context): Boolean {
        for (display in (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays) {
            if (display.state != Display.STATE_OFF) return true
        }
        return false
    }

    fun createChannelNotification(context: Context, importance: Int, channel: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = context.getString(R.string.channel_description)
            val name = context.getString(R.string.channel_name)
            val c = NotificationChannel(channel, name, importance).apply {
                this.description = description
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(c)
        }
    }

    fun getActionText(context: Context, stringRes: String, @ColorRes colorRes: Int): Spannable {
        val spannable = SpannableString(stringRes)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            spannable.setSpan(ForegroundColorSpan(context.getColor(colorRes)), 0, spannable.length, 0)
        return spannable
    }

    @Suppress("DEPRECATION")
    fun makeVibrate(context: Context) {
        val vibrator: Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
        }
    }

    fun effectPressRectangle(context: Context): TypedValue {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        return outValue
    }

    fun effectPressOval(context: Context): TypedValue {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        return outValue
    }

    fun showToast(context: Context, msg: String?, gravity: Int) {
        val toast: Toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.setGravity(gravity, 0, 0)
        toast.show()
    }

    fun setIntent(context: Context, nameActivity: String?) {
        val intent = Intent().apply { component = nameActivity?.let { ComponentName(context, it) } }
        context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left).toBundle())
    }

    fun clearBackStack(manager: FragmentManager) {
        val count = manager.backStackEntryCount
        for (i in 0 until count) {
            manager.popBackStack()
        }
    }

    fun readFromFile(context: Context, nameFile: String?): String {
        val file = File(getStore(context), nameFile)
        val text = StringBuilder()
        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }
        return text.toString()
    }

    fun createFileDescriptorFromByteArray(context: Context, mp3SoundByteArray: ByteArray?): FileDescriptor? {
        try {
            // create temp file that will hold byte array
            val tempMp3 = File.createTempFile("", "mp3", context.cacheDir)
            tempMp3.deleteOnExit()
            val fos = FileOutputStream(tempMp3)
            fos.write(mp3SoundByteArray)
            fos.close()
            val fis = FileInputStream(tempMp3)
            return fis.fd
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }

    fun convertListToString(lstInt: List<Int?>): String {
        val str = StringBuilder()
        val iterator = lstInt.iterator()
        while (iterator.hasNext()) {
            str.append(iterator.next())
            if (iterator.hasNext()) str.append("_")
        }
        return str.toString()
    }

    fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) return true
        }
        return false
    }

    fun underLine(strUnder: String?): SpannableString {
        val underLine = SpannableString(strUnder)
        underLine.setSpan(UnderlineSpan(), 0, underLine.length, 0)
        return underLine
    }

    fun getMIMEType(fileName: String): String {
        val encoded = try {
            URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        } catch (e: Exception) {
            fileName
        }

        return MimeTypeMap.getFileExtensionFromUrl(encoded).lowercase(Locale.ROOT)
    }

    fun getTypeFace(fontFolder: String, nameFont: String, context: Context): Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/$fontFolder/$nameFont")
    }

    fun saveImage(context: Context, bitmap: Bitmap?, namePic: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (bitmap != null) {
                val outFile = File(makeFilename(context, "$namePic-IMG-${Calendar.getInstance(Locale.getDefault()).time}"))
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, outFile.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, getMIMEType(outFile.path))
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val contentResolver = context.applicationContext.contentResolver
                val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                var newUri: Uri? = null
                try {
                    newUri = contentResolver.insert(contentUri, values)
                    contentResolver.openOutputStream(newUri!!)?.let {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                } catch (e: IOException) {
                    contentResolver.delete(newUri!!, null, null)
                }
            }
        } else {
            if (bitmap != null) {
                try {
                    val outFile = File(makeFilename(context, "$namePic-IMG-${Calendar.getInstance(Locale.getDefault()).time}"))
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, outFile.name)
                        put(MediaStore.Images.Media.MIME_TYPE, getMIMEType(outFile.path))
                        put(MediaStore.MediaColumns.DATA, outFile.path)
                    }
                    val contentResolver = context.contentResolver
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    try {
                        contentResolver.openOutputStream(uri!!)?.let {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                    } catch (e: IOException) {
                        if (uri != null) contentResolver.delete(uri, null, null)
                    }
                } catch (e: java.lang.Exception) {
                    Log.d("onBtnSavePng", e.toString()) // java.io.IOException: Operation not permitted
                }
            }
        }
    }

    private fun makeFilename(context: Context, namePic: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            return getStore(context) + "/" + namePic + ".png"
        var externalRootDir = Environment.getExternalStorageDirectory().path
        if (!externalRootDir.endsWith("/")) externalRootDir += "/"
        val subdir = "media/image/"
        var parentDir = externalRootDir + subdir

        // Create the parent directory
        val parentDirFile = File(parentDir)
        parentDirFile.mkdirs()

        // If we can't write to that special path, try just writing directly to the sdcard
        if (!parentDirFile.isDirectory) parentDir = externalRootDir

        // Turn the title into a filename
        val filename = StringBuilder()
        for (i in 0 until "remi-img".length) {
            if (Character.isLetterOrDigit("remi-img"[i])) filename.append("remi-img"[i])
        }

        // Try to make the filename unique
        var path = ""
        for (i in 0..99) {
            val testPath = if (i > 0) "$parentDir$filename$i.png" else "$parentDir$filename.png"
            try {
                val f = RandomAccessFile(File(testPath), "r")
                f.close()
            } catch (e: Exception) {
                // Good, the file didn't exist
                path = testPath
                break
            }
        }
        return path
    }

    fun getStore(c: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val f = c.getExternalFilesDir(null)
            if (f != null) f.absolutePath else "/storage/emulated/0/Android/data/" + c.packageName
        } else Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + c.packageName
    }

    fun makeFolder(c: Context, nameFolder: String): String {
        val path = getStore(c) + "/" + nameFolder
        val f = File(path)
        if (!f.exists()) f.mkdirs()
        return path
    }
}