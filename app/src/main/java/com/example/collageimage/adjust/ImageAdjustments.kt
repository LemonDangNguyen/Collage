package com.example.testadjust

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve3x3

class ImageAdjustments {

    fun applyAdjustments(
        brightness: Float,
        contrast: Float,
        saturation: Float,
        clarity: Float,
        shadows: Float,
        highlights: Float,
        exposure: Float,
        gamma: Float,
        blacks: Float,
        whites: Float,
        temperature: Float,
        fl: Float
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

        // Clarity Adjustment
        val clarityMatrix = ColorMatrix(floatArrayOf(
            1f + clarity, 0f, 0f, 0f, 0f,
            0f, 1f + clarity, 0f, 0f, 0f,
            0f, 0f, 1f + clarity, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(clarityMatrix)

        // Shadows Adjustment
        val shadowsMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, shadows * 255f,
            0f, 1f, 0f, 0f, shadows * 255f,
            0f, 0f, 1f, 0f, shadows * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(shadowsMatrix)

        // Highlights Adjustment
        val highlightsMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, highlights * 255f,
            0f, 1f, 0f, 0f, highlights * 255f,
            0f, 0f, 1f, 0f, highlights * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(highlightsMatrix)

        // Exposure Adjustment
        val exposureMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, exposure * 255f,
            0f, 1f, 0f, 0f, exposure * 255f,
            0f, 0f, 1f, 0f, exposure * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(exposureMatrix)

        // Gamma Adjustment
        val gammaScale = 1 / gamma
        val gammaMatrix = ColorMatrix(floatArrayOf(
            gammaScale, 0f, 0f, 0f, 0f,
            0f, gammaScale, 0f, 0f, 0f,
            0f, 0f, gammaScale, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(gammaMatrix)

        // Blacks Adjustment
        val blacksMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, blacks * 255f,
            0f, 1f, 0f, 0f, blacks * 255f,
            0f, 0f, 1f, 0f, blacks * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(blacksMatrix)

        // Whites Adjustment
        val whitesMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, whites * 255f,
            0f, 1f, 0f, 0f, whites * 255f,
            0f, 0f, 1f, 0f, whites * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(whitesMatrix)

        // Temperature Adjustment
        val tempScale = if (temperature > 0) 1f + temperature else 1f - temperature
        val temperatureMatrix = ColorMatrix(floatArrayOf(
            tempScale, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f - temperature, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(temperatureMatrix)

        return ColorMatrixColorFilter(colorMatrix)
    }

    fun applySharpness(bitmap: Bitmap, sharpness: Float, context: Context): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs))
        val kernel = floatArrayOf(
            0f, -sharpness, 0f,
            -sharpness, 1f + 4 * sharpness, -sharpness,
            0f, -sharpness, 0f
        )
        script.setCoefficients(kernel)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap)
        rs.destroy()
        return bitmap
    }
}
