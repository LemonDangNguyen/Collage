package com.photomaker.camerashot.photocollage.instacolor.image_template

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.R

class ImageTemplateViewModel : ViewModel() {
    private val _imageList = MutableLiveData<MutableList<Any>>()
    val imageList: LiveData<MutableList<Any>> get() = _imageList
    init {
        loadImages()
    }
    private fun loadImages() {
        val imageResIds = mutableListOf<Any>()
        imageResIds.add(ImageTemplateModel(1, R.drawable.templatee01))
        imageResIds.add(ImageTemplateModel(2, R.drawable.templatee02))
        imageResIds.add(ImageTemplateModel(3, R.drawable.templatee03))
        imageResIds.add(ImageTemplateModel(4, R.drawable.templatee04))
        imageResIds.add(ImageTemplateModel(5, R.drawable.templatee05))
        imageResIds.add(ImageTemplateModel(6, R.drawable.templatee06))
        imageResIds.add(ImageTemplateModel(7, R.drawable.templatee07))
        imageResIds.add(ImageTemplateModel(8, R.drawable.templatee08))
        imageResIds.add(ImageTemplateModel(9, R.drawable.templatee09))
        imageResIds.add(ImageTemplateModel(10, R.drawable.templatee10))
        imageResIds.add(ImageTemplateModel(11, R.drawable.templatee11))
        imageResIds.add(ImageTemplateModel(12, R.drawable.templatee12))
        imageResIds.add(ImageTemplateModel(13, R.drawable.templatee13))
        imageResIds.add(ImageTemplateModel(14, R.drawable.templatee14))
        imageResIds.add(ImageTemplateModel(15, R.drawable.templatee15))
        imageResIds.add(ImageTemplateModel(16, R.drawable.templatee16))
        imageResIds.add(ImageTemplateModel(17, R.drawable.templatee17))
        imageResIds.add(ImageTemplateModel(18, R.drawable.templatee18))
        imageResIds.add(ImageTemplateModel(19, R.drawable.templatee19))
        imageResIds.add(ImageTemplateModel(20, R.drawable.templatee20))
        imageResIds.add(ImageTemplateModel(21, R.drawable.templatee21))
        imageResIds.add(ImageTemplateModel(22, R.drawable.templatee22))
        imageResIds.add(ImageTemplateModel(23, R.drawable.templatee23))
        imageResIds.add(ImageTemplateModel(24, R.drawable.templatee24))
        imageResIds.add(ImageTemplateModel(25, R.drawable.templatee25))
        imageResIds.add(ImageTemplateModel(26, R.drawable.templatee26))
        imageResIds.add(ImageTemplateModel(27, R.drawable.templatee27))
        imageResIds.add(ImageTemplateModel(28, R.drawable.templatee28))
        imageResIds.add(ImageTemplateModel(29, R.drawable.templatee29))
        imageResIds.add(ImageTemplateModel(30, R.drawable.templatee30))

        var pos = -4
        var isCheck = false

        while (pos< imageResIds.size){
            pos+= if (!isCheck) 5 else if (AdsConfig.is_load_native_item_template3) 5 else 4
            if (imageResIds.size>= pos +1 && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_item_template1){
                imageResIds.add(pos, AdsModel(pos, null, NMHApp.ctx.getString(R.string.native_item_template1), false, AdsConfig.is_load_native_item_template1))
            }

            pos += if (AdsConfig.is_load_native_item_template1) 5 else 4
            if (imageResIds.size>= pos +1 && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_item_template2)
                imageResIds.add(pos, AdsModel(pos, null, NMHApp.ctx.getString(R.string.native_item_template2), false, AdsConfig.is_load_native_item_template2))

            pos += if (AdsConfig.is_load_native_item_template2) 5 else 4
            if (imageResIds.size>= pos +1 && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_item_template3)
                imageResIds.add(pos, AdsModel(pos, null, NMHApp.ctx.getString(R.string.native_item_template3), false, AdsConfig.is_load_native_item_template3))
            isCheck = true
        }

        _imageList.value = imageResIds
    }

}
