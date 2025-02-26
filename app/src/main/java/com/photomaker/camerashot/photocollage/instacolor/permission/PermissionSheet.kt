package com.photomaker.camerashot.photocollage.instacolor.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.photomaker.camerashot.photocollage.instacolor.extensions.checkAllPerGrand
import com.photomaker.camerashot.photocollage.instacolor.extensions.checkPer
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.openSettingPermission
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.showToast
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.BottomSheetDialogPermissionBinding
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class PermissionSheet @Inject constructor(@ActivityContext private val context: Context): BottomSheetDialog(context, R.style.SheetDialog) {
    private var binding = BottomSheetDialogPermissionBinding.inflate(LayoutInflater.from(context))

    var isDone: ICallBackCheck? = null
    var isDismiss: ICallBackCheck? = null
    private var nativeAds: NativeAd? = null

    private val storagePer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)


    init {
        setContentView(binding.root)
        setCancelable(true)

        binding.root.layoutParams.width = (NMHApp.w * 100).toInt()



        initView()
        evenClick()

        setOnCancelListener {
            if (context.checkAllPerGrand()) isDone?.check(true)
            isDismiss?.check(true)
        }
    }

    fun showDialog() {
        show()
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window?.navigationBarColor = Color.WHITE
        window?.statusBarColor = Color.TRANSPARENT
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        checkPer()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        binding.sc.apply {
            setCheck(true)
            isEnabled = false
        }
        loadNative()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun evenClick() {
        binding.tvStorage.setOnUnDoubleClickListener {
            if (binding.tvStorage.currentTextColor == Color.BLACK)
                return@setOnUnDoubleClickListener

            if (!context.checkPer(storagePer)) {
                binding.rlNative.gone()
                requestStoragePermission()
            }
        }

        binding.tvCamera.setOnUnDoubleClickListener {
            if (binding.tvCamera.currentTextColor == Color.BLACK)
                return@setOnUnDoubleClickListener

            if (!context.checkPer(arrayOf(Manifest.permission.CAMERA))) {
                binding.rlNative.gone()
                requestCameraPermission()
            }
        }

        binding.rl.setOnUnDoubleClickListener {
            context.showToast(context.getString(R.string.click_to_step_by_step_for_require_permission), Gravity.CENTER)
        }
        binding.ivExit.setOnUnDoubleClickListener { cancel() }
    }


    @SuppressLint("SetTextI18n")
    fun checkPer(): Boolean {
        binding.tvCamera.apply {
            setBackgroundResource(R.drawable.bg_button_disable)
            setTextColor(Color.BLACK)
            clearAnimation()
            elevation = 0f
        }

        binding.tvStorage.apply {
            setBackgroundResource(R.drawable.bg_button_disable)
            setTextColor(Color.BLACK)
            clearAnimation()
            elevation = 0f
        }

        return if (!context.checkPer(arrayOf(Manifest.permission.CAMERA))) {
            binding.tvPer.text = "${context.getString(R.string.you_can_select)} ${context.getString(R.string.str_camera)}"
            binding.tvCamera.apply {
                setBackgroundResource(R.drawable.bg_button_enable)
                setTextColor(Color.WHITE)
                startAnimationButton(this)
                elevation = 1f
            }
            false
        } else if (!context.checkPer(storagePer)) {
            binding.tvPer.text = "${context.getString(R.string.you_can_select)} ${context.getString(R.string.str_storage)}"
            binding.tvStorage.apply {
                setBackgroundResource(R.drawable.bg_button_enable)
                setTextColor(Color.WHITE)
                startAnimationButton(this)
                elevation = 1f
            }
            false
        } else {
            cancel()
            true
        }
    }

    private fun startAnimationButton(view: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.heart_beat))
        }, 2000)
    }

    private fun requestCameraPermission() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse) {
                    binding.rlNative.visible()
                    if (checkPer()) cancel()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse) {
                    checkPer()
                    if (p0.isPermanentlyDenied) {
                        AppOpenManager.getInstance().disableAppResumeWithActivity(context::class.java)
                        context.openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken
                ) {
                    p1.continuePermissionRequest()
                }

            }).check()
    }

    private fun requestStoragePermission() {
        Dexter.withContext(context)
            .withPermissions(*storagePer)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                    binding.rlNative.visible()
                    if (p0.isAnyPermissionPermanentlyDenied) {
                        AppOpenManager.getInstance().disableAppResumeWithActivity(context::class.java)
                        context.openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken
                ) {
                    p1.continuePermissionRequest()
                }


            }).check()
    }

    fun checkPerDialog(): Boolean = context.checkAllPerGrand()
    fun loadNative() {
        try {
            if (AdsConfig.haveNetworkConnection(context) && AdsConfig.is_load_native_popup_permission
                && ConsentHelper.getInstance(context).canRequestAds()) {
                binding.rlNative.visible()
                nativeAds?.let {
                    pushViewAds(it)
                } ?: run {
                    Admob.getInstance().loadNativeAd(
                        context,
                        context.getString(R.string.native_permission_in_app),
                        object : NativeCallback() {
                            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                                nativeAds = nativeAd
                                pushViewAds(nativeAd)
                            }

                            override fun onAdFailedToLoad() {
                                binding.frNativeAds.removeAllViews()
                            }

                            override fun onAdImpression() {
                                super.onAdImpression()
                                nativeAds = null
                            }
                        }
                    )
                }
            } else binding.rlNative.gone()
        } catch (e: Exception) {
            binding.rlNative.gone()
            e.printStackTrace()
        }
    }

    private fun pushViewAds(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)
        if (AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)

        binding.rlNative.visible()
        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}