package com.tp.forestfiremonitor.extension

import com.google.android.gms.maps.model.LatLng
import com.tp.forestfiremonitor.data.area.model.Coordinate

fun Coordinate.toLatLng() = LatLng(latitude, longitude)

fun LatLng.toCoordinate() =
    Coordinate(latitude, longitude)