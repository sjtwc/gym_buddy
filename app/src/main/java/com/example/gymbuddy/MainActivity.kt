package com.example.gymbuddy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymbuddy.BuildConfig
import com.example.gymbuddy.service.StreakReminderWorker
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.GymBuddyNavigation
import com.example.gymbuddy.ui.theme.DarkBackground
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sessionManager: WorkoutSessionManager
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        scheduleStreakReminder()
        setContent {
            GymBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    GymBuddyNavigation(sessionManager = sessionManager)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sessionManager.onCleared()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d("MainActivity", "Show rationale for notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d("MainActivity", "Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    private fun scheduleStreakReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val (repeatInterval, timeUnit) = if (BuildConfig.DEBUG) {
            Log.d("StreakReminder", "DEBUG MODE: Scheduling notification every 30 seconds")
            30L to TimeUnit.SECONDS
        } else {
            Log.d("StreakReminder", "PRODUCTION MODE: Scheduling notification every 30 seconds")
            30L to TimeUnit.SECONDS
        }
        
        val reminderRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(
            repeatInterval, timeUnit
        )
            .setConstraints(constraints)
            .setInitialDelay(0, TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            StreakReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
        
        if (BuildConfig.DEBUG) {
            val immediateRequest = OneTimeWorkRequestBuilder<StreakReminderWorker>()
                .build()
            WorkManager.getInstance(this).enqueue(immediateRequest)
        }
    }
    
    private fun calculateInitialDelay(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 19)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        return if (calendar.timeInMillis > now) {
            calendar.timeInMillis - now
        } else {
            calendar.timeInMillis + (24 * 60 * 60 * 1000) - now
        }
    }
}