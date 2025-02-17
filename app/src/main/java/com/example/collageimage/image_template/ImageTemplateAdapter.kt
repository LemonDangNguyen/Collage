package com.example.collageimage.image_template

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemTemplateBinding

class ImageTemplateAdapter : RecyclerView.Adapter<ImageTemplateAdapter.ImageViewHolder>() {
    private var imageList: List<ImagetemplateModel> = listOf()
    var onItemClickListener: ((Int) -> Unit)? = null

    fun setImageList(newImageList: List<ImagetemplateModel>) {
        imageList = newImageList
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ItemTemplateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImagetemplateModel) {
            binding.imageView.setImageResource(item.imageResId)
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = imageList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
