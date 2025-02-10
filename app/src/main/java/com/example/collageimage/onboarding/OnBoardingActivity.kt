package com.example.collageimage.onboarding

import androidx.viewpager2.widget.ViewPager2
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivityOnBoardingBinding
import com.example.collageimage.extensions.setOnUnDoubleClickListener
import com.example.collageimage.permission.PermissionActivity
import com.nmh.base.project.adapter.DepthPageTransformer
import com.nmh.base.project.helpers.FIRST_INSTALL
import com.nmh.base.project.model.OnBoardingModel
import com.nmh.base.project.sharepref.DataLocalManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>(ActivityOnBoardingBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    @Inject
    lateinit var pageAdapter: PagerOnBoardingAdapter

    override fun setUp() {
        binding.tvAction.setOnUnDoubleClickListener {
            when (binding.viewPager.currentItem) {
                0 -> binding.viewPager.setCurrentItem(1, true)
                1 -> binding.viewPager.setCurrentItem(2, true)
                2 -> binding.viewPager.setCurrentItem(3, true)
                3 -> startActivity()
            }
        }

        pageAdapter.setData(mutableListOf<OnBoardingModel>().apply {
            add(OnBoardingModel(getString(R.string.title_onboarding_1), getString(R.string.des_onboarding_1), R.drawable.img_on_boarding_1))
            add(OnBoardingModel(getString(R.string.title_onboarding_2), getString(R.string.des_onboarding_2), R.drawable.img_on_boarding_2))
            add(OnBoardingModel(getString(R.string.title_onboarding_3), getString(R.string.des_onboarding_3), R.drawable.img_on_boarding_3))
            add(OnBoardingModel(getString(R.string.title_onboarding_4), getString(R.string.des_onboarding_4), R.drawable.img_on_boarding_4))
        })

        binding.viewPager.apply {
            setPageTransformer(DepthPageTransformer())
            offscreenPageLimit = 4
            adapter = pageAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            binding.tvAction.text = getString(R.string.next)
                            binding.tvTitle.text = getString(R.string.title_onboarding_1)
                            binding.tvDes.text = getString(R.string.des_onboarding_1)
                        }
                        1 -> {
                            binding.tvAction.text = getString(R.string.next)
                            binding.tvTitle.text = getString(R.string.title_onboarding_2)
                            binding.tvDes.text = getString(R.string.des_onboarding_2)
                        }
                        2 -> {
                            binding.tvAction.text = getString(R.string.next)
                            binding.tvTitle.text = getString(R.string.title_onboarding_3)
                            binding.tvDes.text = getString(R.string.des_onboarding_3)
                        }
                        3 -> {
                            binding.tvAction.text = getString(R.string.start)
                            binding.tvTitle.text = getString(R.string.title_onboarding_4)
                            binding.tvDes.text = getString(R.string.des_onboarding_3)
                        }
                    }
                }
            })
        }
        binding.indicator.attachTo(binding.viewPager)
    }

    private fun startActivity() {
        if (DataLocalManager.getBoolean(FIRST_INSTALL, true))
            startIntent(PermissionActivity::class.java.name, true)
        else startIntent(MainActivity::class.java.name, true)
    }
}
