package com.tp.forestfiremonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tp.base.data.RepositoryResult
import com.tp.forestfiremonitor.R
import com.tp.forestfiremonitor.data.fire.repository.FireDataSource
import com.tp.forestfiremonitor.presentation.view.MapActivity
import com.tp.forestfiremonitor.presentation.view.MapActivity.Companion.CHANNEL_ID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FireNotificationService : FirebaseMessagingService() {

    private val fireRepository: FireDataSource by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            GlobalScope.launch {
                val token = FirebaseInstanceId.getInstance().getToken().orEmpty()
                when (val result = fireRepository.getFiresNotification(token)) {
                    is RepositoryResult.Success -> {
                        Log.d(TAG, "Fires from notification received: ${result.data}")
                        NotificationEvents.serviceEvent.postValue(result.data)
                        sendNotification()
                    }
                    is RepositoryResult.Error -> Log.d(TAG, "Error: ${result.exception.message}")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    private fun sendNotification() {
        Log.i(TAG, "Prepare show notification")
        val intent = Intent(this, MapActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,
            0, intent, PendingIntent.FLAG_ONE_SHOT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delete_black_24dp)
            .setContentTitle("New fires around!")
            .setContentText("Open FFM map and check fires around you")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify((0..Int.MAX_VALUE).random(), builder.build())
    }

    companion object {
        private const val TAG = "FireNotificationService"
    }
}


