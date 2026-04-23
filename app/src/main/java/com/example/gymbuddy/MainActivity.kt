package com.example.gymbuddy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gymbuddy.auth.AuthManager
import com.example.gymbuddy.BuildConfig
import com.example.gymbuddy.service.StreakAlarmReceiver
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.GymBuddyNavigation
import com.example.gymbuddy.ui.theme.DarkBackground
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: WorkoutSessionManager

    @Inject
    lateinit var authManager: AuthManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
            scheduleStreakReminder()
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    private val signInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("MainActivity", "Sign-in result: ${result.resultCode}")
        authManager.handleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager.setActivityResultLauncher(signInLauncher)

        handleDeepLink(intent)

        requestNotificationPermission()

        if (savedInstanceState == null) {
            intent?.getBooleanExtra("expand_overlay", false)?.let { shouldExpand ->
                if (shouldExpand) {
                    sessionManager.expand()
                }
            }
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d("MainActivity", "Deep link received: $uri")

            if (uri.scheme == "gymbuddy" && uri.host == "routine") {
                val routineId = uri.getQueryParameter("id")?.toLongOrNull()
                val date = uri.getQueryParameter("date")

                if (routineId != null) {
                    Log.d("MainActivity", "Opening routine $routineId for date $date")
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
                    scheduleStreakReminder()
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
        } else {
            scheduleStreakReminder()
        }
    }

    private fun scheduleStreakReminder() {
        val intervalMinutes = if (BuildConfig.DEBUG) {
            Log.d("StreakReminder", "DEBUG MODE: Scheduling notification every 2 minutes")
            2L
        } else {
            Log.d("StreakReminder", "PRODUCTION MODE: Scheduling daily reminder")
            1440L
        }

        StreakAlarmReceiver.scheduleAlarm(this, intervalMinutes)
    }
}