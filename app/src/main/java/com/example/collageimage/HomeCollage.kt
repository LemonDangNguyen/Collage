package com.example.collageimage;

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.collageimage.BaseActivity
import com.example.collageimage.MainActivity
import com.example.collageimage.R
import com.example.collageimage.databinding.ActivityHomeCollageBinding
import com.example.collageimage.databinding.DialogExitBinding
import com.example.selectpic.ddat.PuzzleUtils
import com.example.selectpic.ddat.RepoPuzzleUtils
import com.example.selectpic.ddat.RepositoryMediaImages
import com.example.selectpic.ddat.UseCaseMediaImageDetail
import com.example.selectpic.ddat.UseCasePuzzleLayouts
import com.example.selectpic.ddat.ViewModelMediaImageDetail
import com.example.selectpic.ddat.ViewModelMediaImageDetailProvider
import com.example.selectpic.lib.MediaStoreMediaImages
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.adapter.AdapterPuzzleLayoutsPieces
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayouts
import com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels.ViewModelPuzzleLayoutsProvider
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.utils.PuzzlePiece
import com.hypersoft.pzlayout.view.PuzzleView

class HomeCollage : BaseActivity(), PuzzleView.OnPieceClick, PuzzleView.OnPieceSelectedListener {
    private val binding by lazy { ActivityHomeCollageBinding.inflate(layoutInflater) }
    private val mediaStoreMediaImages by lazy { MediaStoreMediaImages(contentResolver) }
    private val useCaseMediaImageDetail by lazy { UseCaseMediaImageDetail(RepositoryMediaImages(mediaStoreMediaImages)) }
    private val viewModelMediaImageDetail by viewModels<ViewModelMediaImageDetail> { ViewModelMediaImageDetailProvider(useCaseMediaImageDetail) }
    private val viewModelPuzzleLayouts by viewModels<ViewModelPuzzleLayouts> { ViewModelPuzzleLayoutsProvider(UseCasePuzzleLayouts(RepoPuzzleUtils(PuzzleUtils()))) }

    private var mList: List<ImageModel> = mutableListOf()
    private val adapterPuzzleLayoutsPieces by lazy { AdapterPuzzleLayoutsPieces(itemClick) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val selectedImages: ArrayList<ImageModel>? = intent.getParcelableArrayListExtra("SELECTED_IMAGES")
        selectedImages?.let {
            mList = it
            fetchLayouts(it)
            checkImageSizeAndSetLayouts(it)
        } ?: Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()

        setupListeners()
        initObservers()
        initRecyclerView()
        initListener()
    }

    private fun initListener() = binding.apply {
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.puzzleView.setPieceRadian(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }
    private fun initRecyclerView() {
        binding.rcvListPuzzleLayouts.adapter = adapterPuzzleLayoutsPieces
    }
    private val itemClick: ((PuzzleLayout, theme: Int) -> Unit) = { _, theme ->
        viewModelPuzzleLayouts.getPuzzleLayout(1, mList.size, theme)
    }

    private fun initObservers() {
        viewModelMediaImageDetail.clickedImagesLiveData.observe(this) {
            mList = it
            if (mList.isEmpty()) {
                Toast.makeText(this, "No images", Toast.LENGTH_SHORT).show()
            } else {
                fetchLayouts(it)
                checkImageSizeAndSetLayouts(it)
            }
        }
        viewModelPuzzleLayouts.puzzleLayoutLiveData.observe(this) { list ->
            initView(list)
        }
        viewModelPuzzleLayouts.puzzleLayoutsLiveData.observe(this) { list ->
            adapterPuzzleLayoutsPieces.setPuzzleLayouts(list)
        }
    }

    private fun initView(list: PuzzleLayout) = binding.puzzleView.apply {
        val context: Context = this.context
        val ta = context.obtainStyledAttributes(
            null,
            com.hypersoft.pzlayout.R.styleable.PuzzleView
        )
        setPuzzleLayout(list)
        isTouchEnable = true
        selectedLineColor = ContextCompat.getColor(context, R.color.black)
        setHandleBarColor(ContextCompat.getColor(context, R.color.black))
        setAnimateDuration(700)
        piecePadding = 10f
        setOnPieceClickListener(this@HomeCollage)
        setOnPieceSelectedListener(this@HomeCollage)
        post { loadPhotoFromRes(list) }
    }
    private fun setupListeners() = binding.apply {
        listOf(pmirror to ::mirror, pflip to ::flip, protate to ::rotate,
            pzoomplus to ::zoomPlus, pzoomminus to ::zoomMinus,
            pleft to ::left, pright to ::right, pup to ::up, pdown to ::down).forEach { (view, action) ->
            view.setOnClickListener { action() }
        }
        btnCorner.setOnClickListener { corner() }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPhotoFromRes(list: PuzzleLayout) {
        val count = minOf(mList.size, list.areaCount)
        val pieces = mutableListOf<Bitmap>()

        for (i in 0 until count) {
            val target: CustomTarget<Bitmap> = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
                    pieces.add(bitmap)
                    if (pieces.size == count) {
                        val remainingPieces = if (mList.size < list.areaCount) List(list.areaCount) { pieces[it % count] } else pieces
                        binding.puzzleView.addPieces(remainingPieces)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Toast.makeText(this@HomeCollage, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
            Glide.with(this).asBitmap().load(mList[i].uri).into(target)
        }
    }

    private fun fetchLayouts(images: List<ImageModel>) {
        viewModelPuzzleLayouts.getPuzzleLayouts(images.size)
    }

    private fun checkImageSizeAndSetLayouts(images: List<ImageModel>) {
        val size = images.size
        if (size in 1..9) {
            val layoutImages = if (size == 1) listOf(images[0], images[0]) else images
            viewModelPuzzleLayouts.getPuzzleLayout(1, layoutImages.size, 0)
        }
    }

    private fun mirror() = handlePuzzleAction {binding.puzzleView.mirrorPiece() }
    private fun flip() = handlePuzzleAction { binding.puzzleView.flipPiece() }
    private fun rotate() = handlePuzzleAction { binding.puzzleView.rotatePiece() }
    private fun zoomPlus() = handlePuzzleAction { binding.puzzleView.zoomInPiece() }
    private fun zoomMinus() = handlePuzzleAction { binding.puzzleView.zoomOutPiece() }
    private fun left() = handlePuzzleAction { binding.puzzleView.moveLeft() }
    private fun right() = handlePuzzleAction { binding.puzzleView.moveRight() }
    private fun up() = handlePuzzleAction { binding.puzzleView.moveUp() }
    private fun down() = handlePuzzleAction { binding.puzzleView.moveDown() }

    private fun corner() = binding.apply {
        seekbar.visibility = View.VISIBLE
        seekbar.max = 100
        seekbar.progress = puzzleView.getPieceRadian().toInt()
    }

    private fun handlePuzzleAction(action: () -> Unit) {
        if (binding.puzzleView.handlingPiecePosition != -1) {
            action()
        } else {
            Toast.makeText(this, R.string.selectsingleimage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPieceClick() {}
    override fun onSwapGetPositions(pos1: Int, pos2: Int) {}
    override fun onPieceSelected(piece: PuzzlePiece?, position: Int) {}
    override fun onBackPressed(){

        val binding2 = DialogExitBinding.inflate(layoutInflater)
        val dialog2 = Dialog(this)
        dialog2.setContentView(binding2.root)
        val window = dialog2.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.setCanceledOnTouchOutside(false)
        dialog2.setCancelable(false)
        binding2.btnExit.setOnClickListener{
            dialog2.dismiss()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            super.onBackPressed()
        }
        binding2.btnStay.setOnClickListener{
            dialog2.dismiss()
        }
        dialog2.show()
    }
}