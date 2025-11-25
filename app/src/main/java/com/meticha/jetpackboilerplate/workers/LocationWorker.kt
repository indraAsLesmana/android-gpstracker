package com.meticha.jetpackboilerplate.workers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.meticha.jetpackboilerplate.data.LocationData
import com.meticha.jetpackboilerplate.data.repository.LocationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

import com.meticha.jetpackboilerplate.utils.hasLocationPermission
import kotlinx.coroutines.tasks.await

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRepository: LocationRepository
) : CoroutineWorker(appContext, workerParams) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting location work")
            
            if (!applicationContext.hasLocationPermission()) {
                Log.w(TAG, "Location permission not granted. Aborting work.")
                return Result.failure()
            }

            val location = fetchCurrentLocation()
            if (location != null) {
                Log.d(TAG, "Location fetched: ${location.latitude}, ${location.longitude}")
                val locationData = LocationData(
                    lot = location.latitude.toString(),
                    lang = location.longitude.toString(),
                    device = Build.MODEL
                )
                // place logic distance comparison before send data here
                locationRepository.sendLocation(locationData)
                Log.d(TAG, "Location sent successfully")
                Result.success()
            } else {
                Log.e(TAG, "Failed to fetch location")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in LocationWorker", e)
            Result.retry()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
            
            if (location != null) {
                location
            } else {
                Log.w(TAG, "getCurrentLocation returned null, trying getLastLocation")
                fusedLocationProviderClient.lastLocation.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching location", e)
            try {
                 Log.w(TAG, "Trying getLastLocation after error")
                 fusedLocationProviderClient.lastLocation.await()
            } catch (e2: Exception) {
                Log.e(TAG, "Error fetching last location", e2)
                null
            }
        }
    }

    companion object {
        private const val TAG = "LocationWorker"
    }
}
