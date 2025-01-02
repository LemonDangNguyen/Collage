package com.example.collageimage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val context: Context,
    private val images: MutableList<ImageModel>,
    private val onItemSelected: (ImageModel, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val selectedImagesMap = mutableMapOf<Long, Int>()

    companion object {
        const val VIEW_TYPE_CAMERA = 0
        const val VIEW_TYPE_IMAGE = 1
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ifv)
        val selectionOrder: TextView = view.findViewById(R.id.selectionOrder)
    }

    inner class CameraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cameraView: ImageView = view.findViewById(R.id.ivcf_camera)
    }

    override fun getItemViewType(position: Int): Int {
        return if (images[position].isCameraItem) {
            VIEW_TYPE_CAMERA
        } else {
            VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CAMERA) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.layout_btn_camera, parent, false)
            CameraViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
            ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = images[position]

        if (holder is CameraViewHolder) {
            holder.cameraView.setOnClickListener {
                // Kiểm tra tên của Activity hiện tại để xác định nguồn
                val activityName = when (context::class.java.simpleName) {
                    "SelectActivity" -> "SelectActivity"
                    "Activity_Select_Image_Edit" -> "Activity_Select_Image_Edit"
                    else -> "Unknown"
                }

                // Tạo Intent chuyển đến ActivityCamera và truyền thông tin nguồn Activity
                val intent = Intent(context, ActivityCamera::class.java)
                intent.putExtra("source_activity", activityName)  // Truyền tên Activity nguồn vào Intent
                context.startActivity(intent)
            }
        } else if (holder is ImageViewHolder) {
            Glide.with(context).load(image.filePath).error(R.drawable.noimage).centerCrop()
                .into(holder.imageView)

            // Hiển thị thông tin chọn ảnh
            if (selectedImagesMap.containsKey(image.id)) {
                holder.selectionOrder.text = selectedImagesMap[image.id].toString()
                holder.selectionOrder.visibility = View.VISIBLE
            } else {
                holder.selectionOrder.visibility = View.GONE
            }

            // Xử lý sự kiện khi người dùng chọn ảnh
            holder.imageView.setOnClickListener {
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
            selected = false,
            uri = Uri.EMPTY,
            isCameraItem = true
        )
        images.add(0, cameraItem)
        notifyItemInserted(0)
    }
}
