package com.tp.forestfiremonitor.service

import androidx.lifecycle.MutableLiveData
import com.tp.forestfiremonitor.data.fire.model.FiresResult

object NotificationEvents {
    val serviceEvent: MutableLiveData<FiresResult> by lazy {
        MutableLiveData<FiresResult>()
    }
}
