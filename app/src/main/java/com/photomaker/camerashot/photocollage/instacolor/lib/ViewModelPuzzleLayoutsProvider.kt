package com.hypersoft.puzzlelayouts.app.features.layouts.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.photomaker.camerashot.photocollage.instacolor.lib.UseCasePuzzleLayouts
import com.photomaker.camerashot.photocollage.instacolor.lib.ViewModelPuzzleLayouts

class ViewModelPuzzleLayoutsProvider(private val useCasePuzzleLayouts: UseCasePuzzleLayouts) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelPuzzleLayouts::class.java)) {
            return ViewModelPuzzleLayouts(useCasePuzzleLayouts) as T
        }
        return super.create(modelClass)
    }
}