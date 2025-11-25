
package com.meticha.jetpackboilerplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meticha.jetpackboilerplate.navigation.AppNavigation
import com.meticha.jetpackboilerplate.navigation.HomeRoute
import com.meticha.jetpackboilerplate.ui.theme.CallBudyTheme
import com.meticha.jetpackboilerplate.workers.LocationWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var backStack: NavBackStack
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        scheduleLocationWork()

        setContent {
            CallBudyTheme {
                backStack = rememberNavBackStack(HomeRoute)
                AppNavigation(backStack = backStack)
            }
        }
    }

    private fun scheduleLocationWork() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LocationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
