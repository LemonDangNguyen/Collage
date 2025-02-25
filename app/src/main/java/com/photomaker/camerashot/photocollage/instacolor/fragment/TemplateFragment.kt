package com.photomaker.camerashot.photocollage.instacolor.fragment

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.photomaker.camerashot.photocollage.instacolor.databinding.FragmentTemplateBinding
import com.photomaker.camerashot.photocollage.instacolor.image_template.ImageTemplateModel
import com.photomaker.camerashot.photocollage.instacolor.permission.PermissionSheet

class TemplateFragment : Fragment() {

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
        bottomSheet?.checkPer()
    }

    companion object {
        fun newInstance(): TemplateFragment {
            val args = Bundle()

            val fragment = TemplateFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private  var bottomSheet: PermissionSheet? =null
    private var _binding: FragmentTemplateBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageTemplateAdapter: ImageTemplateAdapter
    private lateinit var imageTemplateViewModel: ImageTemplateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageTemplateAdapter = ImageTemplateAdapter(activity ?: return)
        imageTemplateViewModel = ViewModelProvider(this).get(ImageTemplateViewModel::class.java)

        imageTemplateViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageTemplateAdapter.setItemList(imageList)
        }
        AdsConfig.loadInterItemTemplate(requireActivity())

        imageTemplateAdapter.onItemClickListener = { imageTemplateModel ->
            if (hasStoragePermissions()) {
                val intent = Intent(requireContext(), TemplateActivity::class.java)
                intent.putExtra("imageId", imageTemplateModel.id)
                showInterHomeTemplate(intent)
            } else {
                showPermissionBottomSheetForTemplate(imageTemplateModel)
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
    private fun hasStoragePermissions(): Boolean {
        val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        return storagePermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun showPermissionBottomSheetForTemplate(imageTemplateModel: ImageTemplateModel) {
        hideAds2()
        bottomSheet = PermissionSheet(requireContext()).apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (status) {
                        val intent = Intent(requireContext(), TemplateActivity::class.java)
                        intent.putExtra("imageId", imageTemplateModel.id)
                        showInterHomeTemplate(intent)
                        cancel()  // Đảm bảo BottomSheet được ẩn
                    } else {
                        Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            isDismiss = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (haveNetworkConnection(requireActivity())
                        && ConsentHelper.getInstance(requireActivity()).canRequestAds()
                        && AdsConfig.isLoadFullAds()
                        && AdsConfig.is_load_native_home
                    )
                    showAds2()
                }
            }

        }
        bottomSheet?.showDialog()
    }


    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        binding.rvTemplate.layoutManager = gridLayoutManager
        binding.rvTemplate.adapter = imageTemplateAdapter
        val spaceDecoration = SpaceItemDecoration(15)
        binding.rvTemplate.addItemDecoration(spaceDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showInterHomeTemplate(intent: Intent) {
        val context = activity ?: return
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
                    AdsConfig.loadInterItemTemplate(context)
                }
            })
        } else {
            startActivity(intent)
        }
    }

    fun hideAds2() {
        val updatedList = mutableListOf<Any>()
        for (item in imageTemplateAdapter.getItemList()) {
            if (item !is AdsModel) {
                updatedList.add(item)
            }
        }
        imageTemplateAdapter.setItemList(updatedList)
    }

    fun showAds2() {
        val updatedList = mutableListOf<Any>()
        for (item in imageTemplateAdapter.getItemList()) {
            updatedList.add(item)
        }

        var pos = -4
        var isCheck = false
        while (pos < updatedList.size) {
            pos += if (!isCheck) 5 else if (AdsConfig.is_load_native_item_template3) 5 else 4
            if (updatedList.size >= pos + 1 && AdsConfig.isLoadFullAds()) {
                updatedList.add(pos, AdsModel(
                    pos, null,
                    NMHApp.ctx.getString(R.string.native_item_template1),
                    false, AdsConfig.is_load_native_item_template3
                ))
            }

            pos += if (AdsConfig.is_load_native_item_template2) 5 else 4
            if (updatedList.size >= pos + 1 && AdsConfig.isLoadFullAds()) {
                updatedList.add(pos, AdsModel(
                    pos, null,
                    NMHApp.ctx.getString(R.string.native_item_template2),
                    false, AdsConfig.is_load_native_item_template2
                ))
            }

            pos += if (AdsConfig.is_load_native_item_template3) 5 else 4
            if (updatedList.size >= pos + 1 && AdsConfig.isLoadFullAds()) {
                updatedList.add(pos, AdsModel(
                    pos, null,
                    NMHApp.ctx.getString(R.string.native_item_template3),
                    false, AdsConfig.is_load_native_item_template3
                ))
            }

            isCheck = true
        }
        imageTemplateAdapter.setItemList(updatedList)
    }

}
