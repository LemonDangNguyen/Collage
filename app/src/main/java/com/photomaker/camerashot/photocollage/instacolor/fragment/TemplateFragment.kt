package com.photomaker.camerashot.photocollage.instacolor.fragment

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.photomaker.camerashot.photocollage.instacolor.Setting
import com.photomaker.camerashot.photocollage.instacolor.TemplateActivity
import com.photomaker.camerashot.photocollage.instacolor.image_template.AdsModel
import com.photomaker.camerashot.photocollage.instacolor.image_template.ImageTemplateAdapter
import com.photomaker.camerashot.photocollage.instacolor.image_template.ImageTemplateViewModel
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.haveNetworkConnection
import com.photomaker.camerashot.photocollage.instacolor.view_template.SpaceItemDecoration
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.callback.ICallBackDimensional
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.AppOpenManager
import com.nmh.base_lib.callback.ICallBackItem
import com.photomaker.camerashot.photocollage.instacolor.MainActivity
import com.photomaker.camerashot.photocollage.instacolor.NMHApp
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.base.BaseFragment
import com.photomaker.camerashot.photocollage.instacolor.databinding.FragmentTemplateBinding
import com.photomaker.camerashot.photocollage.instacolor.extensions.checkPer
import com.photomaker.camerashot.photocollage.instacolor.image_template.ImageTemplateModel
import com.photomaker.camerashot.photocollage.instacolor.permission.PermissionSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TemplateFragment : BaseFragment<FragmentTemplateBinding>(FragmentTemplateBinding::inflate) {

    companion object {
        fun newInstance(): TemplateFragment {
            val args = Bundle()

            val fragment = TemplateFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject lateinit var bottomSheet: PermissionSheet
    @Inject lateinit var imageTemplateAdapter: ImageTemplateAdapter

    private val imageTemplateViewModel: ImageTemplateViewModel by activityViewModels()

    private val storagePer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun setUp() {
        AdsConfig.loadInterItemTemplate(requireActivity())

        bottomSheet.apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                }
            }
            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    imageTemplateAdapter.setShowAds(true)
                }
            }
        }

        imageTemplateViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageTemplateAdapter.setItemList(imageList)
        }

        imageTemplateAdapter.onItemClickListener = { imageTemplateModel ->
            if (requireContext().checkPer(storagePer)) {
                val intent = Intent(requireContext(), TemplateActivity::class.java)
                intent.putExtra("imageId", imageTemplateModel.id)
                showInterHomeTemplate(intent)
            } else {
                if (!bottomSheet.isShowing) {
                    imageTemplateAdapter.setShowAds(false)
                    bottomSheet.showDialog()
                }
            }
        }
        imageTemplateAdapter.callbackDimensional = object : ICallBackDimensional {
            override fun callBackItem(objects: Any, callBackItem: ICallBackItem) {
                if(objects is AdsModel) {
                    if (haveNetworkConnection(requireActivity()) && AdsConfig.isLoadFullAds()
                        && ConsentHelper.getInstance(requireActivity()).canRequestAds()){
                        Admob.getInstance().loadNativeAd(requireActivity(), objects.strId, object: NativeCallback() {
                            override fun onNativeAdLoaded(p0: NativeAd) {
                                super.onNativeAdLoaded(p0)
                                callBackItem.callBack(p0,-1)
                            }



                            override fun onAdFailedToLoad() {
                                super.onAdFailedToLoad()
                                callBackItem.callBack(null,-1)
                            }
                        })
                    }
                } else callBackItem.callBack(null,-1)
            }

            override fun callBackCheck(objects: Any, check: ICallBackCheck) {

            }
        }

        setupRecyclerView()

        binding.btnSetting.setOnClickListener {
            startActivity(Intent(requireContext(), Setting::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)

        if (!bottomSheet.checkPer()) bottomSheet.loadNative()
    }

    private fun setupRecyclerView() {
        binding.rvTemplate.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        binding.rvTemplate.adapter = imageTemplateAdapter
        binding.rvTemplate.addItemDecoration(SpaceItemDecoration(15))
    }

    private fun showInterHomeTemplate(intent: Intent) {
        if (haveNetworkConnection(requireActivity()) && ConsentHelper.getInstance(requireActivity()).canRequestAds()
            && AdsConfig.inter_item_template != null && AdsConfig.checkTimeShowInter()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_item_template) {
            Admob.getInstance().showInterAds(requireActivity(), AdsConfig.inter_item_template, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startActivity(intent)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.inter_item_template = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterItemTemplate(requireActivity())
                }
            })
        } else startActivity(intent)
    }

    fun setShowAds(isShow: Boolean) {
        imageTemplateAdapter.setShowAds(isShow)
    }
}
