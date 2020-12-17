package com.example.study_notification.ext

import android.widget.EditText
import androidx.databinding.BindingAdapter

@BindingAdapter("privateImeOptions")
fun EditText.privateImeOptions(string: String?) {
    privateImeOptions = string
}