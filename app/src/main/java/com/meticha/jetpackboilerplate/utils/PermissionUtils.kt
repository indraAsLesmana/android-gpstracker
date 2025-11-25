package com.meticha.jetpackboilerplate.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasBackgroundLocationPermission(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Below API 29, fine location implies background access
        hasLocationPermission()
    }
}

fun Context.openAppSettings() {
    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.fromParts("package", packageName, null)
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        // Fallback or log if needed
    }
}

fun Context.openLocationSettings() {
    val intent: android.content.Intent
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        intent = android.content.Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
            putExtra("android.intent.extra.PACKAGE_NAME", packageName)
            putExtra("android.intent.extra.PERMISSION_GROUP_NAME", Manifest.permission_group.LOCATION)
        }
    } else {
        intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        // Fallback to generic app settings
        openAppSettings()
    }
}
