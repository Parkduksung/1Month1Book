package com.example.study_notification.language

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.study_notification.R
import com.example.study_notification.databinding.ActivityChangeLanguageBinding


class ChangeLanguage : AppCompatActivity() {

    private lateinit var binding: ActivityChangeLanguageBinding

    private val viewModel by lazy { ChangeLanguageViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_language)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.run {
            vm = viewModel
        }

        viewModel.type.observe(this, Observer {
            Log.d("결과" ,it)
        })

    }
}