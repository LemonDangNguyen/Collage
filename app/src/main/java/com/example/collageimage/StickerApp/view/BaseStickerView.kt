package com.example.collageimage.StickerApp.view
import com.example.collageimage.R
import android.graphics.Rect
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.example.collageimage.StickerApp.model.Sticker
import kotlin.math.atan2

abstract class BaseStickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    var sticker: Sticker? = null
) : RelativeLayout(context, attrs) {

    protected lateinit var borderView: RelativeLayout
    protected lateinit var deleteButton: AppCompatImageView
    protected lateinit var flipButton: AppCompatImageView
    protected lateinit var transformButton: AppCompatImageView
    protected lateinit var rotateButton: AppCompatImageView
    var isResizing = false
    var lastX: Float = 0f
    var lastY: Float = 0f
    private var initialRotation: Float = 0f
    private var midPoint = FloatArray(2)
    private val hideBorderHandler = Handler(android.os.Looper.getMainLooper())
    private val hideBorderRunnable = Runnable { borderView.isVisible = false }

    init {
        setupView()
        alignStickerCenter()
        clipChildren = false
        clipToPadding = false
    }

    private fun alignStickerCenter() {
        post {
            val parent = parent
            if (parent is View) {
                val parentWidth = parent.width
                val parentHeight = parent.height
                val centerX = (parentWidth - this.width * this.scaleX) / 2
                val centerY = (parentHeight - this.height * this.scaleY) / 2

                this.x = centerX
                this.y = centerY
            }
        }
    }

    private fun setupView() {
        borderView = RelativeLayout(context).apply {
            background = createBorderDrawable()
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                }
            isVisible = false
        }
        addView(borderView)

        deleteButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_delete)
            layoutParams = LayoutParams(60, 60).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
                setMargins(0, -15, -15, 0)
            }
            setOnClickListener { removeSticker() }
        }
        borderView.addView(deleteButton)

        flipButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_flip)
            layoutParams = LayoutParams(60, 60).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                addRule(ALIGN_PARENT_START, TRUE)
                setMargins(-15, -15, 0, 0)
            }
            setOnClickListener { flipSticker() }
        }
        borderView.addView(flipButton)

        transformButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_resize)
            layoutParams = LayoutParams(60, 60).apply {
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_END, TRUE)
                setMargins(-15, 0, -15, -15)
            }
            setOnTouchListener { _, event -> handleTransform(event) }
        }
        borderView.addView(transformButton)

        rotateButton = AppCompatImageView(context).apply {
            setImageResource(R.drawable.ic_sticker_rotate)
            layoutParams = LayoutParams(60, 60).apply {
                addRule(ALIGN_PARENT_BOTTOM, TRUE)
                addRule(ALIGN_PARENT_START, TRUE)
                setMargins(-15, 0, -15, -15)
            }
            setOnTouchListener { _, event -> handleRotate(event) }
        }
        borderView.addView(rotateButton)
        updateButtonPositions()
    }

    private fun createBorderDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setStroke(5, Color.parseColor("#FFBDBDBD"))
            setColor(Color.TRANSPARENT) // Màu nền là trong suốt
        }
    }

    fun showBorder() {
        borderView.isVisible = true
        hideBorderHandler.removeCallbacks(hideBorderRunnable)
    }

    fun hideBorderAfterDelay() {
        hideBorderHandler.postDelayed(hideBorderRunnable, 3000)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        val stickerRect = Rect()
        getHitRect(stickerRect)
        if (!stickerRect.contains(touchX.toInt(), touchY.toInt())) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                showBorder()
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - lastX
                val deltaY = event.rawY - lastY
                this.x += deltaX
                this.y += deltaY
                lastX = event.rawX
                lastY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                hideBorderAfterDelay()
            }
        }

        return true
    }

    protected open fun removeSticker() {
        this.visibility = GONE
    }

    protected open fun flipSticker() {
        this.scaleX *= -1
    }

    protected open fun handleTransform(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                isResizing = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY

                    val newScaleX = this.scaleX + deltaX / 200
                    val newScaleY = this.scaleY + deltaY / 200

                    if (newScaleX > 0.1f && newScaleY > 0.1f) {
                        this.scaleX = newScaleX
                        this.scaleY = newScaleY
                    }

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }

            MotionEvent.ACTION_UP -> {
                isResizing = false
                hideBorderAfterDelay()
            }
        }
        return true
    }

    protected open fun handleRotate(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                initialRotation = this.rotation
                calculateMidPoint()

                val dx = event.rawX - midPoint[0]
                val dy = event.rawY - midPoint[1]
                initialRotation = (atan2(dy.toDouble(), dx.toDouble()) * (180 / Math.PI)).toFloat()
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - midPoint[0]
                val dy = event.rawY - midPoint[1]

                val currentAngle = (atan2(dy.toDouble(), dx.toDouble()) * (180 / Math.PI)).toFloat()

                val deltaAngle = currentAngle - initialRotation

                this.rotation = (this.rotation + deltaAngle)

                initialRotation = currentAngle
            }

            MotionEvent.ACTION_UP -> {
                hideBorderAfterDelay()
            }
        }
        return true
    }

    private fun calculateMidPoint() {
        midPoint[0] = this.x + (this.width * this.scaleX) / 2
        midPoint[1] = this.y + (this.height * this.scaleY) / 2
    }

    fun updateButtonPositions() {
        val buttonSize = 60
        val borderPadding = -15

        deleteButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, borderPadding, borderPadding, borderPadding)
        }

        flipButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_TOP, TRUE)
            addRule(ALIGN_PARENT_START, TRUE)
            setMargins(borderPadding, borderPadding, borderPadding, borderPadding)
        }

        transformButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_END, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding)
        }

        rotateButton.layoutParams = LayoutParams(buttonSize, buttonSize).apply {
            addRule(ALIGN_PARENT_BOTTOM, TRUE)
            addRule(ALIGN_PARENT_START, TRUE)
            setMargins(borderPadding, 0, borderPadding, borderPadding)
        }
    }

    abstract fun updateBorderSize()
}