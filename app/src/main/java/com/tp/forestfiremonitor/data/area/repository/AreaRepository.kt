package com.tp.forestfiremonitor.data.area.repository

import com.tp.forestfiremonitor.data.area.local.AreaDao
import com.tp.forestfiremonitor.data.area.model.Area
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AreaRepository(
    private val areaDao: AreaDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AreaDataSource {

    override suspend fun getArea() = withContext(dispatcher) {
        return@withContext areaDao.getArea(Area.AREA_ID)
    }

    override suspend fun saveArea(area: Area) = withContext(dispatcher) {
        areaDao.insertArea(area)
    }
}