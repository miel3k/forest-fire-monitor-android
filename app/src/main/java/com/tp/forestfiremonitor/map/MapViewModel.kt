package com.tp.forestfiremonitor.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {

    val isEditAreaModeEnabled = MutableLiveData<Boolean>().apply { value = false }

    fun openEditAreaMode() {
        isEditAreaModeEnabled.value = true
    }

    fun closeEditAreaMode() {
        isEditAreaModeEnabled.value = false
    }

    fun saveEditArea() {

    }
}