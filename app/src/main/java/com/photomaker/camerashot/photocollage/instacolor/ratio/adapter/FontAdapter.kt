package com.photomaker.camerashot.photocollage.instacolor.ratio.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemFontBinding

class FontAdapter(
    private val fontList: List<String>,
    private val context: Context,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FontAdapter.FontViewHolder>() {

    inner class FontViewHolder(val binding: ItemFontBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fontName: String) {
            val typeface = Typeface.createFromAsset(context.assets, "font/$fontName")
            binding.tvFontName.typeface = typeface

            // Xử lý sự kiện click
            binding.root.setOnClickListener { onItemClick(fontName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val binding = ItemFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FontViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        holder.bind(fontList[position])
    }

    override fun getItemCount(): Int = fontList.size
}
