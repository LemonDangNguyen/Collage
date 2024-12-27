package com.example.collageimage.adjust

import android.graphics.ColorMatrixColorFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.collageimage.adjust.ImageAdjustments

class ImageAdjustmentViewModel : ViewModel() {
    private val imageAdjustments = ImageAdjustments()

    // LiveData cho các giá trị điều chỉnh
    val brightness = MutableLiveData(0f)
    val contrast = MutableLiveData(0f)
    val saturation = MutableLiveData(1f)
    val shadows = MutableLiveData(0f)
    val highlights = MutableLiveData(0f)
    val sharpness = MutableLiveData(0f)
    val warmth = MutableLiveData(0f)
    val vignette = MutableLiveData(0f)
    val hue = MutableLiveData(0f)
    val grain = MutableLiveData(0f)
    val tint = MutableLiveData(0f)
    val fade = MutableLiveData(0f)

    private val _colorFilter = MutableLiveData<ColorMatrixColorFilter>()
    val colorFilter: LiveData<ColorMatrixColorFilter> get() = _colorFilter

    fun updateFilter() {
        val currentBrightness = brightness.value ?: 0f
        val currentContrast = contrast.value ?: 0f
        val currentSaturation = saturation.value ?: 1f
        val currentShadows = shadows.value ?: 0f
        val currentHighlights = highlights.value ?: 0f
        val currentSharpness = sharpness.value ?: 0f
        val currentWarmth = warmth.value ?: 0f
        val currentVignette = vignette.value ?: 0f
        val currentHue = hue.value ?: 0f
        val currentGrain = grain.value ?: 0f
        val currentTint = tint.value ?: 0f
        val currentFade = fade.value ?: 0f

        // Tính toán lại bộ lọc màu
        _colorFilter.value = imageAdjustments.applyAdjustments(
            brightness = currentBrightness,
            contrast = currentContrast,
            saturation = currentSaturation,
            shadows = currentShadows,
            highlights = currentHighlights,
            sharpness = currentSharpness,
//            warmth = currentWarmth,
//            vignette = currentVignette,
//            hue = currentHue,
//            grain = currentGrain,
//            tint = currentTint,
//            fade = currentFade
        )


    }
}
