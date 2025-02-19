package com.example.collageimage


import android.content.Intent
import android.os.Bundle

import com.example.collageimage.base.BaseActivity
import com.example.collageimage.databinding.ActivitySettingBinding
import com.example.collageimage.extensions.gone
import com.example.collageimage.language.LanguageActivity
import com.nlbn.ads.util.AppOpenManager
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.example.collageimage.sharepref.DataLocalManager
import com.nmh.base.project.utils.ActionUtils
import com.nmh.base.project.utils.UtilsRate
import com.nmh.base_lib.callback.ICallBackCheck


class Setting : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnLanguage.setOnClickListener {
            DataLocalManager.setBoolean(IS_SHOW_BACK, true)
            startIntent(Intent(this, LanguageActivity::class.java), false)
        }

        binding.btnRate.setOnClickListener {
            AppOpenManager.getInstance().disableAppResumeWithActivity(Setting::class.java)
            UtilsRate.showRate(this, false, object : ICallBackCheck {
                override fun check(isCheck: Boolean) {
                    if (isCheck) binding.btnRate.gone()
                }
            })
        }
        binding.btnShare.setOnClickListener {
            ActionUtils.shareApp(this)
        }
        binding.btnFeedback.setOnClickListener {
            ActionUtils.sendFeedback(this)
        }
        binding.btnPolicy.setOnClickListener {
            ActionUtils.openPolicy(this)
        }
    }

    override fun setUp() {
    }

}