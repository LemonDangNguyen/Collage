package com.example.collageimage.color

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemColorBinding

class ColorAdapter(private val colors: List<ColorItem>, private val listener: OnColorClickListener) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    colors.forEach { it.isSelected = false }
                    colors[position].isSelected = true
                    listener.onColorClick(colors[position])
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(color: ColorItem) {
            binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color.colorHex))

            if (color.isSelected) {
                binding.selectedIcon.visibility = View.VISIBLE // Show the selected icon
            } else {
                binding.selectedIcon.visibility = View.INVISIBLE // Hide the selected icon
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount() = colors.size
}
