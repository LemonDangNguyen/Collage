package com.example.collageimage.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View

class BlurParentCollage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var aspectRatio: Float = 1f // Mặc định 1:1
    private val horizontalPadding = (context.resources.displayMetrics.density * 20).toInt()

    fun setAspectRatio(ratio: Float) {
        aspectRatio = ratio
        requestLayout() // Yêu cầu đo lại view
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        val actualWidth = if (aspectRatio == 9f / 16f) parentWidth - 2 * horizontalPadding else parentWidth

        val width: Int
        val height: Int
        if (actualWidth / aspectRatio <= parentHeight) {
            width = actualWidth
            height = (actualWidth / aspectRatio).toInt()
        } else {
            height = parentHeight
            width = (parentHeight * aspectRatio).toInt()
        }

        if (aspectRatio == 9f / 16f) {
            setMeasuredDimension(width, height)
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
        } else {
            setMeasuredDimension(width, height)
            setPadding(0, 0, 0, 0)
        }
    }

}
