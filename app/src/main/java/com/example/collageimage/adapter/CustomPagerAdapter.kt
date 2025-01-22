package com.nmh.base.project.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide

class CustomPagerAdapter(context: Context): RecyclerView.Adapter<CustomPagerAdapter.PagerHolder>() {

    private val context: Context
    private var lstPage: ArrayList<Int>

    init {
        this.context = context
        this.lstPage = ArrayList()
    }

    fun setData(lstItem: ArrayList<Int>) {
        this.lstPage = lstItem

        notifyChange()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder {
        return PagerHolder(ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        })
    }

    override fun getItemCount(): Int {
        if (lstPage.isNotEmpty()) return Int.MAX_VALUE
        return 0
    }

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PagerHolder(viewItem: ImageView): ViewHolder(viewItem) {

        private val iv: ImageView

        init {
            this.iv = viewItem
            this.iv.layoutParams = RelativeLayout.LayoutParams(-1, -1)
        }

        fun onBind(position: Int) {
            val item = lstPage[position % lstPage.size]

            Glide.with(context)
                .load(item)
                .into(iv)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyChange() {
        notifyDataSetChanged()
    }
}