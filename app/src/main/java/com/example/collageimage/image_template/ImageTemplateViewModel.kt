package com.example.collageimage.image_template

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.collageimage.R

class ImageTemplateViewModel : ViewModel() {

    // LiveData chứa danh sách hình ảnh
    private val _imageList = MutableLiveData<List<ImagetemplateModel>>()
    val imageList: LiveData<List<ImagetemplateModel>> get() = _imageList

    // Khởi tạo và load hình ảnh
    init {
        loadImages()
    }

    // Phương thức load hình ảnh vào danh sách
    private fun loadImages() {
        val imageResIds = listOf(
            R.drawable.templatee01, R.drawable.templatee02, R.drawable.templatee03,
            R.drawable.templatee04, R.drawable.templatee05, R.drawable.templatee06,
            R.drawable.templatee07, R.drawable.templatee08, R.drawable.templatee09,
            R.drawable.templatee10, R.drawable.templatee11, R.drawable.templatee12,
            R.drawable.templatee13, R.drawable.templatee14, R.drawable.templatee15,
            R.drawable.templatee16, R.drawable.templatee17, R.drawable.templatee18,
            R.drawable.templatee19, R.drawable.templatee20, R.drawable.templatee21,
            R.drawable.templatee22, R.drawable.templatee23, R.drawable.templatee24,
            R.drawable.templatee25, R.drawable.templatee26, R.drawable.templatee27,
            R.drawable.templatee28, R.drawable.templatee29, R.drawable.templatee30
        )

        // Tạo danh sách ImagetemplateModel với ID tăng dần
        val imageListWithIds = imageResIds.mapIndexed { index, resId ->
            ImagetemplateModel(id = index + 1, imageResId = resId)
        }

        // Cập nhật LiveData với danh sách hình ảnh
        _imageList.value = imageListWithIds
    }
}
