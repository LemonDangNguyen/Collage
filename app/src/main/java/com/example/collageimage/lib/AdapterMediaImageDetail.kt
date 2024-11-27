package com.hypersoft.puzzlelayouts.app.features.media.presentation.images.adapter.recyclerView

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemImageBinding
import com.example.selectpic.ImageModel

import com.example.selectpic.ddat.setImageFromUri

class AdapterMediaImageDetail(private val itemClick: (imageUri: Uri) -> Unit) : ListAdapter<ImageModel, AdapterMediaImageDetail.CustomViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(layoutInflater, parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentItem = getItem(position)
        bindViews(holder, currentItem)

        holder.binding.root.setOnClickListener {
            itemClick.invoke(currentItem.uri)
        }
    }

    private fun bindViews(holder: CustomViewHolder, currentItem: ImageModel) {
        holder.binding.ifv.setImageFromUri(currentItem.uri)
    }

    inner class CustomViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<ImageModel>() {
        override fun areItemsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: ImageModel, newItem: ImageModel): Boolean {
            return oldItem == newItem
        }
    }
}