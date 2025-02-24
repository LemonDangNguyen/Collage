package com.example.selectpic.ddat

import android.net.Uri
import com.photomaker.camerashot.photocollage.instacolor.ImageModel


class UseCaseMediaImageDetail(private val repositoryMediaImages: RepositoryMediaImages) {

    /**
     * Sorted by Descending order
     */

    fun getImages(folderName: String): List<ImageModel>? {
        val shouldGetAllImages = folderName.equals(ConstantUtils.GALLERY_ALL, true)
        return when (shouldGetAllImages) {
            true -> repositoryMediaImages.getAllImages()
            false -> repositoryMediaImages.getImages(folderName)
        }
    }

    fun doesUriExist(imageUri: Uri): Boolean? {
        return repositoryMediaImages.doesUriExist(imageUri)
    }
}