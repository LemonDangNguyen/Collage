package com.photomaker.camerashot.photocollage.instacolor.sticker_app.model
data class StickerText(
    override val x: Float,
    override val y: Float,
    override val rotation: Float,
    val text: String,        // Nội dung văn bản
    val textSize: Float,     // Cỡ chữ
    val textColor: Int,      // Màu chữ
    val textFont: String     // Font chữ
) : Sticker(x, y, rotation)