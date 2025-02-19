package com.example.collageimage.permission

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivityPermissionBinding
import com.nmh.base_lib.callback.StatusResultSwitch
import com.example.collageimage.extensions.openSettingPermission
import com.example.collageimage.extensions.setOnUnDoubleClickListener

import com.nmh.base.project.helpers.FIRST_INSTALL
import com.example.collageimage.sharepref.DataLocalManager

class PermissionActivity : BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    val storagePer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var type = ""
    private var countStorage = 0
    private var countCamera = 0

    override fun setUp() {
        evenClick()
    }

    override fun onResume() {
        super.onResume()

        val storagePer = checkPer(storagePer)
        val cameraPer = checkPer(arrayOf(Manifest.permission.CAMERA))

        if (storagePer || cameraPer)
            binding.tvGo.text = getString(R.string.str_continue)
        else binding.tvGo.text = getString(R.string.skip)

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
                        openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                }
                "storage" -> {
                    countStorage++
                    if (countStorage >= 3) {
                        openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                }
            }
        }
}
