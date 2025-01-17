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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.collageimage.ActivitySelectImageEdit
import com.example.collageimage.ImageInMainAdapter
import com.example.collageimage.R
import com.example.collageimage.SelectActivity
import com.example.collageimage.Setting
import com.example.collageimage.TemplateActivity
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

    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                navigateToTargetActivity()
            } else {
                Toast.makeText(requireContext(), "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
            }
        }

    private var targetActivity: Class<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnSetting.setOnClickListener {
            navigateToSettings()
        }
        binding.btnPhotoCollage.setOnClickListener {
            targetActivity = SelectActivity::class.java
            checkAndRequestPermissions()
        }
        binding.btnEditImage.setOnClickListener {
            targetActivity = ActivitySelectImageEdit::class.java
            checkAndRequestPermissions()
        }
        setupViewPager()
        autoScrollViewPager()

        setuptemplate()
    }

    private fun setuptemplate() {
        binding.image1.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 27)
            startActivity(intent)
        }
        binding.image2.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 25)
            startActivity(intent)

        }
        binding.image3.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 29)
            startActivity(intent)

        }
        binding.image4.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 17)
            startActivity(intent)

        }
        binding.image5.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 16)
            startActivity(intent)

        }
        binding.image6.setOnClickListener {
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", 20)
            startActivity(intent)

        }
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
            permissionLauncher.launch(storagePermissions)
        }
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
}
