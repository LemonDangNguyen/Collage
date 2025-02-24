package com.photomaker.camerashot.photocollage.instacolor.StickerApp.model
data class StickerAction(
    val sticker: Sticker,              // Thay đổi từ View sang Sticker
    val position: Pair<Float, Float>, // Vị trí của sticker
    val actionType: ActionType        // Loại hành động: ADD hoặc REMOVE
)


enum class ActionType {
    ADD,
    REMOVE
}
