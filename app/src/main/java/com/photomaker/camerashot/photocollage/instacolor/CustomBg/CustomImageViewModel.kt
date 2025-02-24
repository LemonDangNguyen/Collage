package com.photomaker.camerashot.photocollage.instacolor.CustomBg

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.photomaker.camerashot.photocollage.instacolor.R


class CustomImageViewModel : ViewModel() {

    // Dữ liệu LiveData để quản lý danh sách hình ảnh
    private val _customImages = MutableLiveData<List<CustomImage>>()
    val customImages: LiveData<List<CustomImage>> get() = _customImages

    init {
        // Gán giá trị mặc định cho danh sách hình ảnh
        _customImages.value = listOf(
            CustomImage(R.drawable.ic_custom_bg_01),
            CustomImage(R.drawable.ic_custom_bg_02),
            CustomImage(R.drawable.ic_custom_bg_03),
            CustomImage(R.drawable.ic_custom_bg_04),
            CustomImage(R.drawable.ic_custom_bg_05),
            CustomImage(R.drawable.ic_custom_bg_06),
            CustomImage(R.drawable.ic_custom_bg_07),
            CustomImage(R.drawable.ic_custom_bg_08),
            CustomImage(R.drawable.ic_custom_bg_09),
            CustomImage(R.drawable.ic_custom_bg_10)
        )
    }
}
