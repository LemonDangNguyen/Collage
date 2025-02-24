package com.photomaker.camerashot.photocollage.instacolor.frame

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemFrameBinding

class FrameAdapter(
    private val frameList: List<FrameItem>,
    private val onClick: (Drawable) -> Unit
) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {

    // Lưu vị trí item đã được chọn
    private var selectedPosition = -1

    inner class FrameViewHolder(val binding: ItemFrameBinding)
        : RecyclerView.ViewHolder(binding.root)

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

        // Hiển thị hoặc ẩn ImageViewSelectedIndicator dựa trên vị trí được chọn
        if (position == selectedPosition) {
            holder.binding.imageViewSelectedIndicator.visibility = View.VISIBLE
        } else {
            holder.binding.imageViewSelectedIndicator.visibility = View.GONE
        }

        // Khi người dùng click chọn item
        holder.itemView.setOnClickListener {
            drawable?.let { d ->
                onClick(d)
            }
            // Cập nhật selectedPosition và refresh
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = frameList.size
}
