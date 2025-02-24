package com.photomaker.camerashot.photocollage.instacolor.color

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemColor2Binding


class ColorPenAdapter(
    private val pencolors: List<ColorItem2>,
    private val listener: OnColorClickListener2
) : RecyclerView.Adapter<ColorPenAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(val binding: ItemColor2Binding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    pencolors.forEach { it.isSelected = false }
                    pencolors[position].isSelected = true
                    listener.onColorClick2(pencolors[position])
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(color: ColorItem2) {
            binding.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color.colorHex2))


            if (color.isSelected) {
                binding.selectedIcon.visibility = View.VISIBLE // Show the selected icon
            } else {
                binding.selectedIcon.visibility = View.INVISIBLE // Hide the selected icon
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColor2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(pencolors[position])
    }

    override fun getItemCount() = pencolors.size
}
