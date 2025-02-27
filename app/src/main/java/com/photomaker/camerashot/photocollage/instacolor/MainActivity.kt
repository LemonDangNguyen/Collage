package com.photomaker.camerashot.photocollage.instacolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.ViewControl.actionAnimation
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivityMainBinding
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.setOnUnDoubleClickListener
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.fragment.CollageFragment
import com.photomaker.camerashot.photocollage.instacolor.fragment.TemplateFragment
import com.photomaker.camerashot.photocollage.instacolor.permission.PermissionSheet
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.cbFetchInterval
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject
    lateinit var bottomSheet: PermissionSheet

    private val collageFragment : CollageFragment by lazy { CollageFragment.newInstance() }
    private val templateFragment : TemplateFragment by lazy { TemplateFragment.newInstance() }

    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.banner.gone()
                collageFragment.hideAds()
                templateFragment.setShowAds(false)

                showDialogExit(object : ICallBackCheck {
                    override fun check(isCheck: Boolean) {
                        if(haveNetworkConnection() && ConsentHelper.getInstance(this@MainActivity).canRequestAds()
                            && AdsConfig.is_load_banner_all){
                            binding.banner.visible()
                        }
                        templateFragment.setShowAds(true)
                        collageFragment.showAds()
                    }
                })
            }
        })

        loadBanner()
        AdsConfig.loadNativeHome(this@MainActivity)
        AdsConfig.loadNativeExitApp(this@MainActivity)
        AdsConfig.loadNativeAll(this@MainActivity)
        AdsConfig.loadInterHome(this@MainActivity)
        AdsConfig.loadInterBack(this@MainActivity)
        AdsConfig.loadInterItemTemplate(this@MainActivity)

        binding.vpHome.apply {
            adapter = ViewPagerAdapter(this@MainActivity)
            offscreenPageLimit = 2
            isUserInputEnabled = false
        }
        binding.btnCollage.setOnUnDoubleClickListener {
            if (binding.vpHome.currentItem != 0) showInterHome(0)
        }
        binding.btnTemplate.setOnUnDoubleClickListener {
            if (binding.vpHome.currentItem != 1) showInterHome(1)
        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
        if (::bottomSheet.isInitialized) {
            bottomSheet.checkPer()
        }
    }

    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.is_load_banner_all) {
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval
            config.defaultCBFetchIntervalSec = cbFetchInterval
            config.defaultAdUnitId = getString(R.string.banner_all)
            config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }

    private fun selectBottomNavBar(position: Int) {
        binding.lnBottomBar.actionAnimation()
        binding.vpHome.setCurrentItem(position, true)

        binding.icHome.setImageResource(R.drawable.ic_home)
        binding.tvHome.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.color_78838B))
        binding.icTemplate.setImageResource(R.drawable.ic_template)
        binding.tvTemplate.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.color_78838B))

        when (position) {
            0 -> {
                binding.icHome.setImageResource(R.drawable.ic_home_selected)
                binding.tvHome.setTextColor(Color.parseColor("#3B83FC"))
            }
            1 -> {
                binding.icTemplate.setImageResource(R.drawable.ic_template_selected)
                binding.tvTemplate.setTextColor(Color.parseColor("#3B83FC"))
            }
        }
    }

    private fun showInterHome(position: Int) {
        if (AdsConfig.haveNetworkConnection(this) && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.interHome != null && AdsConfig.checkTimeShowInter() && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_home) {
            Admob.getInstance().showInterAds(this@MainActivity, AdsConfig.interHome, object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    selectBottomNavBar(position)
                }

                override fun onAdClosedByUser() {
                    super.onAdClosedByUser()
                    AdsConfig.interHome = null
                    AdsConfig.lastTimeShowInter = System.currentTimeMillis()
                    AdsConfig.loadInterHome(this@MainActivity)
                }
            })
        } else  selectBottomNavBar(position)
    }


    inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> collageFragment
                1 -> templateFragment
                else -> collageFragment
            }
        }
    }
}