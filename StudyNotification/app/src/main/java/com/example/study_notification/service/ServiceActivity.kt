package com.example.study_notification.service

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class ServiceActivity : AppCompatActivity() {

    private val serviceViewModel by lazy {
        ViewModelProvider(this).get(ServiceViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(serviceViewModel)
    }

    override fun onDestroy() {
        lifecycle.removeObserver(serviceViewModel)
        super.onDestroy()
    }
}