
package com.meticha.jetpackboilerplate.details

import android.Manifest
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.meticha.jetpackboilerplate.utils.hasBackgroundLocationPermission
import com.meticha.jetpackboilerplate.utils.hasLocationPermission
import com.meticha.jetpackboilerplate.utils.openLocationSettings

@Composable

fun DetailsScreen(viewModel: DetailScreenViewModel = hiltViewModel()) {
    val location by viewModel.location.collectAsState()
    val context = LocalContext.current
    var showBackgroundLocationRationale by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Launcher for Background Permission (API 29 only)
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startLocationUpdates()
            viewModel.fetchAndSendLocationImmediately()
        }
    }

    // Launcher for Fine Location
    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Check for background permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: Check if background permission is granted
                if (!context.hasBackgroundLocationPermission()) {
                    showBackgroundLocationRationale = true
                } else {
                    viewModel.startLocationUpdates()
                    viewModel.fetchAndSendLocationImmediately()
                }
            } else {
                // API 29: Request background permission directly
                if (!context.hasBackgroundLocationPermission()) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    viewModel.startLocationUpdates()
                    viewModel.fetchAndSendLocationImmediately()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!context.hasLocationPermission()) {
            fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Fine location already granted, check background
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!context.hasBackgroundLocationPermission()) {
                    showBackgroundLocationRationale = true
                } else {
                    viewModel.startLocationUpdates()
                    viewModel.fetchAndSendLocationImmediately()
                }
            } else {
                if (!context.hasBackgroundLocationPermission()) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    viewModel.startLocationUpdates()
                    viewModel.fetchAndSendLocationImmediately()
                }
            }
        }
    }

    if (showBackgroundLocationRationale) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBackgroundLocationRationale = false },
            title = { Text("Background Location Needed") },
            text = { Text("To track your location in the background, please select \"Allow all the time\" in the settings.") },
            confirmButton = {
                Button(onClick = {
                    showBackgroundLocationRationale = false
                    context.openLocationSettings()
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = { showBackgroundLocationRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    DetailScreenLayout(
        location = location
    )
}

@Composable
fun DetailScreenLayout(
    location: Location?
) {
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Details Screen")
            location?.let {
                Text("Latitude: ${it.latitude}")
                Text("Longitude: ${it.longitude}")
            }
            Text("Location tracking is active in background (every 15 mins)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    val location = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 37.7749
        longitude = -122.4194
    }
    DetailScreenLayout(
        location = location
    )
}
