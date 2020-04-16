package com.tp.forestfiremonitor.map

data class Item(
    val id: String,
    val coordinate: Coordinate,
    val isDraggable: Boolean = false
)