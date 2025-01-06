package com.example.collageimage.CustomBg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemCustomBgImageBinding

class CustomImageAdapter(
    private var images: List<CustomImage>,
    private val onClick: (CustomImage) -> Unit
) : RecyclerView.Adapter<CustomImageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCustomBgImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: CustomImage) {
            binding.imageView.setImageResource(image.resourceId)
            binding.root.setOnClickListener { onClick(image) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCustomBgImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    fun updateImages(newImages: List<CustomImage>) {
        images = newImages
        notifyDataSetChanged()
    }
}
