package com.nmh.base_lib.callback

import android.view.View

interface OnRangeSeekbarResult {
    fun onDown(v: View)
    fun onMove(v: View, start: Int, end: Int)
    fun onUp(v: View, start: Int, end: Int)
}