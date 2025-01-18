package com.example.collageimage.StickerApp.view
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.draw.viewcustom.model.StickerText
import com.example.collageimage.R

class StickerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    sticker: StickerText? = null
) : BaseStickerView(context, attrs, sticker) {

    private val stickerTextView: TextView = TextView(context).apply {
        text = sticker?.text ?: "Sticker Text"
        textSize = sticker?.textSize ?: 24f
        setTextColor(sticker?.textColor ?: Color.BLACK)
        gravity = Gravity.CENTER
        setBackgroundColor(Color.TRANSPARENT)
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }
    }

    init {
        addView(stickerTextView)
        post { updateBorderSize() }
    }

    fun updateText(newText: String) {
        stickerTextView.text = newText
        updateBorderSize()
    }

    fun setTextSize(newSize: Float) {
        stickerTextView.textSize = newSize
        updateBorderSize()
    }

    fun setTextColor(color: Int) {
        stickerTextView.setTextColor(color)
    }

    fun setFont(fontName: String) {
        val fontResId = when (fontName) {
            "nunito_black" -> R.font.nunito_black
            "nunito_bold" -> R.font.nunito_bold
            "nunito_extrabold" -> R.font.nunito_extrabold
            "nunito_light" -> R.font.nunito_light
            "nunito_medium" -> R.font.nunito_medium
            "nunito_regular" -> R.font.nunito_regular
            "nunito_semibold" -> R.font.nunito_semibold
            "i_ciel_cadena" -> R.font.i_ciel_cadena
            else -> R.font.nunito_regular
        }

        val typeface = ResourcesCompat.getFont(context, fontResId)
        if (typeface != null) {
            stickerTextView.typeface = typeface
        }
    }

    // Cập nhật kích thước viền bao quanh dựa trên kích thước TextView
    override fun updateBorderSize() {
        val textViewWidth = (stickerTextView.width * stickerTextView.scaleX).toInt()
        val textViewHeight = (stickerTextView.height * stickerTextView.scaleY).toInt()
        val padding = 25 // Padding cho viền

        if (textViewWidth > 0 && textViewHeight > 0) {
            borderView.layoutParams = LayoutParams(
                textViewWidth + padding * 2,
                textViewHeight + padding * 2
            ).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            borderView.requestLayout()
            updateButtonPositions()
        }
    }

    // Gọi phương thức lật từ lớp cha và thông báo qua listener
    override fun flipSticker() {
        super.flipSticker()
    }

    // Xử lý thay đổi kích thước, không thay đổi logic
    override fun handleTransform(event: MotionEvent): Boolean {
        return super.handleTransform(event)
    }

    // Xử lý touch event, không thay đổi logic
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}