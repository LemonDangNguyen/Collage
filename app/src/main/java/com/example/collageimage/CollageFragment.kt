package com.example.collageimage

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.collageimage.databinding.DialogExitBinding
import com.example.collageimage.databinding.FragmentCollageBinding


class CollageFragment : Fragment() {
   private val binding by lazy { FragmentCollageBinding.inflate(layoutInflater) }
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)




      binding.btnSetting.setOnClickListener {
         startActivity(Intent(requireContext(), Setting::class.java))
      }


   }
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View {
      return binding.root
   }

}