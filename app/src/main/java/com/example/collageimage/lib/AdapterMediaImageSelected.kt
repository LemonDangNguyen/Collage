package com.hypersoft.puzzlelayouts.app.features.media.presentation.images.adapter.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.ImageModel
import com.example.collageimage.databinding.ItemSelectedImageBinding
import com.example.selectpic.lib.setImageFromUri

class AdapterMediaImageSelected() : ListAdapter<ImageModel, AdapterMediaImageSelected.CustomViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSelectedImageBinding.inflate(layoutInflater, parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentItem = getItem(position)
        bindViews(holder, currentItem)
        holder.binding.root.setOnClickListener {
        }
    }

    private fun bindViews(holder: CustomViewHolder, currentItem: ImageModel) {
        holder.binding.ifv.setImageFromUri(currentItem.uri)
    }

    inner class CustomViewHolder(val binding: ItemSelectedImageBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<ImageModel>() {
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem == newItem
        }
    }
}