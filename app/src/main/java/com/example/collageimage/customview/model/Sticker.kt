package com.example.collageimage.customview.model

import android.view.View

data class Sticker(
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val view: View? = null,
    val flip: Boolean = false
)
