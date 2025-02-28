package com.photomaker.camerashot.photocollage.instacolor.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.photomaker.camerashot.photocollage.instacolor.R

abstract class BaseDialog<VB : ViewBinding>(
    val bidingFactory: (LayoutInflater) -> VB,
    private val context: Context,
    themeResId: Int = R.style.ThemeDialog) : Dialog(context, themeResId) {

    val mBinding: VB by lazy { bidingFactory(layoutInflater) }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window?.navigationBarColor = Color.parseColor("#01ffffff")
        window?.statusBarColor = Color.TRANSPARENT
        window?.decorView?.systemUiVisibility = hideSystemBars()
        createContentView()
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        onResizeViews()
        onClickViews()
    }

    private fun createContentView() {
        setContentView(mBinding.root)
    }

    open fun initViews() {}

    open fun onResizeViews() {}

    open fun onClickViews() {}


    fun setDialogBottom() {
        window?.run {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
        }
    }

    private fun hideSystemBars(): Int {
        return (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}