package com.tp.forestfiremonitor.data.area.repository

import androidx.lifecycle.LiveData
import com.tp.forestfiremonitor.data.area.model.Area

interface AreaDataSource {

    suspend fun getArea(): LiveData<Area> {
        throw NotImplementedError(this::class.java.name)
    }

    suspend fun saveArea(area: Area) {
        throw NotImplementedError(this::class.java.name)
    }
}