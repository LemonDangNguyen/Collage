package com.nmh.base.project.utils

import android.view.Gravity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.collageimage.NMHApp
import com.example.collageimage.R
import com.example.collageimage.databinding.DialogRateBinding
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.nmh.base_lib.callback.ICallBackCheck
import com.nmh.base.project.extensions.setUpDialog
import com.nmh.base.project.extensions.showToast
import com.nmh.base.project.helpers.IS_RATED
import com.nmh.base.project.sharepref.DataLocalManager
import com.nmh.base.project.utils.ActionUtils.sendFeedback


object UtilsRate {

    private lateinit var reviewManager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    fun showRate(activity: AppCompatActivity, isFinish: Boolean, isGone: ICallBackCheck) {
        val dialogBinding = DialogRateBinding.inflate(LayoutInflater.from(activity))

        val dialog = AlertDialog.Builder(activity, R.style.SheetDialog).create()
        dialog.setUpDialog(dialogBinding.root, false)

        dialogBinding.root.layoutParams.width = (84.84f * NMHApp.w).toInt()
        dialogBinding.root.layoutParams.height = (105.05f * NMHApp.w).toInt()

        var rating = 5

        dialogBinding.rate5.setOnClickListener {
            dialogBinding.apply {
                rate5.setImageResource(R.drawable.ic_rate_star_on)
                rate4.setImageResource(R.drawable.ic_rate_star_on)
                rate3.setImageResource(R.drawable.ic_rate_star_on)
                rate2.setImageResource(R.drawable.ic_rate_star_on)
                rate1.setImageResource(R.drawable.ic_rate_star_on)

                ivIconRate.setImageResource(R.drawable.img_rate_5)
                tvTitleRate.text = activity.getString(R.string.des_rate_4_5)
                tvDesRate.text = activity.getString(R.string.thanks_for_your_feedback)
            }
            rating = 5
        }
        dialogBinding.rate4.setOnClickListener {
            dialogBinding.apply {
                rate5.setImageResource(R.drawable.ic_rate_star_off_5)
                rate4.setImageResource(R.drawable.ic_rate_star_on)
                rate3.setImageResource(R.drawable.ic_rate_star_on)
                rate2.setImageResource(R.drawable.ic_rate_star_on)
                rate1.setImageResource(R.drawable.ic_rate_star_on)

                ivIconRate.setImageResource(R.drawable.img_rate_4)
                tvTitleRate.text = activity.getString(R.string.des_rate_4_5)
                tvDesRate.text = activity.getString(R.string.thanks_for_your_feedback)
            }
            rating = 4
        }
        dialogBinding.rate3.setOnClickListener {
            dialogBinding.apply {
                rate5.setImageResource(R.drawable.ic_rate_star_off_5)
                rate4.setImageResource(R.drawable.ic_rate_star_off)
                rate3.setImageResource(R.drawable.ic_rate_star_on)
                rate2.setImageResource(R.drawable.ic_rate_star_on)
                rate1.setImageResource(R.drawable.ic_rate_star_on)

                ivIconRate.setImageResource(R.drawable.img_rate_3)
                tvTitleRate.text = activity.getString(R.string.des_rate_1_2_3)
                tvDesRate.text = activity.getString(R.string.please_give_us_some_feedback)
            }
            rating = 3
        }
        dialogBinding.rate2.setOnClickListener {
            dialogBinding.apply {
                rate5.setImageResource(R.drawable.ic_rate_star_off_5)
                rate4.setImageResource(R.drawable.ic_rate_star_off)
                rate3.setImageResource(R.drawable.ic_rate_star_off)
                rate2.setImageResource(R.drawable.ic_rate_star_on)
                rate1.setImageResource(R.drawable.ic_rate_star_on)

                ivIconRate.setImageResource(R.drawable.img_rate_2)
                tvTitleRate.text = activity.getString(R.string.des_rate_1_2_3)
                tvDesRate.text = activity.getString(R.string.please_give_us_some_feedback)
            }
            rating = 2
        }
        dialogBinding.rate1.setOnClickListener {
            dialogBinding.apply {
                rate5.setImageResource(R.drawable.ic_rate_star_off_5)
                rate4.setImageResource(R.drawable.ic_rate_star_off)
                rate3.setImageResource(R.drawable.ic_rate_star_off)
                rate2.setImageResource(R.drawable.ic_rate_star_off)
                rate1.setImageResource(R.drawable.ic_rate_star_on)

                ivIconRate.setImageResource(R.drawable.img_rate_1)
                tvTitleRate.text = activity.getString(R.string.des_rate_1_2_3)
                tvDesRate.text = activity.getString(R.string.please_give_us_some_feedback)
            }
            rating = 1
        }

        dialogBinding.tvRate.setOnClickListener {
            DataLocalManager.setBoolean(IS_RATED, true)
            isGone.check(true)
            if (rating == 5) rateApp(activity, dialog) else sendFeedback(activity)
            dialog.cancel()
            if (isFinish) activity.finish()
        }
        dialogBinding.tvCancel.setOnClickListener {
            if (!isFinish) dialog.cancel() else activity.finish()
            isGone.check(false)
        }
    }

    private fun rateApp(activity: AppCompatActivity, dialog: AlertDialog) {
        if (reviewInfo != null) {
            val flow = reviewManager.launchReviewFlow(activity, reviewInfo!!)
            flow.addOnCompleteListener { _ ->
                activity.showToast(activity.getString(R.string.rate_thanks), Gravity.CENTER)
                dialog.dismiss()
            }
        } else {
            // Review info is not yet available
        }
    }
}