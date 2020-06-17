package com.tp.forestfiremonitor.data.fire.remote

import com.tp.forestfiremonitor.data.fire.model.FiresResult
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface FireService {

    @POST("/api")
    suspend fun searchFiresAsync(
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody
    ): Response<FiresResult>

    @POST("/api/devices")
    suspend fun sendDeviceLocation(
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody
    ): Response<Void>
}
