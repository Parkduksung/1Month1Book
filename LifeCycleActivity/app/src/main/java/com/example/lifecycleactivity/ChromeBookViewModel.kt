package com.example.lifecycleactivity

import android.util.Log
import androidx.lifecycle.ViewModel

class ChromeBookViewModel : ViewModel() {

    override fun onCleared() {
        Log.d("결과", "ChromeBookViewModel onCleared")
        super.onCleared()
    }
}