package com.example.collageimage

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.collageimage.ViewControl.actionAnimation
import com.example.collageimage.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CollageFragment()
                1 -> TemplateFragment()
                else -> CollageFragment()
            }
        }
    }

}