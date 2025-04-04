package com.example.expensetracker.ui.other

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OtherViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Other Fragment"
    }
    val text: LiveData<String> = _text
}