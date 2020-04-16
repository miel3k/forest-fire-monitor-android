package com.tp.forestfiremonitor.data.area.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tp.forestfiremonitor.data.area.model.Area

@Dao
interface AreaDao {
    @Query("SELECT * FROM Area WHERE areaId=:id")
    fun getArea(id: String): LiveData<Area>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArea(area: Area)
}