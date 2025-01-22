package com.nmh.base.project.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.FileDescriptor
import java.io.IOException

fun Bitmap.resizeBitmap(width: Int, height: Int): Bitmap {
    val img = Bitmap.createScaledBitmap(this, width, height, true)
    this.recycle()
    return img
}

fun Bitmap.getRoundedCornerBitmap(cornerSizePx: Int): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint().apply {
        isAntiAlias = true
        color = -0x1
        style = Paint.Style.FILL
    }
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawRoundRect(rectF, cornerSizePx.toFloat(), cornerSizePx.toFloat(), paint)

    // draw bitmap
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

@SuppressLint("Recycle")
@Throws(IOException::class)
fun Bitmap.modifyOrientation(context: Context, uri: Uri?): Bitmap {
    val ei = ExifInterface(context.contentResolver.openInputStream(uri!!)!!)
    return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(this, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(this, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(this, 270f)
        else -> this
    }
}

@Throws(IOException::class)
fun Bitmap.modifyOrientation(fileDescriptor: FileDescriptor?): Bitmap {
    val ei = ExifInterface(fileDescriptor!!)
    return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(this, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(this, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(this, 270f)
        else -> this
    }
}

fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}