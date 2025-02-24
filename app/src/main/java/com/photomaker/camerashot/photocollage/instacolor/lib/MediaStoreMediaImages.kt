package com.example.selectpic.lib

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.photomaker.camerashot.photocollage.instacolor.ImageModel
import com.example.selectpic.ddat.ItemMediaImageFolder
import com.example.selectpic.ddat.MediaStoreUtils


import java.io.FileNotFoundException

@Suppress("UNREACHABLE_CODE")
class MediaStoreMediaImages(private val contentResolver: ContentResolver?) {

    fun getFolderNames(): List<ItemMediaImageFolder>? {
        if (contentResolver == null) {
            Log.e(TAG, "getFolderNames: ", NullPointerException("ContentResolver is null"))
            return null
        }

        val mediaStoreUtils = MediaStoreUtils().MediaStoreFolders()
        val mutableSet = mutableSetOf<ItemMediaImageFolder>()

        contentResolver.query(
            mediaStoreUtils.contentUri,
            mediaStoreUtils.projection,
            mediaStoreUtils.filePathSelection,
            mediaStoreUtils.filePathSelectionArgs,
            mediaStoreUtils.sortBy
        )?.use { cursor ->
            val folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val folderName = cursor.getString(folderColumn) ?: "Unknown"
                mutableSet.add(ItemMediaImageFolder(folderName))
            }
        } ?: run {
            Log.e(TAG, "getFolderNames: ", NullPointerException("Cursor is null"))
            return null
        }
        return mutableSet.toList()
    }

    fun getAllImages(): List<ImageModel>? {
        if (contentResolver == null) {
            Log.e(TAG, "getAllImages: ", NullPointerException("ContentResolver is null"))
            return null
        }

        val mediaStoreUtils = MediaStoreUtils().MediaStoreImages()
        val arrayList = ArrayList<ImageModel>()

        contentResolver.query(
            mediaStoreUtils.contentUri,
            mediaStoreUtils.projection,
            mediaStoreUtils.filePathSelection,
            mediaStoreUtils.filePathSelectionArgs,
            mediaStoreUtils.sortBy
        )?.use { cursor ->
            val columnId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val imageUri: Uri = ContentUris.withAppendedId(mediaStoreUtils.contentUri, cursor.getLong(columnId))
                val photo = ImageModel(
                    uri = imageUri,
                    id = TODO(),
                    dateTaken = TODO(),
                    fileName = TODO(),
                    filePath = TODO(),
                    album = TODO(),
                    selected = TODO()
                )
                arrayList.add(photo)
            }
            return arrayList
        } ?: kotlin.run {
            Log.e(TAG, "getAllImages: ", NullPointerException("Cursor is null"))
            return null
        }
    }

    fun getImages(folderName: String): List<ImageModel>? {
        if (contentResolver == null) {
            Log.e(TAG, "getImages: ", NullPointerException("ContentResolver is null"))
            return null
        }

        val mediaStoreUtils = MediaStoreUtils().MediaStoreImagesByFolder(folderName)
        val arrayList = ArrayList<ImageModel>()

        contentResolver.query(
            mediaStoreUtils.contentUri,
            mediaStoreUtils.projection,
            mediaStoreUtils.filePathSelection,
            mediaStoreUtils.filePathSelectionArgs,
            mediaStoreUtils.sortBy
        )?.use { cursor ->
            val columnId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val imageUri: Uri = ContentUris.withAppendedId(mediaStoreUtils.contentUri, cursor.getLong(columnId))
                val photo = ImageModel(
                    uri = imageUri,
                    id = TODO(),
                    dateTaken = TODO(),
                    fileName = TODO(),
                    filePath = TODO(),
                    album = TODO(),
                    selected = TODO()
                )
                arrayList.add(photo)
            }
            return arrayList
        } ?: kotlin.run {
            Log.e(TAG, "getImages: FolderName($folderName): ", NullPointerException("Cursor is null"))
            return null
        }
    }

    fun doesUriExist(imageUri: Uri): Boolean? {
        if (contentResolver == null) {
            Log.e(TAG, "getImages: ", NullPointerException("ContentResolver is null"))
            return null
        }

        val mediaStoreUtils = MediaStoreUtils().MediaStoreUriExist(imageUri)

        // For Android 11 and above, check if the file is trashed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            contentResolver.query(
                mediaStoreUtils.contentUri,
                mediaStoreUtils.projection,
                mediaStoreUtils.filePathSelection,
                mediaStoreUtils.filePathSelectionArgs,
                mediaStoreUtils.sortBy
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val isTrashed = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_TRASHED)) == 1
                    if (isTrashed) {
                        return false
                    }
                }
            } ?: run {
                return null
            }
        }
        try {
            contentResolver.openInputStream(imageUri)?.use {
                return true // File is accessible and exists
            } ?: run {
                return false
            }
        } catch (ex: FileNotFoundException) {
            return false
        }
    }
}