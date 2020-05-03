package com.tp.forestfiremonitor.data.fire.model

import com.squareup.moshi.Json

data class CurrentFire(
    @field:Json(name = "confidence") val confidence: String,
    @field:Json(name = "date") val date: String,
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double
)