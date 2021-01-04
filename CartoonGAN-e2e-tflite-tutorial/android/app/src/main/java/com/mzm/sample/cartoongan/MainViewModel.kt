package com.mzm.sample.cartoongan

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mzm.sample.cartoongan.ml.LiteModelCartoonganFp161
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model

class MainViewModel : ViewModel() {

    private var cartoonImage: Bitmap? = null

    private val _bitmapTransform = MutableLiveData<Bitmap>()
    val bitmapTransform: LiveData<Bitmap>
        get() = _bitmapTransform
    private val options = Model.Options.Builder()
        .setNumThreads(4)
        .build()


    private var isImageProcess = false

    fun inputSource(bitmap: Bitmap, context: Context) {
        if (!isImageProcess) {
            isImageProcess = true
            Thread {
                val input = TensorImage.fromBitmap(bitmap)
                val output = inferenceWithFp16Model(input, context).bitmap
                cartoonImage = output
                isImageProcess = false
            }.start()
        }
        _bitmapTransform.value = cartoonImage ?: bitmap
    }

    private fun inferenceWithFp16Model(sourceImage: TensorImage, context: Context): TensorImage {
        val model = LiteModelCartoonganFp161.newInstance(context, options)
        // Runs model inference and gets result.
        val outputs = model.process(sourceImage)
        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage

        // Releases model resources if no longer used.
        model.close()

        return cartoonizedImage
    }
}