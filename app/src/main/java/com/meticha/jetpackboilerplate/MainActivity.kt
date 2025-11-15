
package com.meticha.jetpackboilerplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.meticha.jetpackboilerplate.navigation.AppNavigation
import com.meticha.jetpackboilerplate.navigation.DetailsRoute
import com.meticha.jetpackboilerplate.navigation.HomeRoute
import com.meticha.jetpackboilerplate.ui.theme.CallBudyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var backStack: NavBackStack
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallBudyTheme {
                backStack = rememberNavBackStack(HomeRoute)
                AppNavigation(backStack = backStack)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == "OPEN_DETAILS_SCREEN") {
            backStack.add(DetailsRoute)
        }
    }
}
