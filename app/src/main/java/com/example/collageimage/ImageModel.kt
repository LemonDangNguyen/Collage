package com.example.collageimage

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class ImageModel(
    val id: Long,
    val dateTaken: Long,
    val fileName: String,
    val filePath: String,
    val album: String,
    val selected: Boolean = false,
    val uri: Uri,
    val isCameraItem: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(), // For the 'selected' boolean
        parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY, // Handle 'uri'
        parcel.readByte() != 0.toByte() // For the 'isCameraItem' boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(dateTaken)
        parcel.writeString(fileName)
        parcel.writeString(filePath)
        parcel.writeString(album)
        parcel.writeByte(if (selected) 1 else 0) // Write 'selected' as a byte
        parcel.writeParcelable(uri, flags) // Write 'uri'
        parcel.writeByte(if (isCameraItem) 1 else 0) // Write 'isCameraItem' as a byte
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageModel> {
        override fun createFromParcel(parcel: Parcel): ImageModel {
            return ImageModel(parcel)
        }

        override fun newArray(size: Int): Array<ImageModel?> {
            return arrayOfNulls(size)
        }
    }
}
