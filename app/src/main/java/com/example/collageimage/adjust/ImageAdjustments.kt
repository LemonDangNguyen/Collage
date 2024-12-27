package com.example.collageimage.adjust


import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter


class ImageAdjustments {

    fun applyAdjustments(
        brightness: Float,
        contrast: Float,
        saturation: Float,
        shadows: Float,
        highlights: Float,
        sharpness: Float // Thêm tham số sharpness
    ): ColorMatrixColorFilter {
        val colorMatrix = ColorMatrix()

        // Brightness Adjustment
        val brightnessMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness * 255f,
            0f, 1f, 0f, 0f, brightness * 255f,
            0f, 0f, 1f, 0f, brightness * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(brightnessMatrix)

        // Contrast Adjustment
        val scale = contrast + 1f
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(contrastMatrix)

        // Saturation Adjustment
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)
        colorMatrix.postConcat(saturationMatrix)

        // Shadows Adjustment
        val shadowsMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, if (shadows > 0) shadows * 255f else 0f,
            0f, 1f, 0f, 0f, if (shadows > 0) shadows * 255f else 0f,
            0f, 0f, 1f, 0f, if (shadows > 0) shadows * 255f else 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(shadowsMatrix)

        // Highlights Adjustment
        val highlightsMatrix = ColorMatrix(floatArrayOf(
            if (highlights < 0) 1f + highlights else 1f, 0f, 0f, 0f, if (highlights > 0) highlights * -255f else 0f,
            0f, if (highlights < 0) 1f + highlights else 1f, 0f, 0f, if (highlights > 0) highlights * -255f else 0f,
            0f, 0f, if (highlights < 0) 1f + highlights else 1f, 0f, if (highlights > 0) highlights * -255f else 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(highlightsMatrix)


        return ColorMatrixColorFilter(colorMatrix)
    }

}

