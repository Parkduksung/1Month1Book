package com.example.study_notification

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class LifeCycleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle)
        Log.d("생명주기순서", "onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("생명주기순서", "onStart")
    }


    override fun onResume() {
        Log.d("생명주기순서", "onResume")
        super.onResume()
    }


    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        Log.d("생명주기순서", "onPostCreate")
        super.onPostCreate(savedInstanceState, persistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d("생명주기순서", "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("생명주기순서", "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.d("생명주기순서", "onSaveInstanceState")
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("생명주기순서", "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        Log.d("생명주기순서", "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d("생명주기순서", "onDestroy")
        super.onDestroy()
    }
}