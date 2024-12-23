package com.example.collageimage.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


       getPuzzlePieces().forEach { piece ->
            piece?.area?.let { area ->
                canvas.drawPath(area.areaPath, borderPaint)
            }
        }
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
