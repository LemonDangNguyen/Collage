package com.example.collageimage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

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
        // Lấy Uri của ảnh bìa dựa trên file path với điều kiện phiên bản Android
        val imageUri = getImageUriFromFilePath(album.coverImagePath)

        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.noimage)
            .into(holder.albumImageView)

        holder.albumNameTextView.text = album.name
        holder.numberOfImagesTextView.text = "${album.numberOfImages} images"

        holder.itemView.setOnClickListener {
            onItemClick(album)
        }
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
