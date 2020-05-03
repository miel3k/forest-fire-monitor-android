package com.tp.forestfiremonitor.data.fire.repository

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tp.base.ApiConstants
import com.tp.base.MediaTypes
import com.tp.base.RepositoryResult
import com.tp.forestfiremonitor.data.area.model.Coordinate
import com.tp.forestfiremonitor.data.call
import com.tp.forestfiremonitor.data.fire.model.FiresResult
import com.tp.forestfiremonitor.data.fire.remote.FireService
import okhttp3.MediaType
import okhttp3.RequestBody

class FireRepository(private val fireService: FireService) : FireDataSource {

    override suspend fun searchFires(coordinates: List<Coordinate>): RepositoryResult<FiresResult> {
        val headers = mutableMapOf(ApiConstants.CONTENT_TYPE to MediaTypes.APPLICATION_JSON)
        val listType = Types.newParameterizedType(List::class.java, Coordinate::class.java)
        val adapter: JsonAdapter<List<Coordinate>> = Moshi.Builder().build().adapter(listType)
        val json = adapter.toJson(coordinates)
        val body = RequestBody.create(MediaType.parse(MediaTypes.APPLICATION_JSON), json)
        return fireService.searchFiresAsync(headers, body).call()
    }
}