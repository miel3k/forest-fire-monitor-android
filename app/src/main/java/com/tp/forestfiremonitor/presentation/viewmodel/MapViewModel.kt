package com.tp.forestfiremonitor.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.tp.base.MultipleLiveData
import com.tp.base.RepositoryResult
import com.tp.forestfiremonitor.data.area.model.Area
import com.tp.forestfiremonitor.data.area.model.Coordinate
import com.tp.forestfiremonitor.data.area.repository.AreaDataSource
import com.tp.forestfiremonitor.data.fire.repository.FireDataSource
import com.tp.forestfiremonitor.extension.toCoordinate
import com.tp.forestfiremonitor.presentation.Item
import kotlinx.coroutines.launch
import java.util.*

class MapViewModel(
    private val areaRepository: AreaDataSource,
    private val fireRepository: FireDataSource
) : ViewModel() {

    private val selectedItemId = MutableLiveData<String>().apply { value = "" }
    val isEditAreaModeEnabled = MutableLiveData<Boolean>().apply { value = false }
    private val area: LiveData<Area> by lazy {
        MediatorLiveData<Area>().apply {
            viewModelScope.launch {
                addSource(areaRepository.getArea()) {
                    value = it ?: Area()
                }
            }
        }
    }
    val items: MutableLiveData<List<Item>> by lazy {
        MultipleLiveData<List<Item>>().apply {
            addSources(
                area,
                isEditAreaModeEnabled,
                selectedItemId
            ) { area, isEditAreaModeEnabled, selectedItemId ->
                val itemList = area.coordinates.toItems()
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
        if (!isEditAreaModeEnabled()) return
        val items = items.value
        if (items.isNullOrEmpty()) return
        val coordinates = items.toCoordinates()
        val area = Area(coordinates = coordinates)
        viewModelScope.launch { areaRepository.saveArea(area) }
        isEditAreaModeEnabled.value = false
    }

    fun onMapClick(latLng: LatLng) {
        if (!isEditAreaModeEnabled()) return
        val newItem = Item(UUID.randomUUID().toString(), latLng.toCoordinate())
        val newItems = items.value.orEmpty() + newItem
        items.value = newItems
    }

    fun onMarkerClick(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val item = items.value?.find { it.id == marker.tag } ?: return
        selectedItemId.value = item.id
    }

    fun onMarkerDragEnd(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val newItems = items.value
            ?.map { Item(it.id, getMarkerPositionOrDefault(it, marker), it.isDraggable) }
            .orEmpty()
        items.value = newItems
    }

    fun onMarkerDrag(marker: Marker) {
        if (!isEditAreaModeEnabled()) return
        val newPolygonItems = polygonItems.value
            ?.map { Item(it.id, getMarkerPositionOrDefault(it, marker), it.isDraggable) }
            .orEmpty()
        polygonItems.value = newPolygonItems
    }

    fun loadFires() {
        val areaCoordinates = area.value?.coordinates
        if (areaCoordinates.isNullOrEmpty()) {
            return
        }
        val polygonCoordinates = areaCoordinates + areaCoordinates.first()
        viewModelScope.launch {
            when (val result = fireRepository.searchFires(polygonCoordinates)) {
                is RepositoryResult.Success -> {
                    Log.i("MapViewModel", "Current fires success = ${result.data}")
                }
                is RepositoryResult.Error -> {
                    Log.e("MapViewModel", "Current fires error")
                }
            }
        }
    }

    private fun getMarkerPositionOrDefault(item: Item, marker: Marker) =
        if (item.id == marker.tag) marker.position.toCoordinate() else item.coordinate

    private fun isEditAreaModeEnabled() = isEditAreaModeEnabled.value == true

    private fun List<Coordinate>.toItems() = map { Item(UUID.randomUUID().toString(), it) }

    private fun List<Item>.toCoordinates() = map { it.coordinate }
}