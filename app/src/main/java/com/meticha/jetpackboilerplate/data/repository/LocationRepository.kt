package com.meticha.jetpackboilerplate.data.repository

import android.util.Log
import com.meticha.jetpackboilerplate.Constants
import com.meticha.jetpackboilerplate.data.LocationData
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import javax.inject.Inject

class LocationRepository @Inject constructor(private val client: HttpClient) {

    suspend fun sendLocation(locationData: LocationData) {
        if (Constants.FEATURE_FLAG_RETRY_ON_FAIL) {
            for (attempt in 1..Constants.MAX_RETRY_ATTEMPTS) {
                try {
                    client.post("https://gps-tracker-server.indra953.workers.dev/api/location") {
                        contentType(ContentType.Application.Json)
                        setBody(locationData)
                    }
                    return // Success, exit the loop
                } catch (e: Exception) {
                    Log.e("LocationRepository", "Attempt $attempt failed: ${e.message}")
                    if (attempt < Constants.MAX_RETRY_ATTEMPTS) {
                        delay(Constants.RETRY_INTERVAL_MILLISECONDS)
                    } else {
                        throw e // Rethrow after the final attempt
                    }
                }
            }
        } else {
            client.post("https://gps-tracker-server.indra953.workers.dev/api/location") {
                contentType(ContentType.Application.Json)
                setBody(locationData)
            }
        }
    }
}
