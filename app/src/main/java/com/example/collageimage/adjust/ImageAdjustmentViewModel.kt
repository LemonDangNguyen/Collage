package com.example.testadjust

import android.graphics.ColorMatrixColorFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageAdjustmentViewModel : ViewModel() {
    private val imageAdjustments = ImageAdjustments()

    // LiveData cho các giá trị điều chỉnh
    val brightness = MutableLiveData(0f)
    val contrast = MutableLiveData(0f)
    val saturation = MutableLiveData(1f)
    val clarity = MutableLiveData(0f)
    val shadows = MutableLiveData(0f)
    val highlights = MutableLiveData(0f)
    val exposure = MutableLiveData(0f)
    val gamma = MutableLiveData(1f)
    val blacks = MutableLiveData(0f)
    val whites = MutableLiveData(0f)
    val sharpness = MutableLiveData(0f)
    val temperature = MutableLiveData(0f)

    private val _colorFilter = MutableLiveData<ColorMatrixColorFilter>()
    val colorFilter: LiveData<ColorMatrixColorFilter> get() = _colorFilter

    // Cập nhật và áp dụng bộ lọc
    fun updateFilter() {
        _colorFilter.value = imageAdjustments.applyAdjustments(
            brightness.value ?: 0f,
            contrast.value ?: 0f,
            saturation.value ?: 1f,
            clarity.value ?: 0f,
            shadows.value ?: 0f,
            highlights.value ?: 0f,
            exposure.value ?: 0f,
            gamma.value ?: 1f,
            blacks.value ?: 0f,
            whites.value ?: 0f,
            sharpness.value ?: 0f,
            temperature.value ?: 0f
        )
    }
}
