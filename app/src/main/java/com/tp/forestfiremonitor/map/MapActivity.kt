package com.tp.forestfiremonitor.map

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tp.forestfiremonitor.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val viewModel by viewModel<MapViewModel>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupIsEditAreaModeEnabledObserver()
        setupCloseActionModeButton()
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
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun setupIsEditAreaModeEnabledObserver() {
        viewModel.isEditAreaModeEnabled.observe(this, Observer {
            if (it) {
                editAreaActionMode = startSupportActionMode(editAreaActionModeCallback)
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
}
