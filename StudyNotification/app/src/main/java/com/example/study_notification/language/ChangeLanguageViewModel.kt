package com.example.study_notification.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChangeLanguageViewModel : ViewModel() {

    private val _type = MutableLiveData<String>()
    val type: LiveData<String>
        get() = _type

    fun setENType() {
        _type.value = "defaultInputmode=english;"
    }

    fun setKRType() {
        _type.value = "defaultInputmode=korea;"
    }

    fun setNMType() {
        _type.value = "defaultInputmode=numeric;"
    }
}