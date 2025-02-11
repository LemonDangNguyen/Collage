package com.example.collageimage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.collageimage.ViewControl.actionAnimation
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivityMainBinding
import com.example.collageimage.databinding.DialogExitAppBinding
import com.example.collageimage.fragment.CollageFragment
import com.example.collageimage.fragment.TemplateFragment
import com.example.collageimage.permission.PermissionSheet
import com.nlbn.ads.util.AppOpenManager
import com.nmh.base_lib.callback.ICallBackCheck
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    @Inject
    lateinit var bottomSheet: PermissionSheet

    override fun setUp() {

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
    override fun onBackPressed() {

        val binding2 = DialogExitAppBinding.inflate(layoutInflater)
        val dialog2 = Dialog(this)
        dialog2.setContentView(binding2.root)
        val window = dialog2.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.setCanceledOnTouchOutside(false)
        dialog2.setCancelable(false)

        binding2.tvExit.setOnClickListener {
            this.finish()
            super.onBackPressed()

        }

        binding2.tvStay.setOnClickListener {
            dialog2.dismiss()
        }
        dialog2.show()
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