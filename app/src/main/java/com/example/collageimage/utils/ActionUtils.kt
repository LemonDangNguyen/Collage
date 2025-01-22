package com.nmh.base.project.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.collageimage.R
import com.nlbn.ads.util.AppOpenManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ActionUtils {

    private const val AUTHORITY = "com.fakecall.videocall.fakevideo.prankcall"
    private const val GMAIL = "abdallahshure99@gmail.com"
    private const val POLICY = "https://shureestudio.netlify.app/policy"
    private const val HOW_TO_USE = ""
    private const val ID_DEV = ""
    private const val LINK_FACE = "https://www.facebook.com/"
    private const val LINK_FACE_ID = ""
    private const val LINK_INS = "nmh_global"

    fun openOtherApps(c: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uriBuilder = Uri.parse("https://play.google.com/store/apps/dev")
            .buildUpon()
            .appendQueryParameter("id", ID_DEV)
        intent.data = uriBuilder.build()
        try {
            c.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun openPolicy(c: Context) {
        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(POLICY.replace("HTTPS", "https"))
            c.startActivity(i)

            AppOpenManager.getInstance().disableAppResumeWithActivity(c::class.java)
        } catch (e: ActivityNotFoundException) {
            Utils.showToast(c, c.getString(R.string.no_browser), Gravity.CENTER)
        }
    }

    fun openHowToUse(c: Context) {
        openLink(c, HOW_TO_USE)
    }

    fun openFacebook(c: Context) {
        try {
            val applicationInfo = c.packageManager.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                val uri = Uri.parse("fb://page/$LINK_FACE_ID")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                c.startActivity(intent)
                return
            }
        } catch (ignored: Exception) {
        }
        openLink(c, LINK_FACE)
    }

    fun openInstagram(c: Context) {
        val appUri = Uri.parse("https://instagram.com/_u/$LINK_INS")
        try {
            val appIntent =
                c.packageManager.getLaunchIntentForPackage("com.instagram.android")
            appIntent?.let {
                it.action = Intent.ACTION_VIEW
                it.data = appUri
                c.startActivity(it)
                return
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
        openLink(c, "https://instagram.com/$LINK_INS")
    }

    fun rateApp(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
        }
        context.startActivity(intent)
    }

    fun shareApp(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
            }
            var shareMessage = "Let me recommend you this application\nDownload now:\n\n"
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + context.packageName
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "choose one"))

            AppOpenManager.getInstance().disableAppResumeWithActivity(context::class.java)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendFeedback(context: Context) {
        val selectorIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
        }
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(GMAIL))
            putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name) + " feedback")
            putExtra(Intent.EXTRA_TEXT, "")
            selector = selectorIntent
        }
        try {
            context.startActivity(Intent.createChooser(emailIntent, ""))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    fun moreApps(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:$ID_DEV")))
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:$ID_DEV")))
        }
    }

    fun shareFile(context: Context, bitmap: Bitmap, application: String?) {
        val uri = saveImageExternal(context, bitmap)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            setPackage(application)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "image/*")
        }
        context.startActivity(Intent.createChooser(shareIntent, context.resources.getString(R.string.app_name)))
    }

    private fun saveImageExternal(context: Context, image: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "imgNMH.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            uri = FileProvider.getUriForFile(context, AUTHORITY, file)
        } catch (e: IOException) {
            Log.d("TAG", "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    private fun openLink(c: Context, url: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url.replace("HTTPS", "https"))
            c.startActivity(i)

            AppOpenManager.getInstance().disableAppResumeWithActivity(c::class.java)
        } catch (e: ActivityNotFoundException) {
            Utils.showToast(c, c.getString(R.string.no_browser), Gravity.CENTER)
        }
    }
}