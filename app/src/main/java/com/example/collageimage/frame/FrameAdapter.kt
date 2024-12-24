package com.example.collageimage.frame

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemFrameBinding
import java.io.IOException

class FrameAdapter(private val frameList: List<FrameItem>, private val onClick: (Drawable) -> Unit) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {

    class FrameViewHolder(val binding: ItemFrameBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val binding = ItemFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FrameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val frame = frameList[position]
        val assetManager = holder.itemView.context.assets
        val inputStream = assetManager.open(frame.framePath)
        val drawable = Drawable.createFromStream(inputStream, null)
        holder.binding.imageViewFrame.setImageDrawable(drawable)
        holder.itemView.setOnClickListener { drawable?.let { it1 -> onClick(it1) } } // Gửi Drawable khi click
    }

    override fun getItemCount() = frameList.size
}


