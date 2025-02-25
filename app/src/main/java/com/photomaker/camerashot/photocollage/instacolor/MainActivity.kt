package com.photomaker.camerashot.photocollage.instacolor

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.photomaker.camerashot.photocollage.instacolor.ViewControl.actionAnimation
import com.photomaker.camerashot.photocollage.instacolor.base.BaseActivity

import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.fragment.CollageFragment
import com.photomaker.camerashot.photocollage.instacolor.fragment.TemplateFragment
import com.photomaker.camerashot.photocollage.instacolor.permission.PermissionSheet
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.cbFetchInterval
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.callback.AdCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.ActivityMainBinding
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
                if (templateFragment.isAdded) {
                    templateFragment.hideAds2()
                }

                showDialogExit(object : ICallBackCheck {
                    override fun check(isCheck: Boolean) {
                        binding.banner.visible()
                        collageFragment.showAds()
                        if (templateFragment.isAdded) {
                            templateFragment.showAds2()
                        }
                    }
                })
            }
        })
        loadBanner()
        AdsConfig.loadNativeHome(this@MainActivity)
        AdsConfig.loadNativeExitApp(this@MainActivity)
//        AdsConfig.loadNativeAll(this@MainActivity)
        AdsConfig.loadInterHome(this@MainActivity)
        AdsConfig.loadInterBack(this@MainActivity)
        AdsConfig.loadInterItemTemplate(this@MainActivity)

    }
    private fun loadBanner() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()) {
            val config = BannerPlugin.Config()
            config.defaultRefreshRateSec = cbFetchInterval /*cbFetchInterval lấy theo remote*/
            config.defaultCBFetchIntervalSec = cbFetchInterval

            if (true /*thêm biến check remote, thường là switch_banner_collapse*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.CollapsibleBottom
            } else if (true /*thêm biến check remote, thường là banner_all*/) {
                config.defaultAdUnitId = getString(R.string.banner_all)
                config.defaultBannerType = BannerPlugin.BannerType.Adaptive
            } else {
                binding.banner.gone()
                return
            }
            Admob.getInstance().loadBannerPlugin(this, findViewById(R.id.banner), findViewById(R.id.shimmer), config)
        } else binding.banner.gone()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.vpHome.adapter = ViewPagerAdapter(this)
        binding.vpHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectBottomNavBar(position)
            }
        })

        binding.vpHome.currentItem = 0
        binding.vpHome.isUserInputEnabled = false

        binding.btnCollage.setOnClickListener {
            loadInter()
            binding.vpHome.currentItem = 0
        }
        binding.btnTemplate.setOnClickListener {
            loadInter()
            binding.vpHome.currentItem = 1
        }
        if (intent.getBooleanExtra("navigate_to_template", false)) {
            binding.vpHome.currentItem = 1
        }
    }
    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
        if (::bottomSheet.isInitialized) {
            bottomSheet.checkPer()
        }
    }


    private fun selectBottomNavBar(position: Int) {
        binding.lnBottomBar.actionAnimation()
        when (position) {
            0 -> {
                binding.icHome.setImageResource(R.drawable.ic_home_selected)
                binding.tvHome.setTextColor(Color.parseColor("#3B83FC"))
                binding.icTemplate.setImageResource(R.drawable.ic_template)
                binding.tvTemplate.setTextColor(Color.parseColor("#3B83FC"))

            }
            1 -> {
                binding.icHome.setImageResource(R.drawable.ic_home)
                binding.tvHome.setTextColor(Color.parseColor("#3B83FC"))
                binding.icTemplate.setImageResource(R.drawable.ic_template_selected)
                binding.tvTemplate.setTextColor(Color.parseColor("#3B83FC"))

            }

        }
    }
    private fun loadInter() {
        if (haveNetworkConnection() && ConsentHelper.getInstance(this).canRequestAds()
            && AdsConfig.isLoadFullAds() && AdsConfig.is_load_inter_home && AdsConfig.isLoadFullAds()) {

            val callback = object : AdCallback() {
                override fun onNextAction() {
                    super.onNextAction()

                }
            }

            Admob.getInstance().loadAndShowInter(this, getString(R.string.inter_intro), true, callback)
        }
    }

    inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
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