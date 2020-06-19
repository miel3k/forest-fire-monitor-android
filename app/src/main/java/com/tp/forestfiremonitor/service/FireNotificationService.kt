package com.tp.forestfiremonitor.service

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tp.base.data.RepositoryResult
import com.tp.forestfiremonitor.data.fire.repository.FireDataSource
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
                    }
                    is RepositoryResult.Error -> Log.d(TAG, "Error: ${result.exception.message}")
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "FireNotificationService"
    }
}


