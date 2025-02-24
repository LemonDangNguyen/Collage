package com.photomaker.camerashot.photocollage.instacolor.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.model.LanguageModel
import com.nmh.base_lib.callback.ICallBackItem
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemLanguageBinding
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class LanguageAdapter @Inject constructor(@ActivityContext private val context: Context) :
    RecyclerView.Adapter<LanguageAdapter.LanguageHolder>() {

    var callBack: ICallBackItem? = null
    var lstLanguage = mutableListOf<LanguageModel>()
    private var w = context.resources.displayMetrics.widthPixels / 100f
    private var isSelected = false

    fun setData(lstLanguage: MutableList<LanguageModel>) {
        this.lstLanguage = lstLanguage
        isSelected = !lstLanguage.none { it.isCheck }

        changeNotify()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageHolder {
        return LanguageHolder(
            ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = if (lstLanguage.isNotEmpty()) lstLanguage.size else 0

    override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
        holder.onBind(position)
    }

    inner class LanguageHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.layoutParams.height = (15.55f * w).toInt()
        }

        fun onBind(position: Int) {
            val lang = lstLanguage[position]

            Glide.with(context)
                .asBitmap()
                .load("file:///android_asset/${lang.uri}/${lang.name.lowercase()}.webp")
                .into(binding.ivFlag)

            binding.tvName.apply {
                text = when(lang.name){
                    "China_simplified" -> "China (Simplified)"
                    "China_traditional" -> "China (Traditional)"
                    "South_korea" -> "South Korea"
                    else -> lang.name
                }
            }
            binding.tvDes.text = lang.nativeName
            if (position == 1 && !isSelected) {
                binding.animationTap.visible()
                binding.root.setBackgroundResource(R.drawable.border_item_language_select)
            } else {
                binding.animationTap.gone()
                binding.root.setBackgroundResource(R.drawable.border_item_language_un_select)
            }

            if (lang.isCheck) {
                binding.ivSelect.setImageResource(R.drawable.ic_choose)

                binding.animationTap.gone()
                binding.root.setBackgroundResource(R.drawable.border_item_language_un_select)
            } else binding.ivSelect.setImageResource(R.drawable.ic_un_choose)

            binding.root.setOnClickListener {
                isSelected = true
                setCurrent(position)
                callBack?.callBack(lang, position)
            }
        }
    }

    fun setCurrent(position: Int) {
        for (pos in lstLanguage.indices) lstLanguage[pos].isCheck = pos == position

        changeNotify()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeNotify() {
        notifyDataSetChanged()
    }
}