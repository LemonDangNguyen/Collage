package com.example.collageimage;
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.collageimage.R

class SelectedImagesAdapter(
    private val context: Context,
    private var selectedImages: MutableList<ImageModel>,  // Thay đổi từ val thành var để có thể cập nhật lại danh sách
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

        // Sử dụng Glide để tải ảnh
        Glide.with(context)
            .load(image.filePath)  // Sử dụng đường dẫn file của ảnh
            .into(holder.imageView)

        holder.deleteButton.setOnClickListener {
            // Gọi hàm onRemoveImage khi nhấn nút xóa
            onRemoveImage(image)
        }
    }

    override fun getItemCount(): Int = selectedImages.size

    // Cung cấp phương thức để cập nhật lại dữ liệu cho adapter
    fun updateData(newSelectedImages: List<ImageModel>) {
        selectedImages = newSelectedImages.toMutableList()
        notifyDataSetChanged()  // Gọi notifyDataSetChanged để cập nhật giao diện
    }
}
