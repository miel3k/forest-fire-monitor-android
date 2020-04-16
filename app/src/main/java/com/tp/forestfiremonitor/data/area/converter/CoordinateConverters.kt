package com.tp.forestfiremonitor.data.area.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tp.forestfiremonitor.data.area.model.Coordinate

class CoordinateConverters {

    private val moshi by lazy {
        val type = Types.newParameterizedType(List::class.java, Coordinate::class.java)
        Moshi.Builder().build().adapter<List<Coordinate>>(type)
    }

    @TypeConverter
    fun stringToCoordinates(data: String): List<Coordinate> {
        return moshi.fromJson(data) ?: emptyList()
    }

    @TypeConverter
    fun coordinatesToString(coordinates: List<Coordinate>): String {
        return moshi.toJson(coordinates)
    }
}