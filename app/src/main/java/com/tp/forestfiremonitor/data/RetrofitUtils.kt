package com.tp.forestfiremonitor.data

import com.tp.base.data.RepositoryResult
import retrofit2.Response
import java.io.IOException

fun <T : Any> Response<T>.call(): RepositoryResult<T> {
    return if (isSuccessful) {
        RepositoryResult.Success(body()!!)
    } else {
        RepositoryResult.Error(IOException(message()))
    }
}
