package com.tp.forestfiremonitor.data.fire.model

import com.squareup.moshi.Json

class FiresResult(
    @field:Json(name = "currentFires") val currentFires: List<CurrentFire>,
    @field:Json(name = "hazard") val fireHazards: List<FireHazard>
)