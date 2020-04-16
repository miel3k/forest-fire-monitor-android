package com.tp.forestfiremonitor.map.view

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tp.forestfiremonitor.R
import com.tp.forestfiremonitor.extension.toLatLng
import com.tp.forestfiremonitor.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel by viewModel<MapViewModel>()
    private lateinit var map: GoogleMap

    private var editAreaActionMode: ActionMode? = null
    private val editAreaActionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_item_save -> {
                    viewModel.saveEditArea()
                    true
                }
                else -> true
            }
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_edit_area_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            editAreaActionMode = null
        }
    }
    private var editAreaPolygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_edit_area_mode -> {
                viewModel.openEditAreaMode()
                true
            }
            else -> false
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        editAreaActionMode?.let {
            if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                viewModel.closeEditAreaMode()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupInitialCameraPosition()
        setupOnMapClickListener()
        setupOnMarkerClickListener()
        setupOnMarkerDragListener()
        setupIsEditAreaModeEnabledObserver()
        setupItemsObserver()
        setupPolygonItemsObserver()
    }

    private fun setupInitialCameraPosition() {
        val sydney = LatLng(-34.0, 151.0)
        val cameraPosition = CameraPosition
            .builder()
            .target(sydney)
            .zoom(2f)
            .build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun setupOnMapClickListener() {
        map.setOnMapClickListener {
            viewModel.onMapClick(it)
        }
    }

    private fun setupOnMarkerClickListener() {
        map.setOnMarkerClickListener {
            viewModel.onMarkerClick(it)
            true
        }
    }

    private fun setupOnMarkerDragListener() {
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker) {
                viewModel.onMarkerDragEnd(marker)
            }

            override fun onMarkerDragStart(marker: Marker) {
                viewModel.onMarkerDrag(marker)
            }

            override fun onMarkerDrag(marker: Marker) {
                viewModel.onMarkerDrag(marker)
            }
        })
    }

    private fun setupIsEditAreaModeEnabledObserver() {
        viewModel.isEditAreaModeEnabled.observe(this, Observer {
            if (it) {
                editAreaActionMode = startSupportActionMode(editAreaActionModeCallback)
                setupCloseActionModeButton()
            } else {
                editAreaActionMode?.finish()
            }
        })
    }

    private fun setupCloseActionModeButton() {
        val closeButton = findViewById<View>(androidx.appcompat.R.id.action_mode_close_button)
        closeButton?.setOnClickListener {
            viewModel.closeEditAreaMode()
        }
    }

    private fun setupItemsObserver() {
        viewModel.items.observe(this, Observer { coordinates ->
            map.clear()
            coordinates.forEach {
                val markerOptions = MarkerOptions()
                    .position(it.coordinate.toLatLng())
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerIconColor(it.isDraggable)))
                    .draggable(it.isDraggable)
                map.addMarker(markerOptions).apply { tag = it.id }
            }
        })
    }

    private fun getMarkerIconColor(isDraggable: Boolean): Float {
        return if (isDraggable) {
            BitmapDescriptorFactory.HUE_BLUE
        } else {
            BitmapDescriptorFactory.HUE_RED
        }
    }

    private fun setupPolygonItemsObserver() {
        viewModel.polygonItems.observe(this, Observer { coordinates ->
            editAreaPolygon?.remove()
            if (coordinates.isEmpty()) return@Observer
            val polygonOptions = PolygonOptions()
                .strokeColor(Color.BLUE)
                .fillColor(Color.LTGRAY)
                .addAll(coordinates.map { it.coordinate.toLatLng() })
            editAreaPolygon = map.addPolygon(polygonOptions)
        })
    }
}
