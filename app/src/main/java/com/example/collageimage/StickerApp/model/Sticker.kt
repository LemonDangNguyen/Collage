package com.draw.viewcustom.model



open class Sticker(
    open val x: Float,           // Tọa độ X
    open val y: Float,           // Tọa độ Y
    open val rotation: Float,    // Góc xoay của sticker
    var isDeleted: Boolean = false // Trạng thái xóa của sticker
)
