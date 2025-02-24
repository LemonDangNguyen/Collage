package com.photomaker.camerashot.photocollage.instacolor.callback

import com.nmh.base_lib.callback.ICallBackCheck
import com.nmh.base_lib.callback.ICallBackItem

interface ICallBackDimensional {
    fun callBackItem(objects: Any, callBackItem: ICallBackItem)

    fun callBackCheck(objects: Any, check: ICallBackCheck)
}