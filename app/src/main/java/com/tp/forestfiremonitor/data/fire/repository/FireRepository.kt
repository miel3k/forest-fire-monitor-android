package com.tp.forestfiremonitor.data.fire.repository

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tp.base.constant.ApiConstants
import com.tp.base.constant.MediaTypes
import com.tp.base.data.RepositoryResult
import com.tp.forestfiremonitor.data.area.model.Coordinate
import com.tp.forestfiremonitor.data.area.model.Device
import com.tp.forestfiremonitor.data.area.model.FcmTokenPayload
import com.tp.forestfiremonitor.data.call
import com.tp.forestfiremonitor.data.fire.model.FiresResult
import com.tp.forestfiremonitor.data.fire.remote.FireService
import okhttp3.MediaType
import okhttp3.RequestBody
import java.lang.RuntimeException

class FireRepository(private val fireService: FireService) : FireDataSource {

    override suspend fun searchFires(coordinates: List<Coordinate>): RepositoryResult<FiresResult> {
        val headers = mutableMapOf(ApiConstants.CONTENT_TYPE to MediaTypes.APPLICATION_JSON)
        val listType = Types.newParameterizedType(List::class.java, Coordinate::class.java)
        val adapter: JsonAdapter<List<Coordinate>> = Moshi.Builder().build().adapter(listType)
        val json = adapter.toJson(coordinates)
        val body = RequestBody.create(MediaType.parse(MediaTypes.APPLICATION_JSON), json)
        return fireService.searchFiresAsync(headers, body).call()
    }

    override suspend fun updateLocation(device: Device): RepositoryResult<String> {
        val headers = mutableMapOf(ApiConstants.CONTENT_TYPE to MediaTypes.APPLICATION_JSON)
        val adapter: JsonAdapter<Device> = Moshi.Builder().build().adapter(Device::class.java)
        val json = adapter.toJson(device)
        val body = RequestBody.create(MediaType.parse(MediaTypes.APPLICATION_JSON), json)
        Log.i(FireRepository::class.java.simpleName, "Sending device loc: $json")
        return if (fireService.sendDeviceLocation(headers, body).isSuccessful) {
            RepositoryResult.Success("")
        } else {
            RepositoryResult.Error(RuntimeException("Location update error"))
        }
    }

    override suspend fun getFiresNotification(token: String): RepositoryResult<FiresResult> {
        val headers = mutableMapOf(ApiConstants.CONTENT_TYPE to MediaTypes.APPLICATION_JSON)
        val adapter: JsonAdapter<FcmTokenPayload> = Moshi.Builder().build().adapter(FcmTokenPayload::class.java)
        val json = adapter.toJson(FcmTokenPayload(token))
        val body = RequestBody.create(MediaType.parse(MediaTypes.APPLICATION_JSON), json)
        return fireService.getFireNotifications(headers, body).call()
    }
}
