package com.example.selectpic.ddat

import android.net.Uri
import com.example.collageimage.ImageModel

import com.example.selectpic.lib.MediaStoreMediaImages

class RepositoryMediaImages(private val mediaStoreEnhanceGallery: MediaStoreMediaImages) {

    fun getFolderNames(): List<ItemMediaImageFolder>? {
        return mediaStoreEnhanceGallery.getFolderNames()
    }

    fun getAllImages(): List<ImageModel>? {
        return mediaStoreEnhanceGallery.getAllImages()
    }

    fun getImages(folderName: String): List<ImageModel>? {
        return mediaStoreEnhanceGallery.getImages(folderName)
    }

    fun doesUriExist(imageUri: Uri): Boolean? {
        return mediaStoreEnhanceGallery.doesUriExist(imageUri)
    }
}