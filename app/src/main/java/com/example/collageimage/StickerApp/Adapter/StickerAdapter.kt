package com.example.teststicker.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemStickerBinding


class StickerAdapter(
    private var stickers: List<String>
) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStickerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stickerPath = stickers[position]
        val context = holder.binding.root.context
        val assetManager = context.assets

        try {
            val inputStream = assetManager.open(stickerPath)
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.binding.ivSticker.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = stickers.size

    fun updateData(newStickers: List<String>) {
        stickers = newStickers
        notifyDataSetChanged()
    }
}
