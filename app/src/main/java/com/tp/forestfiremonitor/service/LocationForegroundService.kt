package com.tp.forestfiremonitor.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.iid.FirebaseInstanceId
import com.tp.forestfiremonitor.R
import com.tp.forestfiremonitor.data.area.model.Device
import com.tp.forestfiremonitor.data.fire.repository.FireDataSource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationForegroundService : Service() {

    private val fireRepository: FireDataSource by inject()

    private val locationManager by lazy {
        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val locationListener by lazy {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                GlobalScope.launch {
                    sendLocation(location)
                }
            }

            override fun onStatusChanged(
                provider: String,
                status: Int,
                extras: Bundle
            ) {
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    private val binder = LocationForegroundServiceBinder()
    private lateinit var deviceId: String
    private var isStarted = false

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int
    ): Int {
        deviceId = intent.getStringExtra(DEVICE_ID).orEmpty()
        val interval = intent.getIntExtra(
            LOCATION_UPDATES_INTERVAL,
            LOCATION_UPDATES_INTERVAL_DEFAULT_VALUE
        )
        if (intent.action == ServiceAction.START) start(interval) else stop()
        return START_STICKY
    }

    private fun start(interval: Int) {
        if (isStarted) return
        val locationNotification = LocationNotification(applicationContext).create()
        startForeground(LOCATION_FOREGROUND_SERVICE, locationNotification)
        startLocationTracking(interval)
        isStarted = true
    }

    private fun stop() {
        if (isStarted) {
            locationManager.removeUpdates(locationListener)
            stopForeground(true)
            stopSelf()
        }
        isStarted = false
    }

    private fun startLocationTracking(interval: Int) {
        val permissions = getLocationPermissions()
        val arePermissionsGranted = permissions.all {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (arePermissionsGranted) {
            Log.i(LocationForegroundService::class.java.simpleName, "Location tracking requested")
            locationManager.requestLocationUpdates(
                getAvailableLocationProvider(),
                (interval * 1000).toLong(),
                LOCATION_MIN_DISTANCE,
                locationListener
            )
        } else {
            stop()
        }
    }

    private fun getAvailableLocationProvider(): String {
        return if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else {
            LocationManager.NETWORK_PROVIDER
        }
    }

    private suspend fun sendLocation(location: Location) {
        location.toDevice().let {
            fireRepository.updateLocation(it)
        }
    }

    private fun Location.toDevice() = let {
        Device(
            fcmToken = FirebaseInstanceId.getInstance().getToken().orEmpty(),
            latitude = it.latitude,
            longitude = it.longitude
        )
    }

    inner class LocationForegroundServiceBinder : Binder() {
        fun isServiceStarted() = this@LocationForegroundService.isStarted
    }

    private fun getLocationPermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    inner class LocationNotification(private val applicationContext: Context) {

        private val notificationManager by lazy {
            NotificationManagerCompat.from(applicationContext)
        }

        fun create() = applicationContext.run {
            notificationManager.createNotificationChannel()
            createNotification()
        }

        private fun NotificationManagerCompat.createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                createNotificationChannel(notificationChannel)
            }
        }

        private fun createNotification(): Notification {
            return NotificationCompat
                .Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.location_service_title))
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setAutoCancel(false)
                .build()
        }
    }

    companion object {
        private const val LOCATION_MIN_DISTANCE = 0f
        const val LOCATION_UPDATES_INTERVAL_DEFAULT_VALUE = 15
        const val LOCATION_UPDATES_INTERVAL = "LOCATION_UPDATES_INTERVAL"
        const val DEVICE_ID = "DEVICE_ID"
        const val LOCATION_FOREGROUND_SERVICE = 10000
        const val NOTIFICATION_CHANNEL_ID = "LOCATION_NOTIFICATION_CHANNEL_ID"
        const val NOTIFICATION_CHANNEL_NAME = "LOCATION_NOTIFICATION_CHANNEL_NAME"
    }
}

