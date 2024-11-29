package com.example.collageimage.view_template

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.collageimage.R

class ViewTemplate01(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var backgroundBitmap: Bitmap
    private lateinit var bitmap1: Bitmap

    init {

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.template_05)
        backgroundBitmap = backgroundDrawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)


        val drawable1 = ContextCompat.getDrawable(context, R.drawable.template_05_cover_bg)


        bitmap1 = drawable1?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        val rectF = RectF(0f, 0f, width, height)


        canvas.drawBitmap(backgroundBitmap, null, rectF, Paint())

        canvas.drawBitmap(bitmap1, null, rectF, Paint())
    }
}
