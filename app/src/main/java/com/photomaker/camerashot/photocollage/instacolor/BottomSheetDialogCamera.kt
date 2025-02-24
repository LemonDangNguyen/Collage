package com.photomaker.camerashot.photocollage.instacolor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.photomaker.camerashot.photocollage.instacolor.utils.Utils.showToast
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import com.photomaker.camerashot.photocollage.instacolor.ActivityEditImage
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.BottomSheetDialogCameraBinding
import java.io.File

class BottomSheetDialogCamera : BottomSheetDialogFragment() {
    interface OnImagesCapturedListener {
        fun onImagesCaptured(images: ArrayList<ImageModel>)
    }
    private var listener: OnImagesCapturedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnImagesCapturedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnImagesCapturedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    private var _binding: BottomSheetDialogCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraView: CameraView
    private val photoList = mutableListOf<String>()
    private var maxPhotos = Int.MAX_VALUE
    private var sourceActivity: String? = null

    private val cameraListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val photoFile = File(
                requireContext().getExternalFilesDir(null),
                "photo_${photoList.size + 1}_${System.currentTimeMillis()}.jpg"
            )
            result.toFile(photoFile) { file ->
                file?.let {
                    photoList.add(it.absolutePath)
                    updateThumbnail(it.absolutePath)
                    binding.tvTotalImage.text = "${photoList.size}"
                    binding.tvTotalImage.visibility = View.VISIBLE
                    binding.ivDone.visibility = View.VISIBLE

                    if (photoList.size >= maxPhotos) {

                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetDialogCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
            dialog?.let {
                val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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

        sourceActivity = arguments?.getString("source_activity")
        Log.d("BottomSheetCamera", "Activity nguồn: $sourceActivity")

        maxPhotos = if (sourceActivity == "ActivitySelectImageEdit") 1 else 9
        photoList.clear()

        cameraView = binding.cameraView
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.addCameraListener(cameraListener)

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

    private fun takePicture() {
        if (photoList.size >= maxPhotos) {
            showToast(requireContext(),"${getString(R.string.maximunpic )} $maxPhotos" , Gravity.CENTER)
            return
        }
        cameraView.takePictureSnapshot()
    }

    private fun updateThumbnail(photoPath: String) {
        binding.ivThumb.setImageURI(Uri.parse("file://$photoPath"))
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
        if (photoList.isNotEmpty()) {
            if (sourceActivity == "ActivitySelectImageEdit") {
                // Xử lý khi sourceActivity là ActivitySelectImageEdit (không thay đổi)
                val intent = Intent(requireContext(), ActivityEditImage::class.java)
                val selectedImage = photoList[0]
                val selectedImages = listOf(
                    ImageModel(
                        id = System.currentTimeMillis(),
                        dateTaken = System.currentTimeMillis(),
                        fileName = File(selectedImage).name,
                        filePath = selectedImage,
                        album = "Camera",
                        uri = Uri.parse("file://$selectedImage"),
                        isCameraItem = true
                    )
                )
                intent.putParcelableArrayListExtra("IMG_FROM_CAM", ArrayList(selectedImages))
                startActivity(intent)
                dismiss()
            } else if (sourceActivity == "SelectActivity") {
                // Trường hợp sourceActivity là SelectActivity
                val selectedImages = photoList.map {
                    ImageModel(
                        id = System.currentTimeMillis(),
                        dateTaken = System.currentTimeMillis(),
                        fileName = File(it).name,
                        filePath = it,
                        album = "Camera",
                        uri = Uri.parse("file://$it"),
                        isCameraItem = true
                    )
                }
                listener?.onImagesCaptured(ArrayList(selectedImages))
                dismiss()
            } else if (sourceActivity == "SelectImageTemplate") {
                // Sửa nhánh này: thay vì khởi chạy TemplateActivity trực tiếp,
                // trả về dữ liệu cho activity gọi (SelectImageTemplate)
                val selectedImage = photoList[0]
                val selectedImages = listOf(
                    ImageModel(
                        id = System.currentTimeMillis(),
                        dateTaken = System.currentTimeMillis(),
                        fileName = File(selectedImage).name,
                        filePath = selectedImage,
                        album = "Camera",
                        uri = Uri.parse("file://$selectedImage"),
                        isCameraItem = true
                    )
                )
                listener?.onImagesCaptured(ArrayList(selectedImages))
                dismiss()
            } else {
                // Phần else hiện tại
                val intent = Intent(requireContext(), SelectActivity::class.java)
                val selectedImages = photoList.map {
                    ImageModel(
                        id = System.currentTimeMillis(),
                        dateTaken = System.currentTimeMillis(),
                        fileName = File(it).name,
                        filePath = it,
                        album = "Camera",
                        uri = Uri.parse("file://$it"),
                        isCameraItem = true
                    )
                }
                intent.putParcelableArrayListExtra("IMG_FROM_CAM", ArrayList(selectedImages))
                startActivity(intent)
                dismiss()
            }
        } else {
            showToast(requireContext(),getString(R.string.capture_no), Gravity.CENTER)
        }
    }




    companion object {
        fun newInstance(sourceActivity: String?): BottomSheetDialogCamera {
            val fragment = BottomSheetDialogCamera()
            val args = Bundle()
            args.putString("source_activity", sourceActivity)
            fragment.arguments = args
            return fragment
        }
    }
}
