package com.example.gymbuddy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    
    private fun scheduleStreakReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val (repeatInterval, timeUnit) = if (BuildConfig.DEBUG) {
            Log.d("StreakReminder", "DEBUG MODE: Scheduling notification every 1 minute")
            1L to TimeUnit.MINUTES
        } else {
            24L to TimeUnit.HOURS
        }
        
        val reminderRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(
            repeatInterval, timeUnit
        )
            .setConstraints(constraints)
            .setInitialDelay(if (BuildConfig.DEBUG) 0 else calculateInitialDelay(), TimeUnit.MILLISECONDS)
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