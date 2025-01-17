package com.draw.viewcustom.model

import android.view.View


data class StickerText(
    override val x: Float,
    override val y: Float,
    override val rotation: Float,
    val text: String,        // Nội dung văn bản
    val textSize: Float,     // Cỡ chữ
    val textColor: Int,      // Màu chữ
    val textFont: String     // Font chữ
) : Sticker(x, y, rotation)