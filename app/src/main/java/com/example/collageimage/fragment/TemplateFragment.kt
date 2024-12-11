package com.example.collageimage.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.collageimage.R
import com.example.collageimage.Setting
import com.example.collageimage.TemplateActivity
import com.example.collageimage.image_template.ImageTemplateAdapter
import com.example.collageimage.databinding.FragmentTemplateBinding
import com.example.collageimage.image_template.ImageTemplateViewModel
import com.example.collageimage.image_template.ImagetemplateModel
import com.example.collageimage.view_template.SpaceItemDecoration

class TemplateFragment : Fragment() {

    private var _binding: FragmentTemplateBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageTemplateAdapter: ImageTemplateAdapter
    private lateinit var imageTemplateViewModel: ImageTemplateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageTemplateViewModel = ViewModelProvider(this).get(ImageTemplateViewModel::class.java)

        // Quan sát dữ liệu từ ViewModel
        imageTemplateViewModel.imageList.observe(viewLifecycleOwner, Observer { imageList ->
            // Khởi tạo adapter với dữ liệu nhận được
            imageTemplateAdapter = ImageTemplateAdapter(imageList)


            imageTemplateAdapter.setOnItemClickListener { imageId ->
                val intent = Intent(requireContext(), TemplateActivity::class.java)
                intent.putExtra("imageId", imageId)
                startActivity(intent)
            }


            // Thiết lập RecyclerView
            setupRecyclerView(imageList)
        })
    }

    private fun setupRecyclerView(imageList: List<ImagetemplateModel>) {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        binding.rvTemplate.layoutManager = gridLayoutManager
        binding.rvTemplate.adapter = imageTemplateAdapter

        // Thêm khoảng cách giữa các item trong RecyclerView
        val spaceDecoration = SpaceItemDecoration(32)
        binding.rvTemplate.addItemDecoration(spaceDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
