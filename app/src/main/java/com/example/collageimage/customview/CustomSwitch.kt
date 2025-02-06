package com.example.collageimage.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.collageimage.R
import com.nmh.base_lib.callback.StatusResultSwitch

class CustomSwitch : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val rectF = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private var lineShaderTrackEnable: LinearGradient? = null
    private var colorsTrackEnable = intArrayOf(
        ContextCompat.getColor(context, R.color.color_059DDF),
        ContextCompat.getColor(context, R.color.color_0AD6F2),
    )
    private var lineShaderTrackDisable: LinearGradient? = null
    private var colorsTrackDisable = intArrayOf(
        ContextCompat.getColor(context, R.color.color_DFDFDF),
        ContextCompat.getColor(context, R.color.color_DFDFDF),
    )
    private var isCallback = true
    private var isChecked = false
    private var isReset = false
    private var thumbX = 0f
    private var margin = 0f
    private var padding = 0f
    private var sizeThumb = 0f
    private var valueAnimator: ValueAnimator = ValueAnimator().apply {
        duration = 334
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animation ->
            thumbX = animation.animatedValue as Float
            invalidate()
        }
    }
    var onResult: StatusResultSwitch? = null

    init {
        setOnClickListener { setCheck(!isChecked, true) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        padding = 0.084f * width
        margin = 0.084f * width
        sizeThumb = 0.40f * width - padding

        if (lineShaderTrackEnable == null)
            lineShaderTrackEnable = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(), colorsTrackEnable, null, Shader.TileMode.CLAMP
            )
        if (lineShaderTrackDisable == null)
            lineShaderTrackDisable = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(), colorsTrackDisable, null, Shader.TileMode.CLAMP
            )

        paint.shader = if (isChecked) lineShaderTrackEnable else lineShaderTrackDisable
        rectF.set(padding, padding / 2f, width.toFloat() - padding, height.toFloat() - padding / 2f)
        canvas.drawRoundRect(rectF, height / 2f, height / 2f, paint)

        paint.apply {
            shader = null
            color = if (isChecked) ContextCompat.getColor(context, R.color.white)
            else ContextCompat.getColor(context, R.color.white)
        }
        canvas.drawCircle( padding + margin + thumbX + sizeThumb / 2f, height / 2f, sizeThumb / 2f, paint)

        if (isReset) {
            val startValue = if (this.isChecked) 0f else width - 2 * padding - sizeThumb - 2 * margin
            val endValue = if (this.isChecked) width - 2 * padding - sizeThumb - 2 * margin else 0f
            valueAnimator.apply {
                setFloatValues(startValue, endValue)
                start()
            }
            isReset = false
        }
    }

    private fun setChecked(isChecked: Boolean, isCallback: Boolean) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked
            if (width == 0) isReset = true
            val startValue = if (this.isChecked) 0f else width - 2 * padding - sizeThumb - 2 * margin
            val endValue = if (this.isChecked) width - 2 * padding - sizeThumb - 2 * margin else 0f
            valueAnimator.apply {
                setFloatValues(startValue, endValue)
                start()
            }
            if (isCallback) onResult?.onResult(this.isChecked)
        }
    }

    fun getChecked(): Boolean = isChecked

    fun setCheck(isChecked: Boolean, isCallback: Boolean = false) {
        this.setChecked(isChecked, isCallback)
    }
}