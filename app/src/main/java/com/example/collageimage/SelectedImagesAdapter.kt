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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.R
import java.io.File

class SelectedImagesAdapter(
    private val context: Context,
    private var selectedImages: MutableList<ImageModel>,
    private val onRemoveImage: (ImageModel) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.SelectedImageViewHolder>() {

    inner class SelectedImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ifv)
        val deleteButton: ImageView = view.findViewById(R.id.ic_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_image, parent, false)
        return SelectedImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedImageViewHolder, position: Int) {
        val image = selectedImages[position]

        // Sử dụng điều kiện kiểm tra phiên bản Android để lấy Uri phù hợp
        val imageUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getImageUriFromFilePath(image.filePath)
        } else {
            Uri.fromFile(File(image.filePath))
        }

        Glide.with(context)
            .load(imageUri)
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            onRemoveImage(image)
        }
    }

    override fun getItemCount(): Int = selectedImages.size

    fun updateData(newSelectedImages: List<ImageModel>) {
        selectedImages = newSelectedImages.toMutableList()
        notifyDataSetChanged()
    }

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
