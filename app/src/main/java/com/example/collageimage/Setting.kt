package com.example.collageimage


import android.os.Bundle

import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySettingBinding

class Setting : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun setUp() {

    }

}