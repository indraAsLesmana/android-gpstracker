
package com.meticha.jetpackboilerplate.details

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DetailsScreen(viewModel: DetailScreenViewModel = hiltViewModel()) {
    val location by viewModel.location.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startLocationUpdates()
        }
        // Notification permission result is handled by the dedicated launcher below
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.toggleLocationService()
            }
        }
    )

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.startLocationUpdates()
        }
    }

    DetailScreenLayout(
        location = location,
        isServiceRunning = isServiceRunning,
        onToggleLocationService = {
            if (isServiceRunning) {
                viewModel.toggleLocationService() // No permission needed to stop
            } else {
                // Check for notification permission before starting the service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.toggleLocationService()
                    } else {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    viewModel.toggleLocationService() // No runtime permission needed for older versions
                }
            }
        }
    )
}

@Composable
fun DetailScreenLayout(
    location: Location?,
    isServiceRunning: Boolean,
    onToggleLocationService: () -> Unit
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
            Button(onClick = onToggleLocationService) {
                Text(if (isServiceRunning) "Stop Location Service" else "Start Location Service")
            }
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
        location = location,
        isServiceRunning = false,
        onToggleLocationService = {}
    )
}
