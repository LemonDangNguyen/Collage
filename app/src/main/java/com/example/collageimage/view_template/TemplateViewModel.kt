package com.example.collageimage.view_template

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.collageimage.R

class TemplateViewModel : ViewModel() {
    private val _template = MutableLiveData<TemplateModel>()
    val template: LiveData<TemplateModel> get() = _template

    private val _pathClicked = MutableLiveData<Int>()
    val pathClicked: LiveData<Int> get() = _pathClicked

    private val _selectedImage = MutableLiveData<Pair<Int, Bitmap?>>()
    val selectedImage: LiveData<Pair<Int, Bitmap?>> get() = _selectedImage

    // Tải template và các path
    fun loadTemplate(imageId: Int) {
        val template = templates.find { it.id == imageId }
        _template.value = template
    }
    private val templates = listOf(
        TemplateModel(
            id = 1,
            backgroundImageResId = R.drawable.template_05,
            stringPaths = listOf(
                "M109 108.5h502v277H109v-277Z",  // Path 1
                "M109 420h502v277H109V420Z",    // Path 2
                "M109 727h502v277H109V727Z"     // Path 3
            )
        ),
        TemplateModel(
            id = 2,
            backgroundImageResId = R.drawable.template_30,
            stringPaths = listOf(
                "M109 108.5h502v277H109v-277Z",  // Path 1
                "M109 420h502v277H109V420Z",    // Path 2
                "M109 727h502v277H109V727Z"     // Path 3
            )
        ),
        // templt 3-30
    )

    // Sự kiện khi người dùng chạm vào một path
    fun onPathClick(pathIndex: Int) {
        _pathClicked.value = pathIndex
    }

    // Cập nhật ảnh cho path
    fun updateImageForPath(pathIndex: Int, bitmap: Bitmap?) {
        _selectedImage.value = Pair(pathIndex, bitmap)
    }
}
