package com.teempton.DDSKot.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File


object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1
    fun getImagesSize(uri: String): List<Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(uri, options)
        return if (imageRotation(uri) == 0)
            listOf(options.outWidth, options.outHeight)
        else listOf(options.outHeight, options.outWidth)
    }

    private fun imageRotation(uri: String): Int {

        val imageFile = File(uri)
        val exif = ExifInterface(imageFile.absolutePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_180)
            90
        else
            0
    }

    fun chooseScaleType(im:ImageView, bitmap:Bitmap){
        if (bitmap.width > bitmap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResizeImage(uris: List<String>): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            var size = getImagesSize(uris[n])
            var imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()

            if (imageRatio > 1) {
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            } else {
                if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }

        }


        for (i in uris.indices) {
            val e = kotlin.runCatching {
                bitmapList.add(
                    Picasso.get().load(File(uris[i]))
                        .resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get()
                )
            }
        }


        return@withContext bitmapList
    }
}