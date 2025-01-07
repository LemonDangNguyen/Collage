package com.example.collageimage.customview.model

import android.graphics.Paint
import android.graphics.Path

data class PaintPath(
    var path: Path,
    var paint: Paint,
    var isColored: Boolean = false // Thêm thuộc tính isColored mặc định là false
)

