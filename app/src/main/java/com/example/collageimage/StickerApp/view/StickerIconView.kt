package com.example.collageimage.StickerApp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

import com.example.collageimage.StickerApp.model.StickerIcon

class StickerIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    sticker: StickerIcon? = null
) : BaseStickerView(context, attrs, sticker) {

    private val imageView: ImageView = AppCompatImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }
    }

    init {
        addView(imageView)
        post { updateBorderSize() }
    }

    // Cập nhật bitmap
    fun setImageBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
        updateBorderSize()
    }

    // Cập nhật kích thước viền bao quanh dựa trên kích thước
    override fun updateBorderSize() {
        val imageViewWidth = (imageView.width * imageView.scaleX).toInt()
        val imageViewHeight = (imageView.height * imageView.scaleY).toInt()
        val padding = 25 // Padding cho viền

        if (imageViewWidth > 0 && imageViewHeight > 0) {
            borderView.layoutParams = LayoutParams(
                imageViewWidth + padding * 2,
                imageViewHeight + padding * 2
            ).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            borderView.requestLayout()
            updateButtonPositions()
        }
    }

    // Xử lý sự kiện thay đổi kích thước
    override fun handleTransform(event: MotionEvent): Boolean {
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
                    val newScaleX = imageView.scaleX + deltaX / 200
                    val newScaleY = imageView.scaleY + deltaY / 200

                    if (newScaleX > 0.1f && newScaleY > 0.1f) {
                        imageView.scaleX = newScaleX
                        imageView.scaleY = newScaleY
                        updateBorderSize()
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

    // Xử lý touch event cho việc kéo di chuyển sticker icon
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        val imageRect = Rect()
        imageView.getHitRect(imageRect)

        if (!imageRect.contains(touchX.toInt(), touchY.toInt())) {
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

    override fun flipSticker() {
        imageView.scaleX *= -1
    }
}