package com.meticha.jetpackboilerplate.details

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.meticha.jetpackboilerplate.Constants
import com.meticha.jetpackboilerplate.MainActivity
import com.meticha.jetpackboilerplate.R
import com.meticha.jetpackboilerplate.data.LocationData
import com.meticha.jetpackboilerplate.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationSendJob: Job? = null

    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null

    private var isServiceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startService() {
        if (isServiceRunning) return
        isServiceRunning = true

        startForegroundWithNotification()
        startLocationUpdates()
        startSendingLocationData()
    }

    private fun stopService() {
        stopForeground(true)
        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        locationSendJob?.cancel()
        isServiceRunning = false
        stopSelf()
    }

    private fun startLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                lastLocation = locationResult.lastLocation
                Log.d(TAG, "New Location: Lat: ${lastLocation?.latitude}, Lon: ${lastLocation?.longitude}")
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 45_000
        ).build()

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            stopSelf()
        }
    }

    private fun startSendingLocationData() {
        locationSendJob = tickerFlow(Constants.LOCATION_SEND_INTERVAL_MILLISECONDS)
            .onEach {
                lastLocation?.let {
                    val locationData = LocationData(
                        lot = it.latitude.toString(),
                        lang = it.longitude.toString(),
                        device = Build.MODEL
                    )
                    try {
                        locationRepository.sendLocation(locationData)
                        Log.d(TAG, "Location data sent successfully.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending location data", e)
                    }
                }
            }
            .catch { e -> Log.e(TAG, "Error in ticker flow", e) }
            .launchIn(serviceScope)
    }

    private fun tickerFlow(period: Long, initialDelay: Long = 0) = kotlinx.coroutines.flow.flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    private fun startForegroundWithNotification() {
        val channelId = "location_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = "OPEN_DETAILS_SCREEN"
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "LocationService.ACTION_START"
        const val ACTION_STOP = "LocationService.ACTION_STOP"
    }
}
