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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.gymbuddy.auth.AuthManager
import com.example.gymbuddy.BuildConfig
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.domain.model.Workout
import com.example.gymbuddy.service.StreakAlarmReceiver
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.GymBuddyNavigation
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.DarkBackground
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: WorkoutSessionManager

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    private var navController: NavHostController? = null
    private var pendingWidgetIntent: Intent? = null

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
            pendingWidgetIntent = intent
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
                    GymBuddyNavigation(
                        sessionManager = sessionManager,
                        onNavControllerReady = { nc -> navController = nc }
                    )

                    LaunchedEffect(Unit) {
                        pendingWidgetIntent?.let { intent ->
                            handleWidgetAction(intent)
                            pendingWidgetIntent = null
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
        handleWidgetAction(intent)
    }

    private fun handleWidgetAction(intent: Intent?) {
        when (intent?.getStringExtra("action")) {
            "quick_start" -> {
                if (sessionManager.isActive.value) {
                    sessionManager.expand()
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        val workout = Workout(
                            name = "Quick Workout",
                            date = System.currentTimeMillis(),
                            startedAt = System.currentTimeMillis(),
                            isCompleted = false
                        )
                        val workoutId = workoutRepository.startWorkout(workout)
                        sessionManager.startSession(workoutId, "Quick Workout")
                    }
                }
            }
            "navigate_profile" -> {
                navController?.navigate(Screen.Profile.route)
            }
            "navigate_home" -> {
                navController?.navigate(Screen.Home.route)
            }
            "navigate_progress" -> {
                navController?.navigate(Screen.Progress.route)
            }
        }
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