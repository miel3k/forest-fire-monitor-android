package com.tp.forestfiremonitor.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tp.forestfiremonitor.data.area.converter.CoordinateConverters
import com.tp.forestfiremonitor.data.area.local.AreaDao
import com.tp.forestfiremonitor.data.area.model.Area

@Database(entities = [Area::class], version = 1, exportSchema = false)
@TypeConverters(CoordinateConverters::class)
abstract class ForestFireMonitorDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
}