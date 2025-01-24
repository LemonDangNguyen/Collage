package com.example.collageimage

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.collageimage.ViewControl.actionAnimation
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivityMainBinding
import com.example.collageimage.fragment.CollageFragment
import com.example.collageimage.fragment.TemplateFragment

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
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
        }
    }


    private fun selectBottomNavBar(position: Int) {
        binding.lnBottomBar.actionAnimation()
        when (position) {
            0 -> {
                binding.icHome.setImageResource(R.drawable.ic_home_selected)
                binding.tvHome.setTextColor(Color.parseColor("#3B83FC"))
               // binding.tvHome.typeface = getFont(this, R.font.nunito_bold)

                binding.icTemplate.setImageResource(R.drawable.ic_template)
                binding.tvTemplate.setTextColor(Color.parseColor("#3B83FC"))

            }
            1 -> {
                binding.icHome.setImageResource(R.drawable.ic_home)
                binding.tvHome.setTextColor(Color.parseColor("#3B83FC"))
                // binding.tvHome.typeface = getFont(this, R.font.nunito_bold)

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