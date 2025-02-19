package com.example.collageimage.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.collageimage.Setting
import com.example.collageimage.TemplateActivity
import com.example.collageimage.databinding.FragmentTemplateBinding
import com.example.collageimage.image_template.AdsModel
import com.example.collageimage.image_template.ImageTemplateAdapter
import com.example.collageimage.image_template.ImageTemplateViewModel
import com.example.collageimage.utils.AdsConfig.haveNetworkConnection
import com.example.collageimage.view_template.SpaceItemDecoration
import com.google.android.gms.ads.nativead.NativeAd
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.example.collageimage.callback.ICallBackDimensional
import com.example.collageimage.utils.AdsConfig
import com.nlbn.ads.callback.AdCallback
import com.nmh.base_lib.callback.ICallBackItem

class TemplateFragment : Fragment() {

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
        imageTemplateAdapter = ImageTemplateAdapter(requireContext())
        imageTemplateViewModel = ViewModelProvider(this).get(ImageTemplateViewModel::class.java)

        imageTemplateViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageTemplateAdapter.setItemList(imageList)
        }
        AdsConfig.loadInterItemTemplate(requireActivity())

        imageTemplateAdapter.onItemClickListener = { imageId ->
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", imageId.id)
            showInterHomeTemplate(intent)
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
        } else {
            startActivity(intent)
        }
    }
}
