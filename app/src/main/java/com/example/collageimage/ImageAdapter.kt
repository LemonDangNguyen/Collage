package com.example.collageimage;

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val context: Context,
    private val images: List<ImageModel>,
    private val onItemSelected: (ImageModel, Boolean) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val selectedImagesMap = mutableMapOf<Long, Int>() // Lưu thứ tự chọn theo id ảnh

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ifv)
        val selectionOrder: TextView = view.findViewById(R.id.selectionOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]

        // Hiển thị ảnh bằng Glide
        Glide.with(context).load(image.filePath).error(R.drawable.noimage).centerCrop().into(holder.imageView)

        // Hiển thị thứ tự chọn (nếu đã chọn)
        if (selectedImagesMap.containsKey(image.id)) {
            holder.selectionOrder.text = selectedImagesMap[image.id].toString()
            holder.selectionOrder.visibility = View.VISIBLE
        } else {
            holder.selectionOrder.visibility = View.GONE
        }

        // Xử lý sự kiện khi ảnh được chọn/bỏ chọn
        holder.imageView.setOnClickListener {
            val isSelected = !selectedImagesMap.containsKey(image.id)
            onItemSelected(image, isSelected)
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
}
