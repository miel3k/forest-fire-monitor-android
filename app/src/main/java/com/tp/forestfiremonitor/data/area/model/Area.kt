package com.tp.forestfiremonitor.data.area.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Area")
data class Area @JvmOverloads constructor(
    @PrimaryKey
    @ColumnInfo(name = "areaId")
    var id: String = AREA_ID,
    @ColumnInfo(name = "coordinates")
    var coordinates: List<Coordinate> = listOf()
) {
    companion object {
        const val AREA_ID = "AREA_ID"
    }
}