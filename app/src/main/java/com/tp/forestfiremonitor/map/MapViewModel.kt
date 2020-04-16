package com.tp.forestfiremonitor.map

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.tp.base.MultipleLiveData
import com.tp.forestfiremonitor.extension.toCoordinate
import java.util.*

class MapViewModel : ViewModel() {

    private val itemList = listOf(
        Item(UUID.randomUUID().toString(), Coordinate(-27.47093, 153.0235)),
        Item(UUID.randomUUID().toString(), Coordinate(-33.87365, 151.20689)),
        Item(UUID.randomUUID().toString(), Coordinate(-34.92873, 138.59995)),
        Item(UUID.randomUUID().toString(), Coordinate(-12.4634, 130.8456))
    )
    private val selectedItemId = MutableLiveData<String>().apply { value = "" }
    val isEditAreaModeEnabled = MutableLiveData<Boolean>().apply { value = false }
    val items: MutableLiveData<List<Item>> by lazy {
        MultipleLiveData<List<Item>>().apply {
            addSources(
                isEditAreaModeEnabled,
                selectedItemId
            ) { isEditAreaModeEnabled, selectedItemId ->
                value = if (isEditAreaModeEnabled == true) {
                    val currentItems = value
                    val items = if (currentItems.isNullOrEmpty()) itemList else currentItems
                    items.map { Item(it.id, it.coordinate, isDraggable = it.id == selectedItemId) }
                } else {
                    itemList
                }
            }
        }
    }
    val polygonItems: MutableLiveData<List<Item>> by lazy {
        MediatorLiveData<List<Item>>().apply {
            addSource(items) {
                value = it
            }
        }
    }

    fun openEditAreaMode() {
        isEditAreaModeEnabled.value = true
    }

    fun closeEditAreaMode() {
        selectedItemId.value = ""
        isEditAreaModeEnabled.value = false
    }

    fun saveEditArea() {

    }

    fun onMapClick(latLng: LatLng) {
        if (!isEditAreaModeEnabled()) return
        val newItem = Item(UUID.randomUUID().toString(), latLng.toCoordinate())
        val newItems = items.value.orEmpty() + newItem
        items.value = newItems
        if (selectedItemId.value == null) {
            selectedItemId.value = newItem.id
        }
    }

    fun onMarkerClick(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val item = items.value?.find { it.id == marker.tag } ?: return
        selectedItemId.value = item.id
    }

    fun onMarkerDragEnd(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val newItems = items.value?.map {
            if (it.id == marker.tag) {
                Item(it.id, marker.position.toCoordinate(), it.isDraggable)
            } else {
                it
            }
        }.orEmpty()
        items.value = newItems
    }

    fun onMarkerDrag(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val newPolygonItems = polygonItems.value?.map {
            if (it.id == marker.tag) {
                Item(it.id, marker.position.toCoordinate(), it.isDraggable)
            } else {
                it
            }
        }.orEmpty()
        polygonItems.value = newPolygonItems
    }

    private fun isEditAreaModeEnabled() = isEditAreaModeEnabled.value == true
}