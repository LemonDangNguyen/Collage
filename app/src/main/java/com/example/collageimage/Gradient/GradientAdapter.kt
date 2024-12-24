package com.example.collageimage.Gradient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemGradientBgBinding

class GradientAdapter(private val gradients: List<GradientItem>, private val onClick: (GradientItem) -> Unit) : RecyclerView.Adapter<GradientAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemGradientBgBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gradient: GradientItem, onClick: (GradientItem) -> Unit) {
            binding.imageView.setBackgroundResource(gradient.resourceId)
            binding.root.setOnClickListener { onClick(gradient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGradientBgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(gradients[position], onClick)
    }

    override fun getItemCount() = gradients.size
}
