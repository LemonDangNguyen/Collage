package com.example.collageimage.language

import android.view.Gravity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collageimage.R
import com.example.collageimage.base.BaseActivity
import com.example.collageimage.base.UiState
import com.example.collageimage.databinding.ActivityLanguageAcyivityBinding
import com.example.collageimage.extensions.invisible
import com.example.collageimage.extensions.setOnUnDoubleClickListener
import com.example.collageimage.onboarding.OnBoardingActivity

import com.example.collageimage.extensions.showToast
import com.example.collageimage.extensions.visible

import com.nmh.base.project.helpers.CURRENT_LANGUAGE
import com.nmh.base.project.helpers.IS_SHOW_BACK
import com.nmh.base.project.model.LanguageModel
import com.nmh.base.project.sharepref.DataLocalManager
import com.nmh.base_lib.callback.ICallBackItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageAcyivityBinding>(ActivityLanguageAcyivityBinding::inflate) {

    override fun isHideNavigation(): Boolean = true

    @Inject
    lateinit var langAdapter: LanguageAdapter
    private val viewModel: LanguageActivityViewModel by viewModels()
    private var lang: LanguageModel? = null

    override fun setUp() {
        onBackPressedDispatcher.addCallback(this@LanguageActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        if (DataLocalManager.getBoolean(IS_SHOW_BACK, false)) {
            DataLocalManager.getLanguage(CURRENT_LANGUAGE)?.let { lang = it }
            binding.ivBack.visible()
        } else {
            binding.ivBack.invisible()
        }
        binding.ivTick.invisible()

        langAdapter.callBack = object : ICallBackItem {
            override fun callBack(ob: Any?, position: Int) {
                lang = ob as LanguageModel
                binding.ivTick.visible()
            }
        }

        binding.rcvLanguage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvLanguage.adapter = langAdapter

        binding.ivBack.setOnUnDoubleClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivTick.setOnUnDoubleClickListener {
            lang?.let {
                DataLocalManager.setLanguage(CURRENT_LANGUAGE, it)
                startIntent(OnBoardingActivity::class.java.name, true)
                finishAffinity()
            } ?: run { showToast(getString(R.string.you_need_pick_a_language), Gravity.CENTER) }
        }

        lifecycleScope.launch {
            viewModel.getAllLanguage()
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateLanguage.collect {
                    when (it) {
                        is UiState.Loading -> {}
                        is UiState.Error -> {}
                        is UiState.Success -> {
                            if (it.data.isNotEmpty()) langAdapter.setData(it.data)
                        }
                    }
                }
            }
        }
    }
}
