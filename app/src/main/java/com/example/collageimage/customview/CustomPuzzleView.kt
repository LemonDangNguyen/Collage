package com.example.collageimage.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.hypersoft.pzlayout.view.PuzzleView

class CustomPuzzleView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PuzzleView(context, attrs, defStyleAttr) {

    private val borderPaint = Paint().apply {
        color = Color.WHITE // Màu viền
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var backgroundDrawable: Drawable? = null
    private var colorFilter: ColorFilter? = null // Thêm colorFilter

    override fun onDraw(canvas: Canvas) {
        // Áp dụng colorFilter nếu có cho backgroundDrawable
        backgroundDrawable?.colorFilter = colorFilter

        // Vẽ ảnh nền trước khi vẽ các mảnh ghép
        backgroundDrawable?.setBounds(0, 0, width, height)
        backgroundDrawable?.draw(canvas)

        super.onDraw(canvas)

        // Vẽ viền cho từng mảnh ghép
        getPuzzlePieces().forEach { piece ->
            piece?.area?.let { area ->
                canvas.drawPath(area.areaPath, borderPaint)
            }
        }
    }

    fun setBackgroundImage(resourceId: Int) {
        backgroundDrawable = context?.resources?.getDrawable(resourceId, context.theme)
        invalidate()
    }

    fun setBorderColor(color: Int) {
        borderPaint.color = color
        invalidate()
    }

    fun setBorderWidth(width: Float) {
        borderPaint.strokeWidth = width
        invalidate()
    }
}
