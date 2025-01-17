package com.draw.viewcustom.model
import android.graphics.Bitmap
data class StickerIcon(
    override val x: Float,
    override val y: Float,
    override val rotation: Float,
    val bitmap: Bitmap,      // Ảnh dưới dạng Bitmap
    val scaleX: Float,       // Độ thu phóng ngang
    val scaleY: Float        // Độ thu phóng dọc
) : Sticker(x, y, rotation)