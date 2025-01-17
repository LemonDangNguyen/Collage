package com.draw.viewcustom.model
import android.graphics.Bitmap
data class StickerPhoto(
    override val x: Float,
    override val y: Float,
    override val rotation: Float,
    val bitmap: Bitmap,
    val scaleX: Float,
    val scaleY: Float
) : Sticker(x, y, rotation)