package com.photomaker.camerashot.photocollage.instacolor.Gradient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemGradientBgBinding


class GradientAdapter(
    private var gradients: List<GradientItem>,
    private val onClick: (GradientItem) -> Unit
) : RecyclerView.Adapter<GradientAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGradientBgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Hàm bind dữ liệu GradientItem vào UI
        fun bind(gradient: GradientItem) {
            binding.imageView.setBackgroundResource(gradient.resourceId)
            binding.root.setOnClickListener { onClick(gradient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemGradientBgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(gradients[position])
    }

    override fun getItemCount() = gradients.size

    fun updateGradients(newGradients: List<GradientItem>) {
        gradients = newGradients
        notifyDataSetChanged()
    }
}
