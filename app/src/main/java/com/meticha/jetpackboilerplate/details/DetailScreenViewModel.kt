
package com.meticha.jetpackboilerplate.details

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
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
    private val application: Application
) : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    init {
        viewModelScope.launch {
            _isServiceRunning.value = isLocationServiceRunning()
        }
    }

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

    fun toggleLocationService() {
        viewModelScope.launch {
            val intent = Intent(application, LocationService::class.java)
            if (_isServiceRunning.value) {
                intent.action = LocationService.ACTION_STOP
                application.startService(intent)
            } else {
                intent.action = LocationService.ACTION_START
                application.startForegroundService(intent)
            }
            _isServiceRunning.value = !_isServiceRunning.value
        }
    }

    @Suppress("DEPRECATION")
    private fun isLocationServiceRunning(): Boolean {
        val manager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
