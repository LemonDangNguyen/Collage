package com.example.collageimage.color

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemColorBinding

class ColorAdapter(private val colors: List<ColorItem>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: ColorItem) {
            binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color.colorHex))
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
