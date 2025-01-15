package com.example.collageimage.StickerApp.model


import android.graphics.Matrix

enum class StickerType {
    TEXT, ICON, PHOTO
}

data class Sticker(
    val id: Int,
    var type: StickerType,
    var content: Any, // Có thể là String, Drawable, hoặc Bitmap
    var matrix: Matrix = Matrix(),
    var isFlipped: Boolean = false
)
