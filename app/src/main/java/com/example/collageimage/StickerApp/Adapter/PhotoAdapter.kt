package com.example.collageimage.StickerApp.Adapter

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collageimage.ImageModel
import com.example.collageimage.R
import com.example.collageimage.databinding.ItemImageStickerBinding
import com.example.collageimage.databinding.LayoutBtnCameraBinding
import java.io.File

class PhotoAdapter(
    private val context: Context,
    private val images: MutableList<ImageModel>,
    private val onItemSelected: (ImageModel, Boolean) -> Unit,
    private val onCameraClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Lưu lại trạng thái chọn hay chưa chọn của ảnh,
    // nếu cần dùng (tuỳ logic bạn muốn).
    private val selectedImagesMap = mutableMapOf<Long, Int>()

    companion object {
        private const val VIEW_TYPE_CAMERA = 0
        private const val VIEW_TYPE_IMAGE = 1
    }

    inner class CameraViewHolder(val binding: LayoutBtnCameraBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ImageViewHolder(val binding: ItemImageStickerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (images[position].isCameraItem) {
            VIEW_TYPE_CAMERA
        } else {
            VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CAMERA) {
            val binding = LayoutBtnCameraBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            CameraViewHolder(binding)
        } else {
            val binding = ItemImageStickerBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            ImageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = images[position]
        if (holder is CameraViewHolder) {
            // Xử lý click vào item camera
            holder.binding.ivcfCamera.setOnClickListener {
                onCameraClick()
            }
        } else if (holder is ImageViewHolder) {
            // Lấy Uri từ filePath
            val imageUri = getImageUriFromFilePath(image.filePath)
            // Load ảnh với Glide
            Glide.with(context)
                .load(imageUri)
                .error(R.drawable.noimage)
                .centerCrop()
                .into(holder.binding.ifv)

            // Xử lý click vào ảnh
            holder.binding.ifv.setOnClickListener {
                val isSelected = !selectedImagesMap.containsKey(image.id)
                onItemSelected(image, isSelected)

                // Nếu cần đánh dấu ảnh được chọn, bạn có thể lưu vào map hoặc đổi background
                // selectedImagesMap[image.id] = position
            }
        }
    }

    override fun getItemCount(): Int = images.size

    /**
     * Thêm một item camera vào đầu danh sách
     */
    fun addCameraItem() {
        val cameraItem = ImageModel(
            id = -1L,
            dateTaken = System.currentTimeMillis(),
            fileName = "Camera",
            filePath = "",
            album = "",
            uri = Uri.EMPTY,
            isCameraItem = true
        )
        images.add(0, cameraItem)
        notifyItemInserted(0)
    }

    /**
     * Chuyển từ đường dẫn file sang Uri (phục vụ việc load ảnh bằng Glide)
     */
    private fun getImageUriFromFilePath(filePath: String): Uri {
        if (filePath.isEmpty()) {
            // Trường hợp filePath rỗng (ví dụ: camera item) thì trả về Uri.EMPTY
            return Uri.EMPTY
        }
        val file = File(filePath)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DATA} = ?"
            val selectionArgs = arrayOf(filePath)

            val cursor = context.contentResolver.query(
                contentUri,
                projection,
                selection,
                selectionArgs,
                null
            )
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
