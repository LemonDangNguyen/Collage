package com.photomaker.camerashot.photocollage.instacolor

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemImageBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.LayoutBtnCameraBinding
import java.io.File

class ImageAdapter(
    private val context: Context,
    private val images: MutableList<ImageModel>,
    private val onItemSelected: (ImageModel, Boolean) -> Unit,
    private val onCameraClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val selectedImagesMap = mutableMapOf<Long, Int>()

    companion object {
        const val VIEW_TYPE_CAMERA = 0
        const val VIEW_TYPE_IMAGE = 1
    }

    inner class ImageViewHolder(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class CameraViewHolder(val binding: LayoutBtnCameraBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (images[position].isCameraItem) {
            VIEW_TYPE_CAMERA
        } else {
            VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CAMERA) {
            val binding =
                LayoutBtnCameraBinding.inflate(LayoutInflater.from(context), parent, false)
            CameraViewHolder(binding)
        } else {
            val binding = ItemImageBinding.inflate(LayoutInflater.from(context), parent, false)
            ImageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = images[position]

        if (holder is CameraViewHolder) {
            holder.binding.ivcfCamera.setOnClickListener {
                onCameraClick()
            }
        } else if (holder is ImageViewHolder) {
            val imageUri = getImageUriFromFilePath(image.filePath)
            Glide.with(context)
                .load(imageUri)
              //  .error(R.drawable.noimage)
                .centerCrop()
              //  .placeholder(R.drawable.noimage)
                .into(holder.binding.ifv)


            if (selectedImagesMap.containsKey(image.id)) {
                holder.binding.selectionOrder.text = selectedImagesMap[image.id].toString()
                holder.binding.selectionOrder.visibility = View.VISIBLE
            } else {
                holder.binding.selectionOrder.visibility = View.GONE
            }

            holder.binding.ifv.setOnClickListener {
                val isSelected = !selectedImagesMap.containsKey(image.id)
                onItemSelected(image, isSelected)
            }
        }
    }

    override fun getItemCount(): Int = images.size

    fun updateSelection(selectedImages: List<ImageModel>) {
        selectedImagesMap.clear()
        selectedImages.forEachIndexed { index, image ->
            selectedImagesMap[image.id] = index + 1
        }
        notifyDataSetChanged()
    }

    fun addCameraItem() {
        val cameraItem = ImageModel(
            id = -1L,
            dateTaken = System.currentTimeMillis(),
            fileName = "Camera",
            filePath = "",
            album = "",
            uri = Uri.EMPTY,
            isCameraItem = true
        )
        images.add(0, cameraItem)
        notifyItemInserted(0)
    }

    private fun getImageUriFromFilePath(filePath: String): Uri {
        val file = File(filePath)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DATA} = ?"
            val selectionArgs = arrayOf(filePath)

            val cursor = context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
            return if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val uri = ContentUris.withAppendedId(contentUri, id)
                cursor.close()
                uri
            } else {
                Uri.parse(filePath)
            }
        } else {
            Uri.fromFile(file)
        }
    }
}

