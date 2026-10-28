package com.smartgas_frontend.shared.data.remote

import android.util.Log
import com.smartgas_frontend.common.Resource
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

// Retrofit se construye en shared/data/di/SharedDataModule (como DataModule.getRetrofit() de MealsCompose).
// Aqui quedan solo las utilidades comunes para las respuestas.

const val API_ERROR = "API_ERROR"

class DomainException(val code: String) : Exception(code)

fun <T> Response<T>.bodyOrThrow(): T {
    if (isSuccessful) {
        body()?.let { return it }
    }
    throw HttpException(this)
}

fun <T> Response<T>.bodyOrNull(): T? = if (isSuccessful) body() else null

fun Response<*>.errorMessage(): String = try {
    errorBody()?.string().orEmpty()
} catch (e: Exception) {
    ""
}

suspend fun <T> safeCall(block: suspend () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: DomainException) {
    Resource.Error(e.code)
} catch (e: Exception) {
    Log.e("SmartGasApi", "API call failed", e)
    Resource.Error(API_ERROR)
}
