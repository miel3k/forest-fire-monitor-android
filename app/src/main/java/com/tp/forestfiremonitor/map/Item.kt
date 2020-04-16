package com.tp.forestfiremonitor.map

import com.tp.forestfiremonitor.data.area.model.Coordinate

data class Item(
    val id: String,
    val coordinate: Coordinate,
    val isDraggable: Boolean = false
)