package com.nmh.base.project.extensions

import android.content.Context
import android.util.TypedValue
import android.widget.EditText
import com.nmh.base.project.utils.Utils

fun EditText.textCustom(contentHint: String, colorHint: Int, color: Int, textSize: Float, font: String, context: Context) {
    hint = contentHint
    setHintTextColor(colorHint)
    setTextColor(color)
    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    typeface = try {
        Utils.getTypeFace(font.substring(0, 8), "$font.ttf", context)
    } catch (e: Exception) {
        Utils.getTypeFace(font.substring(0, 8), "$font.otf", context)
    }
}