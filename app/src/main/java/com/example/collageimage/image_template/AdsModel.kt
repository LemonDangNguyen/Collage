package com.example.collageimage.image_template

import com.google.android.gms.ads.nativead.NativeAd

data class AdsModel (
    var id: Int = -1,
    var nativeAd: NativeAd? = null,
    var strId: String = "",
    var isLoaded: Boolean = false,
    var keyRemote: Boolean =true

)
