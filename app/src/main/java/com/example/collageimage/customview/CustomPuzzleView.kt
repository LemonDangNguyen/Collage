package com.example.collageimage.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import com.hypersoft.pzlayout.view.PuzzleView
import com.hypersoft.pzlayout.utils.PuzzlePiece

class CustomPuzzleView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PuzzleView(context, attrs, defStyleAttr) {

    private val borderPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.STROKE
        strokeWidth = 0f
    }
    private var handlingPiece: PuzzlePiece? = null
    private var backgroundDrawable: Drawable? = null
    private var firstSelectedPiece: PuzzlePiece? = null
    private var swapModeEnabled = false

    override fun onDraw(canvas: Canvas) {
        backgroundDrawable?.setBounds(0, 0, width, height)
        backgroundDrawable?.draw(canvas)
        super.onDraw(canvas)

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

    fun enableSwapMode(enable: Boolean) {
        swapModeEnabled = enable
        firstSelectedPiece = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!swapModeEnabled) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            val selectedPiece = findHandlingPiece(event.x, event.y)

            if (selectedPiece != null) {
                if (firstSelectedPiece == null) {
                    firstSelectedPiece = selectedPiece
                } else if (firstSelectedPiece != selectedPiece) {
                    swapPieces(firstSelectedPiece!!, selectedPiece)
                    firstSelectedPiece = null
                    enableSwapMode(false) // Tắt chế độ swap sau khi hoàn thành
                } else {
                    firstSelectedPiece = null
                }
            }
        }
        return true
    }

    private fun findHandlingPiece(x: Float, y: Float): PuzzlePiece? {
        return getPuzzlePieces().find { it?.contains(x, y) == true }
    }

    private fun swapPieces(piece1: PuzzlePiece, piece2: PuzzlePiece) {
        val tempDrawable = piece1.getDrawable()
        val tempPath = piece1.path

        piece1.setDrawable(piece2.getDrawable())
        piece1.path = piece2.path

        piece2.setDrawable(tempDrawable)
        piece2.path = tempPath

        piece1.fillArea(this, true)
        piece2.fillArea(this, true)

        invalidate()
    }
    fun selectPiece(piece: PuzzlePiece?) {
        handlingPiece = if (handlingPiece == piece) null else piece
        invalidate() // Vẽ lại để cập nhật viền
    }

    // Kiểm tra xem mảnh có được chọn không
    fun isPieceSelected(piece: PuzzlePiece?): Boolean {
        return handlingPiece == piece
    }


}
