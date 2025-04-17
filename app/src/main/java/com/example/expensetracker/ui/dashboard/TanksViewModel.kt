package com.example.expensetracker.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TanksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Tanks Fragment"
    }
    val text: LiveData<String> = _text
}