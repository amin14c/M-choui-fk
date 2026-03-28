package com.smsscheduler

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smsscheduler.ui.AddScheduleScreen
import com.smsscheduler.ui.HomeScreen
import com.smsscheduler.ui.theme.SmsSchedulerTheme

class MainActivity : ComponentActivity() {

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermission.launch(Manifest.permission.SEND_SMS)

        setContent {
            SmsSchedulerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("add") { AddScheduleScreen(navController) }
                        composable("edit/{scheduleId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("scheduleId")?.toIntOrNull() ?: return@composable
                            AddScheduleScreen(navController, scheduleId = id)
                        }
                    }
                }
            }
        }
    }
}
