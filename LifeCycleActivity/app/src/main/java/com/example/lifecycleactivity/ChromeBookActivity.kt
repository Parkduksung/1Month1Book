package com.example.lifecycleactivity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class ChromeBookActivity : AppCompatActivity() {

    private lateinit var chromeBookViewModel: ChromeBookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrome_book)

        chromeBookViewModel = ViewModelProvider(this).get(ChromeBookViewModel::class.java)

        Log.d("결과", "ChromeBookActivity onCreate")

        if (packageManager.hasSystemFeature("org.chromium.arc.device_management")) {
            Log.d("결과", "크름북 0")
        } else {
            Log.d("결과", "크름북 X")
        }

    }

    override fun onStart() {
        Log.d("결과", "ChromeBookActivity onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d("결과", "ChromeBookActivity onResume")
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("결과", "ChromeBookActivity onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        Log.d("결과", "ChromeBookActivity onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("결과", "ChromeBookActivity onStop")
        super.onStop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("결과", "ChromeBookActivity onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d("결과", "ChromeBookActivity onDestroy")
        super.onDestroy()
    }
}