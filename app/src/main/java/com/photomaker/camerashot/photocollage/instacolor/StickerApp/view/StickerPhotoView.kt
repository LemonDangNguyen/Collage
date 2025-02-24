package com.photomaker.camerashot.photocollage.instacolor.StickerApp.view

import android.graphics.Rect
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.photomaker.camerashot.photocollage.instacolor.StickerApp.model.StickerPhoto

class StickerPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    sticker: StickerPhoto? = null
) : BaseStickerView(context, attrs, sticker) {

    private val imageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }
    }

    init {
        addView(imageView)
        post { updateBorderSize() }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
        updateBorderSize()
    }

    override fun updateBorderSize() {
        val imageViewWidth = (imageView.width * imageView.scaleX).toInt()
        val imageViewHeight = (imageView.height * imageView.scaleY).toInt()
        val padding = 25

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