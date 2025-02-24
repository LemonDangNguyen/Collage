package com.photomaker.camerashot.photocollage.instacolor

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photomaker.camerashot.photocollage.instacolor.AlbumModel
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemAlbumBinding
import java.io.File

class AlbumAdapter(
    private val context: Context,
    private val albumList: List<AlbumModel>,
    private val onItemClick: (AlbumModel) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(album: AlbumModel) {
            val imageUri = getImageUriFromFilePath(album.coverImagePath)

            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.noimage)
                .into(binding.imgAlbum)

            binding.albumName.text = album.name
            binding.numberImages.text = "${album.numberOfImages} ${binding.root.context.getString(R.string.image_count)}"
            itemView.setOnClickListener {
                onItemClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albumList[position]
        holder.bind(album)
    }

    override fun getItemCount(): Int = albumList.size

    // Hàm chuyển file path thành Uri với điều kiện kiểm tra phiên bản Android
    private fun getImageUriFromFilePath(filePath: String): Uri {
        val file = File(filePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DATA} = ?"
            val selectionArgs = arrayOf(filePath)
            val cursor = context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val uri = ContentUris.withAppendedId(contentUri, id)
                cursor.close()
                uri
            } else {
                Uri.parse(filePath)
            }
        } else {
            Uri.fromFile(file)
        }
    }
}
