package com.photomaker.camerashot.photocollage.instacolor.lib

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.layouts.slant.NumberSlantLayout
import com.hypersoft.pzlayout.layouts.straight.NumberStraightLayout
import com.photomaker.camerashot.photocollage.instacolor.databinding.ItemPuzzleLayoutsPieceBinding

class AdapterPuzzleLayoutsPieces(
    private val itemClick: (puzzleLayout: PuzzleLayout, theme: Int) -> Unit,
) : RecyclerView.Adapter<AdapterPuzzleLayoutsPieces.CustomViewHolder>() {


    private var puzzleLayouts: List<PuzzleLayout> = emptyList()
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var tempSelectedPosition: Int = RecyclerView.NO_POSITION
    private var previousSelectedPosition: Int = RecyclerView.NO_POSITION

    fun setPuzzleLayouts(newPuzzleLayouts: List<PuzzleLayout>) {
        this.puzzleLayouts = newPuzzleLayouts
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = puzzleLayouts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(ItemPuzzleLayoutsPieceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.onBind()
    }

    fun confirmSelection() {
        if (tempSelectedPosition != RecyclerView.NO_POSITION) {

            selectedPosition = tempSelectedPosition
            val selectedLayout = puzzleLayouts[selectedPosition]
            val theme = when (selectedLayout) {
                is NumberSlantLayout -> selectedLayout.theme
                is NumberStraightLayout -> selectedLayout.theme
                else -> 0
            }
            itemClick.invoke(selectedLayout, theme)
        }
        tempSelectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }



    fun discardSelection() {
        tempSelectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    inner class CustomViewHolder(val binding: ItemPuzzleLayoutsPieceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind() {
            val currentItem = puzzleLayouts[adapterPosition]

            binding.puzzle.apply {
                needDrawLine = true
                needDrawOuterLine = true
                isTouchEnable = false
                setPuzzleLayout(currentItem)
            }

            binding.puzzle.apply {
                setSelectedLineColor(Color.parseColor("#ff287a"))
                when (adapterPosition) {
                    selectedPosition -> setLineColor(Color.parseColor("#3B83FC"))
                    else -> setLineColor(Color.DKGRAY)
                }
                setPiecePadding(4.0F)
            }

            binding.root.setOnClickListener {
                tempSelectedPosition = adapterPosition
                confirmSelection()
                notifyDataSetChanged()
            }
        }
    }
}

