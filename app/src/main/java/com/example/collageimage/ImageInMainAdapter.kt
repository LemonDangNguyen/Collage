package com.example.collageimage;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.R


class ImageInMainAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<ImageInMainAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image2, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}
