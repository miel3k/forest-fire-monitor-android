package com.tp.forestfiremonitor.data.fire.repository

import com.tp.base.data.RepositoryResult
import com.tp.forestfiremonitor.data.area.model.Coordinate
import com.tp.forestfiremonitor.data.area.model.Device
import com.tp.forestfiremonitor.data.fire.model.FiresResult

interface FireDataSource {

    suspend fun searchFires(coordinates: List<Coordinate>): RepositoryResult<FiresResult>

    suspend fun updateLocation(device: Device): RepositoryResult<String>

    suspend fun getFiresNotification(token: String): RepositoryResult<FiresResult>
}
