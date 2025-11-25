
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
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.startLocationUpdates()
            viewModel.fetchAndSendLocationImmediately()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        // On Android 10 (Q) and above, background location permission is needed.
        // On Android 11 (R) and above, it must be requested separately.
        // For now, let's request it if we can.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             // Note: On Android 11+, requesting this with other permissions might be ignored or cause issues.
             // But for Android 10 it works.
             // Ideally we should have a separate flow.
             // Let's add it here for now, but if it fails, the user needs to go to settings.
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                 permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
             }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.startLocationUpdates()
            viewModel.fetchAndSendLocationImmediately()
            
            // Check for background permission on Android 11+ separately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // We can't request it directly in the same launcher easily without a separate flow.
                // For this MVP, we will rely on the user granting "Allow all the time" in settings
                // or we could trigger a separate request here.
                // Let's try to request it if fine location is already granted.
                 permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
        }
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
