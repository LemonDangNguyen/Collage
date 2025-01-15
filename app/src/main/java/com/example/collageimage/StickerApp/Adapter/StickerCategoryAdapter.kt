package com.example.teststicker.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.R
import com.example.collageimage.databinding.ItemCategoryBinding


class StickerCategoryAdapter(
    private val categories: Map<String, List<String>>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<StickerCategoryAdapter.ViewHolder>() {

    private var selectedPosition = 0 // Mặc định chọn vị trí đầu tiên

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryName = categories.keys.toList()[position]
        val firstStickerPath = categories[categoryName]?.firstOrNull()

        // Hiển thị hình ảnh đầu tiên của danh mục
        if (firstStickerPath != null) {
            val context = holder.binding.root.context
            val assetManager = context.assets
            try {
                val inputStream = assetManager.open(firstStickerPath)
                val drawable = Drawable.createFromStream(inputStream, null)
                holder.binding.ivCategoryIcon.setImageDrawable(drawable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Cập nhật trạng thái "được chọn"
        holder.binding.root.setBackgroundResource(
            if (position == selectedPosition) R.drawable.rounded_border else android.R.color.transparent
        )

        // Xử lý sự kiện click
        holder.binding.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition) // Cập nhật trạng thái của item cũ
            notifyItemChanged(selectedPosition) // Cập nhật trạng thái của item mới
            onCategoryClick(categoryName)
        }
    }

    override fun getItemCount() = categories.size

    // Đặt danh mục được chọn
    fun setSelectedCategory(category: String) {
        val position = categories.keys.toList().indexOf(category)
        if (position != -1) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}

