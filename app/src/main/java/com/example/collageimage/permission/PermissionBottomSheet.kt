package com.example.collageimage.permission

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import com.example.collageimage.databinding.BottomSheetDialogPermissionBinding
import com.example.collageimage.extensions.openSettingPermission
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class PermissionBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetDialogPermissionBinding

    var storagePermissions: Array<String> = emptyArray()
    var onPermissionGranted: (() -> Unit)? = null

    private var isStoragePermissionGranted = false
    private var isCameraPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetDialogPermissionBinding.inflate(inflater, container, false)
        binding.tvStorage.setOnClickListener {
            requestStoragePermission()
        }
        binding.tvCamera.setOnClickListener {
            requestCameraPermission()
        }

        return binding.root
    }

    private fun requestStoragePermission() {
        Dexter.withContext(requireContext())
            .withPermissions(*storagePermissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    isStoragePermissionGranted = report.areAllPermissionsGranted()
                    checkPermissionsAndProceed()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun requestCameraPermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    isCameraPermissionGranted = true
                    checkPermissionsAndProceed()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response?.isPermanentlyDenied == true) {
                        context?.openSettingPermission(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun checkPermissionsAndProceed() {
        if (isStoragePermissionGranted && isCameraPermissionGranted) {
            onPermissionGranted?.invoke()
            dismiss()
        }
    }
}
