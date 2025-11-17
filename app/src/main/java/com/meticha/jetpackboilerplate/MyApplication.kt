package com.meticha.jetpackboilerplate

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.meticha.jetpackboilerplate.details.LocationService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Constants.AUTO_START_LOCATION_SERVICE) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                }
                startForegroundService(intent)
            }
        }
    }
}
