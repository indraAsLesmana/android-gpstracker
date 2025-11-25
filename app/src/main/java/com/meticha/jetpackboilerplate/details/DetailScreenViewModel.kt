
package com.meticha.jetpackboilerplate.details

import android.annotation.SuppressLint

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val workManager: androidx.work.WorkManager
) : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            _location.value = locationResult.lastLocation
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 45_000 // 45 seconds
        ).build()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    fun fetchAndSendLocationImmediately() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.meticha.jetpackboilerplate.workers.LocationWorker>()
            .setConstraints(constraints)
            .build()
        workManager.enqueue(workRequest)
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
