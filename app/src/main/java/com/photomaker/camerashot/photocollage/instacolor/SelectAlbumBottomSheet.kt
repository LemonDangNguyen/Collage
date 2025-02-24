package com.photomaker.camerashot.photocollage.instacolor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.photomaker.camerashot.photocollage.instacolor.extensions.gone
import com.photomaker.camerashot.photocollage.instacolor.extensions.visible
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig
import com.photomaker.camerashot.photocollage.instacolor.utils.AdsConfig.haveNetworkConnection
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlbn.ads.callback.NativeCallback
import com.nlbn.ads.util.Admob
import com.nlbn.ads.util.ConsentHelper
import com.photomaker.camerashot.photocollage.instacolor.AlbumAdapter
import com.photomaker.camerashot.photocollage.instacolor.R
import com.photomaker.camerashot.photocollage.instacolor.databinding.AdsNativeBotHorizontalMediaLeftBinding
import com.photomaker.camerashot.photocollage.instacolor.databinding.FragmentSelectAlbumBinding

class SelectAlbumBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentSelectAlbumBinding? = null
    private val binding get() = _binding!!

    private val albumList = mutableListOf<AlbumModel>()
    private lateinit var albumAdapter: AlbumAdapter
    private val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    private var albumSelectedListener: OnAlbumSelectedListener? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                loadAlbums()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAlbumSelectedListener) {
            albumSelectedListener = context
        } else {
            throw RuntimeException("$context must implement OnAlbumSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
            dialog?.let {
                val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    val behavior = BottomSheetBehavior.from(sheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.isHideable = false

                    val layoutParams = sheet.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    sheet.layoutParams = layoutParams
                }
            }
        }

        setupRecyclerView()

        if (hasStoragePermissions()) {
            loadAlbums()
        } else {
            permissionLauncher.launch(storagePermissions)
        }
        showNative()
        binding.btnBack.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter(requireContext(), albumList) { album ->
            albumSelectedListener?.onAlbumSelected(album.name)
            dismiss()
        }

        binding.selectedAlbum.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = albumAdapter
        }
    }

    private fun loadAlbums() {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} IS NOT NULL"
        val albumMap = mutableMapOf<String, AlbumModel>()
        var recentImagesCount = 0
        var recentCoverImagePath: String? = null

        requireContext().contentResolver.query(uri, projection, selection, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            if (cursor.count == 0) {
                Toast.makeText(requireContext(), "No albums found", Toast.LENGTH_SHORT).show()
                return
            }
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val albumName = cursor.getString(nameIndex)
                val coverImagePath = cursor.getString(pathIndex)
                if (recentCoverImagePath == null) {
                    recentCoverImagePath = coverImagePath
                }
                recentImagesCount++
                if (!albumMap.containsKey(albumName)) {
                    albumMap[albumName] = AlbumModel(
                        name = albumName,
                        coverImagePath = coverImagePath,
                        numberOfImages = 1
                    )
                } else {
                    albumMap[albumName]?.numberOfImages =
                        (albumMap[albumName]?.numberOfImages ?: 0) + 1
                }
            }
            if (recentImagesCount > 0) {
                albumList.add(
                    0,
                    AlbumModel(
                        name = "All Images",
                        coverImagePath = recentCoverImagePath ?: "",
                        numberOfImages = recentImagesCount
                    )
                )
            }
            albumList.addAll(albumMap.values)
            albumAdapter.notifyDataSetChanged()
        } ?: run {
            Toast.makeText(requireContext(), "Failed to load albums", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasStoragePermissions(): Boolean =
        storagePermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNative() {
        if (haveNetworkConnection(requireContext()) && ConsentHelper.getInstance(requireContext()).canRequestAds()
            && AdsConfig.is_load_native_select_albums) {
            binding.rlNative.visible()
            AdsConfig.nativeAll?.let {
                pushViewAds(it)
            } ?: run {
                Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_all),
                    object : NativeCallback() {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            pushViewAds(nativeAd)
                        }

                        override fun onAdFailedToLoad() {
                            binding.frNativeAds.removeAllViews()
                        }
                    }
                )
            }
        } else binding.rlNative.gone()
    }
    private fun pushViewAds(nativeAd: NativeAd) {
        val adView = AdsNativeBotHorizontalMediaLeftBinding.inflate(layoutInflater)

        if (!AdsConfig.isLoadFullAds())
            adView.adUnitContent.setBackgroundResource(R.drawable.bg_native)
        else adView.adUnitContent.setBackgroundResource(R.drawable.bg_native_no_stroke)

        binding.frNativeAds.removeAllViews()
        binding.frNativeAds.addView(adView.root)
        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView.root)
    }
}
