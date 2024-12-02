
package com.example.collageimage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectActivityViewModel : ViewModel() {
    val selectedImages = MutableLiveData<List<ImageModel>>()
}
