package com.hypersoft.puzzlelayouts.app.features.layouts.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.collageimage.databinding.ItemPuzzleLayoutsPieceBinding
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.layouts.slant.NumberSlantLayout
import com.hypersoft.pzlayout.layouts.straight.NumberStraightLayout

class AdapterPuzzleLayoutsPieces(
    private val itemClick: (puzzleLayout: PuzzleLayout, theme: Int) -> Unit
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
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPuzzleLayoutsPieceBinding.inflate(layoutInflater, parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val currentItem = puzzleLayouts[position]
        bindViews(holder, currentItem)

        holder.binding.puzzle.apply {
            when (position) {
                selectedPosition -> setLineColor(Color.parseColor("#3B83FC"))
                else -> setLineColor(Color.DKGRAY)
            }
            setPiecePadding(4.0F)
        }

        holder.binding.root.setOnClickListener {
            tempSelectedPosition = position
            confirmSelection()
            notifyDataSetChanged()
        }
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

    private fun bindViews(holder: CustomViewHolder, currentItem: PuzzleLayout) {
        holder.binding.puzzle.needDrawLine = true
        holder.binding.puzzle.needDrawOuterLine = true
        holder.binding.puzzle.isTouchEnable = false
        holder.binding.puzzle.setPuzzleLayout(currentItem)
    }

    inner class CustomViewHolder(val binding: ItemPuzzleLayoutsPieceBinding) :
        RecyclerView.ViewHolder(binding.root)
}

