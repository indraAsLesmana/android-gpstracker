package com.meticha.jetpackboilerplate.utils

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

private const val TAG = "LocationUtils"

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.fetchCurrentLocation(): Location? {
    return try {
        val cancellationTokenSource = CancellationTokenSource()
        val location = getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).await()

        if (location != null) {
            location
        } else {
            Log.w(TAG, "getCurrentLocation returned null, trying getLastLocation")
            lastLocation.await()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching location", e)
        try {
            Log.w(TAG, "Trying getLastLocation after error")
            lastLocation.await()
        } catch (e2: Exception) {
            Log.e(TAG, "Error fetching last location", e2)
            null
        }
    }
}

fun createLocationRequest(priority: Int, intervalMillis: Long): com.google.android.gms.location.LocationRequest {
    return com.google.android.gms.location.LocationRequest.Builder(
        priority, intervalMillis
    ).build()
}
