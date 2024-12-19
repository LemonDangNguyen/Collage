package com.example.collageimage.ratio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.R

class RatioAdapter(
    private val items: List<Triple<String, Int, Float>>, // Label, Image ID, Ratio
    private val onItemClick: (Float) -> Unit
) : RecyclerView.Adapter<RatioAdapter.RatioViewHolder>() {

    private var selectedIndex = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ratio_image_text, parent, false)
        return RatioViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatioViewHolder, position: Int) {
        val (label, imageResId, ratio) = items[position]
        holder.bind(label, imageResId, ratio, position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class RatioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.ratioImage)
        private val textView: TextView = view.findViewById(R.id.ratioText)

        fun bind(label: String, imageResId: Int, ratio: Float, isSelected: Boolean) {
            textView.text = label
            imageView.setImageResource(imageResId)
            imageView.isSelected = isSelected
            imageView.imageTintList = ContextCompat.getColorStateList(imageView.context, R.color.ratio_icon_color)

            itemView.setOnClickListener {
                onItemClick(ratio)
                notifyItemChanged(selectedIndex)
                selectedIndex = adapterPosition
                notifyItemChanged(selectedIndex)
            }
        }
    }
}
