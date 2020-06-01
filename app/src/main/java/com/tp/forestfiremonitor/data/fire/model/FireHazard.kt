package com.tp.forestfiremonitor.data.fire.model

import com.squareup.moshi.Json

data class FireHazard(
    @field:Json(name = "hazard") val hazard: Double,
    @field:Json(name = "point") val hazardPoint: HazardPoint
)