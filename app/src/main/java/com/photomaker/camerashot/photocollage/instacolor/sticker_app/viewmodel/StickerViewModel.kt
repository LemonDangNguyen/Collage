package com.photomaker.camerashot.photocollage.instacolor.sticker_app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.ActionType
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.Sticker
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerAction
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerHistoryModel
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerIcon
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerPhoto
import com.photomaker.camerashot.photocollage.instacolor.sticker_app.model.StickerText

class StickerViewModel : ViewModel() {

    // LiveData quản lý danh sách sticker
    private val _stickers = MutableLiveData<MutableList<Sticker>>().apply { value = mutableListOf() }
    val stickers: LiveData<MutableList<Sticker>> = _stickers

    // StickerHistoryModel quản lý Undo/Redo
    private val model = StickerHistoryModel()

    // Thêm sticker text
    fun addStickerText(text: String, textSize: Float, textColor: Int, textFont: String, x: Float, y: Float) {
        val stickerText = StickerText(
            x = x,
            y = y,
            rotation = 0f,
            text = text,
            textSize = textSize,
            textColor = textColor,
            textFont = textFont
        )
        _stickers.value?.add(stickerText)
        _stickers.value = _stickers.value
        model.addAction(StickerAction(stickerText, Pair(x, y), ActionType.ADD))
    }

    // Thêm sticker icon
    fun addStickerPhoto(photo: Bitmap, x: Float, y: Float) {
        val stickerIcon = StickerIcon(
            x = x,
            y = y,
            rotation = 0f,
            bitmap = photo,
            scaleX = 1f,
            scaleY = 1f
        )
        _stickers.value?.add(stickerIcon)
        _stickers.value = _stickers.value
        model.addAction(StickerAction(stickerIcon, Pair(x, y), ActionType.ADD))
    }

    // Thêm sticker meme
    fun addStickerMeme(icon: Bitmap, x: Float, y: Float) {
        val stickerPhoto = StickerPhoto(
            x = x,
            y = y,
            rotation = 0f,
            bitmap = icon,
            scaleX = 1f,
            scaleY = 1f
        )
        _stickers.value?.add(stickerPhoto)
        _stickers.value = _stickers.value
        model.addAction(StickerAction(stickerPhoto, Pair(x, y), ActionType.ADD))
    }

    // Xóa sticker
    fun removeSticker(sticker: Sticker) {
        _stickers.value?.remove(sticker)
        _stickers.value = _stickers.value
        model.addAction(StickerAction(sticker, Pair(sticker.x, sticker.y), ActionType.REMOVE))
    }

    // Undo
    fun undo() {
        val action = model.undo()
        action?.let {
            when (it.actionType) {
                ActionType.ADD -> _stickers.value?.remove(it.sticker)
                ActionType.REMOVE -> _stickers.value?.add(it.sticker)
            }
            _stickers.value = _stickers.value
        }
    }

    // Redo
    fun redo() {
        val action = model.redo()
        action?.let {
            when (it.actionType) {
                ActionType.ADD -> _stickers.value?.add(it.sticker)
                ActionType.REMOVE -> _stickers.value?.remove(it.sticker)
            }
            _stickers.value = _stickers.value
        }
    }

    // Xóa tất cả sticker
    fun clearAllStickers() {
        _stickers.value?.clear()
        _stickers.value = _stickers.value
        model.clearHistory()
    }
}
