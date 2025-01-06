package com.example.collageimage.fragment

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.collageimage.ActivitySelectImageEdit
import com.example.collageimage.ImageInMainAdapter
import com.example.collageimage.R
import com.example.collageimage.SelectActivity

import com.example.collageimage.Setting
import com.example.collageimage.databinding.FragmentCollageBinding

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollageFragment : Fragment() {

    private val binding by lazy { FragmentCollageBinding.inflate(layoutInflater) }
    private val imageList = listOf(
        R.drawable.bg_in_main_01,
        R.drawable.bg_in_main_02,
        R.drawable.bg_in_main_03,
        R.drawable.bg_in_main_04,
        R.drawable.bg_in_main_05,
        R.drawable.bg_in_main_06
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSetting.setOnClickListener {
            navigateToSettings()
        }
        binding.btnPhotoCollage.setOnClickListener {
            startActivity(Intent(requireContext(), SelectActivity::class.java))
        }
        binding.btnEditImage.setOnClickListener {
            startActivity(Intent(requireContext(), ActivitySelectImageEdit::class.java))
        }
        setupViewPager()
        autoScrollViewPager()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
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
}