package com.example.collageimage.StickerApp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.databinding.ItemPhotoStickerBinding

class PhotoAdapter(
    private val context: Context,
    private val photoList: List<String>,
    private val onPhotoClick: (String) -> Unit // Callback khi click vào ảnh
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoStickerBinding.inflate(LayoutInflater.from(context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoPath = photoList[position]
        Glide.with(context)
            .load(photoPath)
            .into(holder.binding.imageView)

        // Xử lý sự kiện click
        holder.binding.root.setOnClickListener {
            onPhotoClick(photoPath)  // Gọi callback khi ảnh được chọn
        }
    }

    override fun getItemCount(): Int = photoList.size

    class PhotoViewHolder(val binding: ItemPhotoStickerBinding) : RecyclerView.ViewHolder(binding.root)
}