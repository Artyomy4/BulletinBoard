package com.teempton.DDSKot.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import com.squareup.picasso.Picasso
import com.teempton.DDSKot.adapters.ImageAdapter
import com.teempton.DDSKot.model.Ad
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream


object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImagesSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outHeight, options.outWidth)
    }


    fun chooseScaleType(im:ImageView, bitmap:Bitmap){
        if (bitmap.width > bitmap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        }else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResizeImage(uris: List<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            var size = getImagesSize(uris[n], act)
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
                    Picasso.get().load(uris[i])
                        .resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get()
                )
            }
        }


        return@withContext bitmapList
    }

    private suspend fun getBitMapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {

        val bitmapList = ArrayList<Bitmap>()

        for (i in uris.indices) {
            val e = kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get())
            }
        }
        return@withContext bitmapList
    }

    fun fillImageArray(ad: Ad, adapter:ImageAdapter){
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitMapFromUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }

}