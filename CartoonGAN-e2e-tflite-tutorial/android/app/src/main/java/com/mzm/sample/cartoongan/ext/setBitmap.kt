package com.mzm.sample.cartoongan.ext

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("setBitmap")
fun ImageView.setBitmap(bitmap: Bitmap?) {
    setImageBitmap(bitmap)
}