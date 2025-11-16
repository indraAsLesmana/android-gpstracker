
package com.meticha.jetpackboilerplate.data.repository

import com.meticha.jetpackboilerplate.data.LocationData
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class LocationRepository @Inject constructor(private val client: HttpClient) {

    suspend fun sendLocation(locationData: LocationData) {
        client.post("https://gps-tracker-server.indra953.workers.dev/api/location") {
            contentType(ContentType.Application.Json)
            setBody(locationData)
        }
    }
}
