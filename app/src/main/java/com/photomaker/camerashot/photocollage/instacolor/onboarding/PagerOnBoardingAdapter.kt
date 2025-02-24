package com.photomaker.camerashot.photocollage.instacolor.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide

import com.photomaker.camerashot.photocollage.instacolor.model.OnBoardingModel
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemOnBoardingBinding
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import kotlin.text.Typography.dagger

class PagerOnBoardingAdapter @Inject constructor(@ActivityContext private val context: Context) : RecyclerView.Adapter<PagerOnBoardingAdapter.PagerHolder>() {

    private lateinit var lstPage: MutableList<OnBoardingModel>
    private var w = context.resources.displayMetrics.widthPixels / 100f

    fun setData(lstTip: MutableList<OnBoardingModel>) {
        this.lstPage = lstTip

        notifyChange()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerHolder {
        return PagerHolder(
            ItemOnBoardingBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (lstPage.isNotEmpty()) lstPage.size else 0
    }

    override fun onBindViewHolder(holder: PagerHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PagerHolder(private val binding: ItemOnBoardingBinding) : ViewHolder(binding.root) {

        fun onBind(position: Int) {
            val item = lstPage[position]

            if (item.imgPage != -1)
                Glide.with(context)
                    .asBitmap()
                    .load(item.imgPage)
                    .into(binding.iv)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyChange() {
        notifyDataSetChanged()
    }
}