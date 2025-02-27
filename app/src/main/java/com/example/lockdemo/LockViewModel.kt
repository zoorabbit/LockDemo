package com.example.lockdemo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class LockViewModel : ViewModel() {
    val isLockFlow = MutableStateFlow(false)
    val translationFlow = MutableStateFlow(0f)
}
