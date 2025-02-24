package com.photomaker.camerashot.photocollage.instacolor.image_template

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.util.Admob
import com.photomaker.camerashot.photocollage.instacolor.callback.ICallBackDimensional
import com.nmh.base_lib.callback.ICallBackItem
import com.google.android.gms.ads.nativead.NativeAdView
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotAdapterBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotAdapterNomediaBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemAdsBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemTemplateBinding

class ImageTemplateAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_TYPE_IMAGE = 0
        const val ITEM_TYPE_ADS = 1
    }

    private var itemList: List<Any> = listOf()

    var onItemClickListener: ((ImageTemplateModel) -> Unit)? = null
    var callbackDimensional: ICallBackDimensional? = null

    fun setItemList(newItemList: MutableList<Any>) {
        itemList = newItemList
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ItemTemplateBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int) {
            val item = itemList[pos] as ImageTemplateModel

            binding.imageView.setImageResource(item.imageResId)
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    inner class AdsViewHolder(private val binding: ItemAdsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val item = itemList[position] as AdsModel
            if (!item.keyRemote) binding.rlNative.gone()
            else {
                binding.rlNative.visible()
                binding.nativeAds
                if (!item.isLoaded) {
                    callbackDimensional?.callBackItem(item, object : ICallBackItem {
                        override fun callBack(ob: Any?, position: Int) {
                            if (ob is NativeAd) {
                                item.apply {
                                    this.nativeAd = ob
                                    isLoaded = true
                                }
                                pushAdsToView(ob)
                            } else binding.rlNative.gone()
                        }
                    })
                } else item.nativeAd?.let { pushAdsToView(it) }
            }
        }

        private fun pushAdsToView(nativeAd: NativeAd) {
            val bindingAds: NativeAdView // Sử dụng NativeAdView của Google
            if (AdsConfig.isLoadFullAds())
                bindingAds = AdsNativeBotAdapterNomediaBinding.inflate(LayoutInflater.from(context)).root
            else
                bindingAds = AdsNativeBotAdapterBinding.inflate(LayoutInflater.from(context)).root

            binding.nativeAds.removeAllViews()
            binding.nativeAds.addView(bindingAds)
            Admob.getInstance().pushAdsToViewCustom(nativeAd, bindingAds)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ImageTemplateModel -> ITEM_TYPE_IMAGE  // Trả về kiểu cho hình ảnh
            is AdsModel -> ITEM_TYPE_ADS  // Trả về kiểu cho quảng cáo
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    // Tạo ViewHolder phù hợp cho mỗi loại item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_IMAGE -> {
                val binding = ItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(binding)
            }
            ITEM_TYPE_ADS -> {
                val binding = ItemAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdsViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM_TYPE_IMAGE -> (holder as ImageViewHolder).bind(position)  // Gắn dữ liệu hình ảnh
            ITEM_TYPE_ADS -> (holder as AdsViewHolder).bind(position)  // Gắn dữ liệu quảng cáo
        }
    }
    override fun getItemCount(): Int {
        return itemList.size
    }
    fun getItemList(): List<Any> {
        return itemList
    }

}
