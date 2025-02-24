package com.photomaker.camerashot.photocollage.instacolor.ratio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Color

class AspectRatioViewModel : ViewModel() {
    private val _aspectRatio = MutableLiveData<Float>(1f) // Mặc định 1:1
    private val _backgroundColor = MutableLiveData<Int>(Color.TRANSPARENT) // Mặc định màu trắng

    val aspectRatio: LiveData<Float> = _aspectRatio
    val backgroundColor: LiveData<Int> = _backgroundColor

    fun setAspectRatio(ratio: Float) {
        _aspectRatio.value = ratio
      //  updateBackgroundColor(ratio)
    }

    private fun updateBackgroundColor(ratio: Float) {
        // Màu sắc có thể được cấu hình tùy ý
        _backgroundColor.value = when (ratio) {
            1f -> Color.RED
            4f / 5f -> Color.GREEN
            5f/4f -> Color.MAGENTA
            3f / 4f -> Color.BLUE
            9f / 16f -> Color.YELLOW
            else -> Color.GRAY
        }
    }
}
