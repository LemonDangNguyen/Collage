package com.example.collageimage.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collageimage.R
import com.example.collageimage.Setting
import com.example.collageimage.TemplateActivity
import com.example.collageimage.TemplateAdapter
import com.example.collageimage.databinding.FragmentTemplateBinding
import com.example.collageimage.view_template.SpaceItemDecoration

class TemplateFragment : Fragment() {

    private var _binding: FragmentTemplateBinding? = null
    private val binding get() = _binding!!

    private val imageList = listOf(
        R.drawable.templatee01,
        R.drawable.templatee02,
        R.drawable.templatee03,
        R.drawable.templatee04,
        R.drawable.templatee05,
        R.drawable.templatee06,
        R.drawable.templatee07,
        R.drawable.templatee08,
        R.drawable.templatee09,
        R.drawable.templatee10,
        R.drawable.templatee11,
        R.drawable.templatee12,
        R.drawable.templatee13,
        R.drawable.templatee14,
        R.drawable.templatee15,
        R.drawable.templatee16,
        R.drawable.templatee17,
        R.drawable.templatee18,
        R.drawable.templatee19,
        R.drawable.templatee20,
        R.drawable.templatee21,
        R.drawable.templatee22,
        R.drawable.templatee23,
        R.drawable.templatee24,
        R.drawable.templatee25,
        R.drawable.templatee26,
        R.drawable.templatee27,
        R.drawable.templatee28,
        R.drawable.templatee29,
        R.drawable.templatee30
    )

    private lateinit var templateAdapter: TemplateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cài đặt RecyclerView
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL)
        binding.rvTemplate.layoutManager = gridLayoutManager
        templateAdapter = TemplateAdapter(imageList)
        binding.rvTemplate.adapter = templateAdapter

        val spaceDecoration = SpaceItemDecoration(32)
        binding.rvTemplate.addItemDecoration(spaceDecoration)

        templateAdapter.setOnItemClickListener { imageId ->
            val intent = Intent(requireContext(), TemplateActivity::class.java)
            intent.putExtra("imageId", imageId)
            startActivity(intent)
        }

        binding.btnSetting.setOnClickListener {
            startActivity(Intent(requireContext(), Setting::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
