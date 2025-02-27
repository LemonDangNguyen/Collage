package com.photomaker.camerashot.photocollage.instacolor.permission

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.photomaker.camerashot.photocollage.instacolor.MainActivity
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.nmh.base_lib.callback.StatusResultSwitch
import com.photomaker.camerashot.photocollage.instacolor.extensions.openSettingPermission
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper

import com.photomaker.camerashot.photocollage.instacolor.helpers.FIRST_INSTALL
import com.photomaker.camerashot.photocollage.instacolor.sharepref.DataLocalManager
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivityPermissionBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeTopFullAdsBinding

class PermissionActivity : BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    val storagePer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var type = ""
    private var countStorage = 0
    private var countCamera = 0

    override fun setUp() {
        AdsConfig.loadNativeHome(this@PermissionActivity)

        setUpLayout()
        evenClick()
    }

    override fun onResume() {
        super.onResume()

        val storagePer = checkPer(storagePer)
        val cameraPer = checkPer(arrayOf(Manifest.permission.CAMERA))

        if (storagePer || cameraPer) binding.tvGo.text = getString(R.string.str_continue) else binding.tvGo.text = getString(R.string.skip)
        binding.scCamera.setCheck(cameraPer)
        binding.scStorage.setCheck(storagePer)
    }

    private fun evenClick() {

        binding.scCamera.onResult = object : StatusResultSwitch {
            override fun onResult(isChecked: Boolean) {
                if (checkPer(arrayOf(Manifest.permission.CAMERA))) {
                    binding.scCamera.setCheck(true)
                    return
                }
                if (isChecked) {
                    type = "camera"
                    binding.layoutNative.gone()
                    checkPermission.launch(arrayOf(Manifest.permission.CAMERA))
                }
            }
        }
        binding.scStorage.onResult = object : StatusResultSwitch {
            override fun onResult(isChecked: Boolean) {
                if (checkPer(storagePer)) {
                    binding.scStorage.setCheck(true)
                    return
                }
                if (isChecked) {
                    type = "storage"
                    binding.layoutNative.gone()
                    checkPermission.launch(storagePer)
                }
            }
        }

        binding.tvGo.setOnUnDoubleClickListener {
            DataLocalManager.setBoolean(FIRST_INSTALL, false)
            startIntent(MainActivity::class.java.name, true)
        }
    }

    private var checkPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when (type) {
                "camera" -> {
                    countCamera++
                    if (countCamera >= 3) {
                        binding.layoutNative.gone()
                        AppOpenManager.getInstance().disableAppResumeWithActivity(PermissionActivity::class.java)
                        openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }

                    if (AdsConfig.is_load_native_permission_camera)
                        loadNative(getString(R.string.native_permission_camera))
                    else binding.layoutNative.gone()
                }
                "storage" -> {
                    countStorage++
                    if (countStorage >= 3) {
                        binding.layoutNative.gone()
                        AppOpenManager.getInstance().disableAppResumeWithActivity(PermissionActivity::class.java)
                        openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }

                    if (AdsConfig.is_load_native_permission_storage)
                        loadNative(getString(R.string.native_permission_storage))
                    else binding.layoutNative.gone()
                }

            }
        }
    private fun setUpLayout() {
        if (AdsConfig.is_load_native_permission) {
            if (AdsConfig.isLoadFullAds())
                loadNative(getString(R.string.native_permission))
            else binding.layoutNative.gone()
        } else binding.layoutNative.gone()
    }

    private fun loadNative(strId: String) {
        if(haveNetworkConnection() && AdsConfig.isLoadFullAds() && ConsentHelper.getInstance(this).canRequestAds()) {
            binding.layoutNative.visible()
            AdsConfig.nativePermission?.let {
                pushViewNative(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(this@PermissionActivity, strId, object : NativeCallback() {
                    override fun onNativeAdLoaded(nativeAd: NativeAd) {
                        super.onNativeAdLoaded(nativeAd)
                        pushViewNative(nativeAd)
                    }

                    override fun onAdFailedToLoad() {
                        super.onAdFailedToLoad()
                        binding.frNativeAds.removeAllViews()
                    }
                })
            }
        } else binding.layoutNative.gone()
    }

    private fun pushViewNative(nativeAd: NativeAd) {
        val adView = AdsNativeTopFullAdsBinding.inflate(layoutInflater)

        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
