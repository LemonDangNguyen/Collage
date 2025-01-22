package com.nmh.base.project.utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CustomEditTextWithBackPressEvent(context: Context?, attrs: AttributeSet?) : AppCompatEditText(
    context!!, attrs
) {
    var onBackPressListener: MyEditTextListener? = null

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            //back button pressed
            if (ViewCompat.getRootWindowInsets(rootView)?.isVisible(WindowInsetsCompat.Type.ime()) == false) {
                //keyboard is close
                onBackPressListener?.callback()
            }
            return false
        }
        return super.dispatchKeyEvent(event)
    }

    interface MyEditTextListener {
        fun callback()
    }
}