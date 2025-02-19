package com.example.collageimage.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.collageimage.ActivitySelectImageEdit
import com.example.collageimage.ImageInMainAdapter
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.SelectActivity
import com.example.collageimage.Setting
import com.example.collageimage.TemplateActivity
import com.example.collageimage.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.example.collageimage.databinding.FragmentCollageBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.visible
import com.example.collageimage.permission.PermissionSheet
import com.example.collageimage.utils.AdsConfig
import com.example.collageimage.utils.AdsConfig.haveNetworkConnection
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollageFragment : Fragment() {

    private lateinit var bottomSheet: PermissionSheet

    private val binding by lazy { FragmentCollageBinding.inflate(layoutInflater) }
    private val imageList = listOf(
        R.drawable.bg_in_main_01,
        R.drawable.bg_in_main_02,
        R.drawable.bg_in_main_03,
        R.drawable.bg_in_main_04,
        R.drawable.bg_in_main_05,
        R.drawable.bg_in_main_06
    )

    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    private var targetActivity: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSetting.setOnClickListener {
            showInterHome(Setting::class.java.name)
        }

        binding.btnPhotoCollage.setOnClickListener {
            targetActivity = SelectActivity::class.java
            checkAndRequestPermissionsForHome(targetActivity?.name)
        }

        binding.btnEditImage.setOnClickListener {
            targetActivity = ActivitySelectImageEdit::class.java
            checkAndRequestPermissionsForHome(targetActivity?.name)
        }

        setupViewPager()
        autoScrollViewPager()
        showNative()
        setuptemplate()
    }
    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
        if (::bottomSheet.isInitialized) {
            bottomSheet.checkPer()
        }
    }

    private fun setuptemplate() {
        binding.image1.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 27)
        }
        binding.image2.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 25)
        }
        binding.image3.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 29)
        }
        binding.image4.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 17)
        }
        binding.image5.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 16)
        }
        binding.image6.setOnClickListener {
            showInterHomeTemplate(TemplateActivity::class.java.name, 20)
        }
    }


    private fun checkAndRequestPermissions2(imageId: Int) {
        if (hasStoragePermissions()) {
            //navigateToTemplateActivity(imageId)
            showInterHomeTemplate(TemplateActivity::class.java.name, imageId)
        } else {
            showPermissionBottomSheet(imageId)
        }
    }

    private fun showPermissionBottomSheet(imageId: Int) {
        bottomSheet = PermissionSheet(requireContext()).apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (status) {
                        navigateToTemplateActivity(TemplateActivity::class.java.name, imageId)
                        cancel()
                    } else {
                        Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {}
            }
        }
        bottomSheet.showDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun checkAndRequestPermissions() {
        if (hasStoragePermissions()) {
            navigateToTargetActivity()
        } else {
            showPermissionBottomSheet()
        }
    }
    private fun showPermissionBottomSheet() {
         bottomSheet = PermissionSheet(requireContext()).apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (status) {
                        navigateToTargetActivity()
                        cancel()
                    } else {
                        Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {
                }
            }
        }
        bottomSheet.showDialog()
    }



    private fun hasStoragePermissions(): Boolean {
        return storagePermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun navigateToTargetActivity() {
        targetActivity?.let { startActivity(Intent(requireContext(), it)) }
    }

    private fun navigateToSettings() {
        startActivity(Intent(requireContext(), Setting::class.java))
    }

    private fun setupViewPager() {
        val adapter = ImageInMainAdapter(imageList)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.setViewPager2(binding.viewPager)
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
                    } else {
                        currentItem + 1
                    }
                } else {
                    if (currentItem == 0) {
                        isForwardScroll = true
                        currentItem + 1
                    } else {
                        currentItem - 1
                    }
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
        if (haveNetworkConnection(requireContext())
            && ConsentHelper.getInstance(requireContext()).canRequestAds()
            && AdsConfig.interHome != null
            && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds()
            &&AdsConfig.is_load_inter_home) {

            Admob.getInstance().showInterAds(requireContext(), AdsConfig.interHome, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    navigateToTemplateActivity(className, imageId)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interHome = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterHome(requireContext())
                }
            })
        } else {
            navigateToTemplateActivity(className, imageId)
        }
    }

    private fun navigateToTemplateActivity(className: String, imageId: Int) {
        val intent = Intent(requireContext(), Class.forName(className))
        intent.putExtra("imageId", imageId)
        startActivity(intent)
    }



    private fun showInterHome(className: String) {
        if (haveNetworkConnection(requireContext())
            && ConsentHelper.getInstance(requireContext()).canRequestAds()
            && AdsConfig.interHome != null
            && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds()
            && AdsConfig.is_load_inter_home){
            Admob.getInstance().showInterAds(requireContext(), AdsConfig.interHome, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()

                    startActivity(className)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interHome = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterHome(requireContext())
                }
            })
        } else startActivity(className)
    }

    private fun startActivity(className: String) {
        startActivity(Intent(requireContext(), Class.forName(className)))
    }
    private fun checkAndRequestPermissionsForHome(className: String?) {
        if (hasStoragePermissions()) {
            // Nếu đã có quyền, hiển thị quảng cáo rồi chuyển đến activity cần thiết
            showInterHome(className.orEmpty())
        } else {
            // Nếu không có quyền, hiển thị bảng yêu cầu quyền
            showPermissionBottomSheetForHome(className.orEmpty())
        }
    }

    private fun showPermissionBottomSheetForHome(className: String) {
        bottomSheet = PermissionSheet(requireContext()).apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (status) {
                        // Nếu người dùng cấp quyền, hiển thị quảng cáo và chuyển đến màn hình
                        showInterHome(className)
                        cancel()
                    } else {
                        Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {}
            }
        }
        bottomSheet.showDialog()
    }
    private fun showNative() {
        if (haveNetworkConnection(requireActivity())
            && ConsentHelper.getInstance(requireActivity()).canRequestAds()
            && AdsConfig.isLoadFullAds()
            && AdsConfig.is_load_native_home)
        {
            binding.rlNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(requireActivity(), getString(R.string.native_all),
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
        if (!AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)
        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
