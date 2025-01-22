package com.nmh.base.project.extensions

import android.content.res.ColorStateList
import android.widget.RadioButton

fun RadioButton.customState(color: Int, isEnable: Boolean) {
    buttonTintList =
        ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(color, color)
        )
    isEnabled = isEnable
}