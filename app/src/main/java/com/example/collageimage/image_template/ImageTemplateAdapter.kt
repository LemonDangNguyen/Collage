package com.example.collageimage.image_template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.R

class ImageTemplateAdapter(private val imageList: List<ImagetemplateModel>) : RecyclerView.Adapter<ImageTemplateAdapter.ImageViewHolder>() {

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
        val item = imageList[position]
        holder.imageView.setImageResource(item.imageResId)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
