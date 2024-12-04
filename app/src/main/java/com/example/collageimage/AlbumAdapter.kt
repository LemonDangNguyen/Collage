package com.example.collageimage;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumAdapter(
    private val context: Context,
    private val albumList: List<AlbumModel>,
    private val onItemClick: (AlbumModel) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumImageView: ImageView = view.findViewById(R.id.img_album)
        val albumNameTextView: TextView = view.findViewById(R.id.album_name)
        val numberOfImagesTextView: TextView = view.findViewById(R.id.number_images)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albumList[position]
        Glide.with(context)
            .load(album.coverImagePath)
            .placeholder(R.drawable.noimage)
            .into(holder.albumImageView)


        holder.albumNameTextView.text = album.name
        holder.numberOfImagesTextView.text = "${album.numberOfImages} images"


        holder.itemView.setOnClickListener {
            onItemClick(album)
        }
    }

    override fun getItemCount(): Int = albumList.size
}
