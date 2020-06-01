package com.tp.forestfiremonitor.data.fire.model

import com.squareup.moshi.Json

data class HazardPoint(
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double
)