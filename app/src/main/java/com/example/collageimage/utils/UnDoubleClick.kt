package com.nmh.base.project.utils

import android.os.SystemClock
import android.view.View

class UnDoubleClick(
    private var defaultInterval: Int = 1000,
    private val onUnDoubleCLick: (View) -> Unit
) : View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) return

        lastTimeClicked = SystemClock.elapsedRealtime()
        onUnDoubleCLick(v)
    }
}