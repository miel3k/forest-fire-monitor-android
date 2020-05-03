package com.tp.forestfiremonitor.data.fire.repository

import com.tp.base.RepositoryResult
import com.tp.forestfiremonitor.data.area.model.Coordinate
import com.tp.forestfiremonitor.data.fire.model.FiresResult

interface FireDataSource {

    suspend fun searchFires(coordinates: List<Coordinate>): RepositoryResult<FiresResult>
}