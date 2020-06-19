package com.tp.forestfiremonitor.presentation.view

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.tp.base.extension.requestPermission
import com.tp.base.extension.toast
import com.tp.forestfiremonitor.ForestFireMonitorApplication
import com.tp.forestfiremonitor.R
import com.tp.forestfiremonitor.data.fire.model.CurrentFire
import com.tp.forestfiremonitor.data.fire.model.FireHazard
import com.tp.forestfiremonitor.data.fire.model.FiresResult
import com.tp.forestfiremonitor.extension.toLatLng
import com.tp.forestfiremonitor.presentation.Item
import com.tp.forestfiremonitor.presentation.viewmodel.MapViewModel
import com.tp.forestfiremonitor.service.LocationForegroundService
import com.tp.forestfiremonitor.service.NotificationEvents
import com.tp.forestfiremonitor.service.ServiceAction
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.concurrent.timerTask

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel by viewModel<MapViewModel>()
    private lateinit var map: GoogleMap
    private lateinit var timer: Timer

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
    private var balloon: Balloon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        NotificationEvents.serviceEvent.observe(this, Observer<FiresResult> {
            fireResult -> viewModel.handleNewNotification(fireResult)
        })
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

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    override fun onResume() {
        super.onResume()
        setupLoadFiresTimerTask()
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
        setupZoomControls()
        setupInitialCameraPosition()
        setupOnMapClickListener()
        setupOnMarkerClickListener()
        setupOnMarkerDragListener()
        setupMyLocation()
        setupIsEditAreaModeEnabledObserver()
        setupMapStateObserver()
        setupPolygonItemsObserver()
        setupErrorObserver()
        setupRemoveTooltipEventObserver()
        setupLoadFiresTimerTask()
        setupLocationService()
    }

    private fun setupLocationService() {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as LocationForegroundService.LocationForegroundServiceBinder
                val isServiceStarted = binder.isServiceStarted()
                Log.i(ForestFireMonitorApplication::class.java.simpleName,"Service started: $isServiceStarted")
            }
        }

        applicationContext?.let {
            val serviceIntent = getLocationForegroundServiceIntent(it)
            it.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            serviceIntent.setServiceAction(ServiceAction.START)
            ContextCompat.startForegroundService(it, serviceIntent)
        }
    }

    private fun Intent.setServiceAction(serviceAction: String) {
        action = serviceAction
    }

    private fun getLocationForegroundServiceIntent(
        context: Context,
        deviceId: String = "",
        interval: Int = LocationForegroundService.LOCATION_UPDATES_INTERVAL_DEFAULT_VALUE
    ) = Intent(context, LocationForegroundService::class.java).apply {
        putExtra(LocationForegroundService.DEVICE_ID, deviceId)
        putExtra(LocationForegroundService.LOCATION_UPDATES_INTERVAL, interval)
    }

    private fun setupZoomControls() {
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun setupLoadFiresTimerTask() {
        timer = Timer()
        val timerTask = timerTask {
            viewModel.loadFires()
        }
        timer.schedule(timerTask, 10000L, 10000L)
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
            it.showInfoWindow()
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

    private fun setupMyLocation() {
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            onGranted = { map.isMyLocationEnabled = true },
            onDenied = { map.isMyLocationEnabled = false }
        )
    }

    private fun setupIsEditAreaModeEnabledObserver() {
        viewModel.isEditAreaModeEnabled.observe(this, Observer {
            if (it) {
                editAreaActionMode = startSupportActionMode(editAreaActionModeCallback)
                setupCloseActionModeButton()
            } else {
                editAreaActionMode?.finish()
                balloon?.dismiss()
                balloon = null
            }
        })
    }

    private fun setupCloseActionModeButton() {
        val closeButton = findViewById<View>(androidx.appcompat.R.id.action_mode_close_button)
        closeButton?.setOnClickListener {
            viewModel.closeEditAreaMode()
        }
    }

    private fun getMarkerIconColor(isDraggable: Boolean): Float {
        return if (isDraggable) {
            BitmapDescriptorFactory.HUE_BLUE
        } else {
            BitmapDescriptorFactory.HUE_RED
        }
    }

    private fun setupPolygonItemsObserver() {
        viewModel.polygonItems.observe(this, Observer { polygonItems ->
            drawPolygonItems(polygonItems)
        })
    }

    private fun drawPolygonItems(items: List<Item>) {
        editAreaPolygon?.remove()
        if (items.isEmpty()) return
        val polygonOptions = PolygonOptions()
            .strokeColor(Color.BLUE)
            .fillColor(0x4F0077c2)
            .addAll(items.map { it.coordinate.toLatLng() })
        editAreaPolygon = map.addPolygon(polygonOptions)
    }

    private fun setupMapStateObserver() {
        viewModel.mapState.observe(this, Observer { mapState ->
            map.clear()
            drawItems(mapState.items)
            drawCurrentFires(mapState.currentFires)
            drawFireHazards(mapState.fireHazards)
        })
    }

    private fun drawItems(items: List<Item>) {
        items.forEach {
            val markerOptions = MarkerOptions()
                .position(it.coordinate.toLatLng())
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerIconColor(it.isDraggable)))
                .draggable(it.isDraggable)
            map.addMarker(markerOptions).apply { tag = it.id }
        }
    }

    private fun drawCurrentFires(currentFires: List<CurrentFire>) {
        currentFires.forEach {
            val markerOptions = MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("Date: " + it.date)
                .snippet("Confidence: " + it.confidence)
            map.addMarker(markerOptions)
        }
    }

    private fun drawFireHazards(fireHazards: List<FireHazard>) {
        fireHazards.forEach {
            val markerOptions = MarkerOptions()
                .position(LatLng(it.hazardPoint.latitude, it.hazardPoint.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title("Hazard: " + it.hazard)
            map.addMarker(markerOptions)
        }
    }

    private fun setupErrorObserver() {
        viewModel.error.observe(this, Observer {
            toast(it)
        })
    }

    private fun setupRemoveTooltipEventObserver() {
        viewModel.removeTooltipEvent.observe(this, Observer {
            balloon?.dismiss()
            balloon = null
            showRemoveTooltip(this)
        })
    }

    private fun showRemoveTooltip(context: Context) {
        balloon = createBalloon(context) {
            setWidthRatio(0.5f)
            setArrowVisible(false)
            setHeight(65)
            setCornerRadius(4f)
            setAlpha(0.9f)
            setText("Remove marker")
            setTextColorResource(R.color.white)
            setIconDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp))
            setBackgroundColorResource(R.color.colorPrimaryDark)
            setOnBalloonClickListener {
                viewModel.onRemoveBalloonClick()
            }
            setFocusable(false)
            setDismissWhenClicked(true)
            setDismissWhenTouchOutside(false)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
        val mapView = supportFragmentManager.findFragmentById(R.id.map)!!.view!!
        balloon?.showAlignBottom(mapView)
    }
}
