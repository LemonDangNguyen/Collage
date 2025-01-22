package com.nmh.base_lib.callback

import android.view.View

interface OnSeekbarResult {
    fun onDown(v: View)
    fun onMove(v: View, value: Int)
    fun onUp(v: View, value: Int)
}