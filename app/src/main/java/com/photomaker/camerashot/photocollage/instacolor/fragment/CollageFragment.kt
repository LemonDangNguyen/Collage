package com.photomaker.camerashot.photocollage.instacolor.fragment

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.ActivitySelectImageEdit
import com.photomaker.camerashot.photocollage.instacolor.ImageInMainAdapter
import com.photomaker.camerashot.photocollage.instacolor.MainActivity
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.SelectActivity
import com.photomaker.camerashot.photocollage.instacolor.Setting
import com.photomaker.camerashot.photocollage.instacolor.TemplateActivity
import com.photomaker.camerashot.photocollage.instacolor.base.BaseFragment
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.FragmentCollageBinding
import com.photomaker.camerashot.photocollage.instacolor.extensions.checkAllPerGrand
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.permission.PermissionSheet
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.haveNetworkConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CollageFragment : BaseFragment<FragmentCollageBinding>(FragmentCollageBinding::inflate) {

    companion object {
        fun newInstance(): CollageFragment {
            val args = Bundle()

            val fragment = CollageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject lateinit var bottomSheet: PermissionSheet

    private val imageList = listOf(
        R.drawable.bg_in_main_01,
        R.drawable.bg_in_main_02,
        R.drawable.bg_in_main_03,
        R.drawable.bg_in_main_04,
        R.drawable.bg_in_main_05,
        R.drawable.bg_in_main_06
    )
    private var targetActivity: Class<*>? = null

    override fun setUp() {
        showNative()

        bottomSheet.apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {

                }
            }
            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (haveNetworkConnection(requireActivity()) && ConsentHelper.getInstance(requireActivity()).canRequestAds()
                        && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_home)
                        binding.rlNative.visible()
                }
            }
        }

        binding.viewPager.adapter = ImageInMainAdapter(imageList)
        binding.dotsIndicator.attachTo(binding.viewPager)

        autoScrollViewPager()
        evenClick()
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)

        if (!bottomSheet.checkPer()) bottomSheet.loadNative()
    }

    private fun evenClick() {
        binding.btnSetting.setOnUnDoubleClickListener {
            showInterHomeTemplate(Setting::class.java.name, -1)
        }
        binding.btnPhotoCollage.setOnUnDoubleClickListener {
            targetActivity = SelectActivity::class.java
            checkAndRequestPermissionsForHome(targetActivity?.name)
        }
        binding.btnEditImage.setOnUnDoubleClickListener {
            targetActivity = ActivitySelectImageEdit::class.java
            checkAndRequestPermissionsForHome(targetActivity?.name)
        }

        binding.image1.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 27)
        }
        binding.image2.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 25)
        }
        binding.image3.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 29)
        }
        binding.image4.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 17)
        }
        binding.image5.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 16)
        }
        binding.image6.setOnUnDoubleClickListener {
            checkAndRequestPermissionsForHomeTemplate(TemplateActivity::class.java.name, 20)
        }
    }

    private fun checkAndRequestPermissionsForHomeTemplate(className: String, imageId: Int) {
        if (requireContext().checkAllPerGrand()) showInterHomeTemplate(className, imageId)
        else if (!bottomSheet.isShowing) bottomSheet.showDialog()
    }

    private var isForwardScroll = true

    private fun autoScrollViewPager() {
        lifecycleScope.launch {
            while (true) {
                delay(3000)

                val currentItem = binding.viewPager.currentItem
                val nextItem = if (isForwardScroll) {
                    if (currentItem == imageList.size - 1) {
                        isForwardScroll = false
                        currentItem - 1
                    } else currentItem + 1
                } else {
                    if (currentItem == 0) {
                        isForwardScroll = true
                        currentItem + 1
                    } else currentItem - 1
                }

                smoothScrollToItem(nextItem)
            }
        }
    }

    private fun smoothScrollToItem(item: Int) {
        val recyclerView =
            binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView ?: return
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 150f / displayMetrics.densityDpi
            }
        }
        smoothScroller.targetPosition = item
        recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun showInterHomeTemplate(className: String, imageId: Int) {
        if (haveNetworkConnection(requireContext()) && ConsentHelper.getInstance(requireActivity()).canRequestAds()
            && AdsConfig.interHome != null && AdsConfig.checkTimeShowInter() && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_home) {
            Admob.getInstance().showInterAds(requireActivity(), AdsConfig.interHome, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    navigateToTemplateActivity(className, imageId)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interHome = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterHome(requireActivity())
                }
            })
        } else navigateToTemplateActivity(className, imageId)
    }

    private fun navigateToTemplateActivity(className: String, imageId: Int) {
        val intent = Intent(requireContext(), Class.forName(className))
        if (imageId != -1) intent.putExtra("imageId", imageId)

        startActivity(intent)
    }

    private fun checkAndRequestPermissionsForHome(className: String?) {
        if (requireContext().checkAllPerGrand())
            showInterHomeTemplate(className.orEmpty(), -1)
        else if (!bottomSheet.checkPer()) bottomSheet.loadNative()
    }

    private fun showNative() {
        if (haveNetworkConnection(requireActivity()) && ConsentHelper.getInstance(requireActivity()).canRequestAds()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_home) {
            binding.rlNative.visible()
            AdsConfig.nativeHome?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(requireActivity(), getString(R.string.native_home),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAds(nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            binding.frNativeAds.removeAllViews()
                        }
                    }
                )
            }
        } else binding.rlNative.gone()
    }
    private fun pushViewAds(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)
        if (!AdsConfig.isLoadFullAds()) adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)

        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }

    fun hideAds() {
        binding.rlNative.gone()
    }

    fun showAds(){
        if (haveNetworkConnection(requireActivity()) && ConsentHelper.getInstance(requireActivity()).canRequestAds()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_native_home) {
            binding.rlNative.visible()
        } else binding.rlNative.gone()
    }
}
