package com.example.collageimage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class TemplateAdapter(private val imageList: List<Int>) : RecyclerView.Adapter<TemplateAdapter.ImageViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(imageList[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(imageList[position])  // Gửi ID ảnh khi nhấn vào
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
