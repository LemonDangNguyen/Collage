package com.photomaker.camerashot.photocollage.instacolor.extensions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat


fun Context.openSettingPermission(action: String) {
    val intent = Intent(action).apply { data = Uri.fromParts("package", packageName, null) }
    startActivity(intent)
}

fun Context.showToast(msg: String, gravity: Int) {
    val toast: Toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    toast.setGravity(gravity, 0, 0)
    toast.show()
}

fun Context.checkPer(str: Array<String>): Boolean {
    if (str.isEmpty()) return true
    var isCheck = true
    for (i in str) {
        if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED)
            isCheck = false
    }
    return isCheck
}

fun Context.checkAllPerGrand(): Boolean {
    val storagePer = checkPer(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))

    val cameraPer = checkPer(arrayOf(Manifest.permission.CAMERA))

    return storagePer && cameraPer
}
