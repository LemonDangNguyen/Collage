package com.example.collageimage

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
import com.example.collageimage.ViewControl.actionAnimation
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivityMainBinding
import com.example.collageimage.databinding.DialogExitAppBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.extensions.visible
import com.example.collageimage.fragment.CollageFragment
import com.example.collageimage.fragment.TemplateFragment
import com.example.collageimage.permission.PermissionSheet
import com.example.collageimage.utils.AdsConfig
import com.example.collageimage.utils.AdsConfig.cbFetchInterval
import com.nlbn.ads.banner.BannerPlugin
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.AppOpenManager
import com.nlbn.ads.util.ConsentHelper
import com.nmh.base_lib.callback.ICallBackCheck
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    @Inject
    lateinit var bottomSheet: PermissionSheet

    override fun setUp() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.banner.gone()
                showDialogExit(object : ICallBackCheck {
                    override fun check(isCheck: Boolean) {
                        binding.banner.visible()
                    }
                })
            }
        })
        loadBanner()
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
            binding.vpHome.currentItem = 0
        }
        binding.btnTemplate.setOnClickListener {
            binding.vpHome.currentItem = 1
           // showPermissionBottomSheet()
        }
    }
    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
        if (::bottomSheet.isInitialized) {
            bottomSheet.checkPer()
        }
    }

    private fun showPermissionBottomSheet() {
        bottomSheet = PermissionSheet(this).apply {
            isDone = object : ICallBackCheck {
                override fun check(status: Boolean) {
                    if (status) {
                        binding.vpHome.currentItem = 1
                        binding.vpHome.adapter?.notifyDataSetChanged()
                        cancel()
                    } else {
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

    inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CollageFragment()
                1 -> TemplateFragment()
                else -> CollageFragment()
            }
        }
    }
}