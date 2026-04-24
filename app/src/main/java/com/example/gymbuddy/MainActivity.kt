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
import com.example.gymbuddy.service.UserSummaryWorker
import com.example.gymbuddy.service.WorkoutSessionManager
import com.example.gymbuddy.ui.navigation.GymBuddyNavigation
import com.example.gymbuddy.ui.navigation.Screen
import com.example.gymbuddy.ui.theme.DarkBackground
import com.example.gymbuddy.ui.theme.GymBuddyTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    @Inject
    lateinit var llmService: com.example.gymbuddy.service.LlmService

    private var navController: NavHostController? = null
    private var pendingWidgetIntent: Intent? = null
    private var isProcessingWidgetIntent = false

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

        preloadLlmModel()
        scheduleUserSummaryWork()

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

                    LaunchedEffect(navController, pendingWidgetIntent) {
                        if (navController != null && pendingWidgetIntent != null) {
                            handleWidgetAction(pendingWidgetIntent)
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
        pendingWidgetIntent = intent
    }

    private fun handleWidgetAction(intent: Intent?) {
        if (isProcessingWidgetIntent) {
            Log.d("MainActivity", "handleWidgetAction: already processing, skipping")
            return
        }
        isProcessingWidgetIntent = true
        val action = intent?.getStringExtra("action")
        Log.d("MainActivity", "handleWidgetAction: action=$action, navController=${navController != null}")
        try {
            when (action) {
                "quick_start" -> {
                    Log.d("MainActivity", "quick_start: isActive=${sessionManager.isActive.value}")
                    if (sessionManager.isActive.value) {
                        sessionManager.expand()
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("MainActivity", "quick_start: creating workout...")
                            val workout = Workout(
                                name = "Quick Workout",
                                date = System.currentTimeMillis(),
                                startedAt = System.currentTimeMillis(),
                                isCompleted = false
                            )
                            val workoutId = workoutRepository.startWorkout(workout)
                            Log.d("MainActivity", "quick_start: workoutId=$workoutId")
                            sessionManager.startSession(workoutId, "Quick Workout")
                            Log.d("MainActivity", "quick_start: session started")
                        }
                    }
                }
                "navigate_profile" -> {
                    Log.d("MainActivity", "Navigating to Profile")
                    val nc = navController ?: return
                    if (nc.currentDestination?.route == Screen.Profile.route) {
                        Log.d("MainActivity", "Already at Profile, skipping")
                    } else {
                        nc.navigate(Screen.Profile.route)
                    }
                }
                "navigate_home" -> {
                    Log.d("MainActivity", "Navigating to Home")
                    val nc = navController ?: return
                    if (nc.currentDestination?.route == Screen.Home.route) {
                        Log.d("MainActivity", "Already at Home, skipping")
                    } else {
                        nc.navigate(Screen.Home.route)
                    }
                }
                "navigate_progress" -> {
                    Log.d("MainActivity", "Navigating to Progress")
                    val nc = navController ?: return
                    if (nc.currentDestination?.route == Screen.Progress.route) {
                        Log.d("MainActivity", "Already at Progress, skipping")
                    } else {
                        nc.navigate(Screen.Progress.route)
                    }
                }
                else -> {
                    Log.w("MainActivity", "handleWidgetAction: unknown action=$action")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "handleWidgetAction: error", e)
        } finally {
            isProcessingWidgetIntent = false
            pendingWidgetIntent = null
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

    private fun preloadLlmModel() {
        Log.d("MainActivity", "Preloading LLM model...")
        CoroutineScope(Dispatchers.Main).launch {
            val ready = llmService.ensureModelReady()
            if (ready) {
                Log.d("MainActivity", "LLM model preloaded successfully")
            } else {
                Log.w("MainActivity", "LLM model preload failed - will load on demand")
            }
        }
    }

    private fun scheduleUserSummaryWork() {
        Log.d("MainActivity", "Scheduling user summary work...")
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<UserSummaryWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        )
            .setInitialDelay(1, java.util.concurrent.TimeUnit.HOURS)
            .build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UserSummaryWorker.WORK_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d("MainActivity", "User summary work scheduled")
    }
}