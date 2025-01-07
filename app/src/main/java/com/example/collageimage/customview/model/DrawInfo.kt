package com.example.collageimage.customview.model

import android.graphics.Bitmap


data class DrawInfo(
    var bitmap: Bitmap?,
    val listHistory: MutableList<PaintPath?>,
    val listUndo: MutableList<PaintPath?>,

    var stickers: MutableList<Sticker>? = mutableListOf()
) {
    fun setInfo(drawInfo: DrawInfo) {
        this.bitmap = drawInfo.bitmap
        this.listHistory.clear()
        this.listHistory.addAll(drawInfo.listHistory)
        this.listUndo.clear()
        this.listUndo.addAll(drawInfo.listUndo)
        this.stickers?.clear()
        drawInfo.stickers?.let { this.stickers?.addAll(it) }

    }
}
