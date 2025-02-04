package com.example.collageimage

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.collageimage.databinding.BottomSheetDialogCameraBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import java.io.File

class BottomSheetDialogCameraSticker : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraView: CameraView
    private var photoPath: String? = null
    var onDone: ((String) -> Unit)? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDialogCameraBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        cameraView = binding.cameraView
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                val photoFile = File(
                    requireContext().getExternalFilesDir(null),
                    "photo_${System.currentTimeMillis()}.jpg"
                )
                result.toFile(photoFile) { file ->
                    file?.let {
                        photoPath = it.absolutePath
                        binding.ivThumb.setImageURI(Uri.parse("file://${it.absolutePath}"))
                        binding.tvTotalImage.text = "1"
                        binding.tvTotalImage.visibility = View.VISIBLE
                        binding.ivDone.visibility = View.VISIBLE
                        binding.ivCapture.visibility = View.GONE // Ẩn nút chụp sau khi chụp
                        Toast.makeText(context, "Bạn chỉ có thể chụp 1 ảnh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        binding.ivClose.setOnClickListener { dismiss() }
        binding.ivFlash.setOnClickListener { toggleFlash() }
        binding.ivCapture.setOnClickListener { takePicture() }
        binding.ivDone.setOnClickListener { onDoneClicked() }
    }

    override fun onStart() {
        super.onStart()
        cameraView.open()
    }
    override fun onStop() {
        super.onStop()
        cameraView.close()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupFullHeight() {
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
            bottomSheetDialog?.let { bsd ->
                val bottomSheet = bsd.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    val behavior = BottomSheetBehavior.from(sheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                    behavior.isDraggable = false

                    val layoutParams = sheet.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    sheet.layoutParams = layoutParams
                }
            }
        }
    }
    private fun takePicture() {
        if (photoPath != null) {
            Toast.makeText(requireContext(), "Bạn chỉ có thể chụp 1 ảnh", Toast.LENGTH_SHORT).show()
            return
        }
        cameraView.takePictureSnapshot()
    }
    private fun toggleFlash() {
        when (cameraView.flash) {
            Flash.OFF -> {
                cameraView.flash = Flash.TORCH
                binding.ivFlash.setImageResource(R.drawable.ic_flash)
            }
            Flash.TORCH -> {
                cameraView.flash = Flash.OFF
                binding.ivFlash.setImageResource(R.drawable.ic_no_flash)
            }
            else -> {
                cameraView.flash = Flash.OFF
                binding.ivFlash.setImageResource(R.drawable.ic_no_flash)
            }
        }
    }
    private fun onDoneClicked() {
        photoPath?.let { path ->
            onDone?.invoke(path)
        }
        dismiss()
    }
    companion object {
        fun newInstance(): BottomSheetDialogCameraSticker {
            return BottomSheetDialogCameraSticker()
        }
    }
}