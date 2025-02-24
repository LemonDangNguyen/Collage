package com.photomaker.camerashot.photocollage.instacolor.Gradient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.photomaker.camerashot.photocollage.instacolor.R


class GradientViewModel : ViewModel() {
    // Đổi từ LiveData<GradientItem> thành LiveData<List<GradientItem>> để lưu trữ danh sách
    private val _selectedGradient = MutableLiveData<List<GradientItem>>()
    val selectedGradient: LiveData<List<GradientItem>> get() = _selectedGradient

    init {
        // Gán một danh sách các GradientItem cho _selectedGradient
        _selectedGradient.value = listOf(
            GradientItem(R.drawable.ic_gradient_01),
            GradientItem(R.drawable.ic_gradient_02),
            GradientItem(R.drawable.ic_gradient_03),
            GradientItem(R.drawable.ic_gradient_04),
            GradientItem(R.drawable.ic_gradient_05),
            GradientItem(R.drawable.ic_gradient_06),
            GradientItem(R.drawable.ic_gradient_07),
            GradientItem(R.drawable.ic_gradient_08),
            GradientItem(R.drawable.ic_gradient_09),
            GradientItem(R.drawable.ic_gradient_10),
            GradientItem(R.drawable.ic_gradient_11),
            GradientItem(R.drawable.ic_gradient_12),
            GradientItem(R.drawable.ic_gradient_13),
            GradientItem(R.drawable.ic_gradient_14),
            GradientItem(R.drawable.ic_gradient_15)
        )
    }
}
