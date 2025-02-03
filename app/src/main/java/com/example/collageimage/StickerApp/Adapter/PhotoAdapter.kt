package com.example.collageimage.StickerApp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.databinding.ItemPhotoStickerBinding
import com.example.collageimage.R

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

        if (photoPath == "drawable_static_image") {
            // Nếu là ảnh từ drawable, hiển thị bằng resource ID
            Glide.with(context)
                .load(R.drawable.ic_take_camera)
                .into(holder.binding.imageView)

            // Xử lý sự kiện click cho ảnh từ drawable
            holder.binding.root.setOnClickListener {
                Toast.makeText(context, "HIHI", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Nếu là ảnh từ thư viện, hiển thị bình thường
            Glide.with(context)
                .load(photoPath)
                .into(holder.binding.imageView)

            // Xử lý sự kiện click cho ảnh thông thường
            holder.binding.root.setOnClickListener {
                onPhotoClick(photoPath)
            }
        }
    }

    override fun getItemCount(): Int = photoList.size

    class PhotoViewHolder(val binding: ItemPhotoStickerBinding) : RecyclerView.ViewHolder(binding.root)
}
