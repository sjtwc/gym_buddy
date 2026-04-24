package com.example.gymbuddy.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.gymbuddy.data.local.GymBuddyDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class ExerciseSession(
    val title: String,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val exerciseType: String
)

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val TYPE_STRENGTH = "STRENGTH"
        const val TYPE_CARDIO = "CARDIO"
        const val TYPE_FLEXIBILITY = "FLEXIBILITY"
        
        val HEALTH_FITNESS_PACKAGES = listOf(
            "com.google.android.apps.fitness",
            "com.google.android.apps.healthdata",
            "com.samsung.android.shealth",
            "com.huawei.health",
            "com.oneplus.health",
            "com.mi.health",
            "com.oppo.health"
        )
        
        const val GOOGLE_FIT_PACKAGE = "com.google.android.apps.fitness"
        const val GOOGLE_HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    }

    private var availablePackage: String? = null
    
    private fun findAvailableHealthPackage(): String? {
        for (pkg in HEALTH_FITNESS_PACKAGES) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
        }
        return null
    }

    fun isAvailable(): Boolean {
        if (availablePackage != null) return true
        availablePackage = findAvailableHealthPackage()
        return availablePackage != null
    }
    
    fun getAvailableAppName(): String {
        return when (availablePackage) {
            GOOGLE_FIT_PACKAGE -> "Google Fit"
            GOOGLE_HEALTH_CONNECT_PACKAGE -> "Google Health Connect"
            else -> "Health App"
        }
    }

    fun hasAllPermissions(): Boolean = isAvailable()

    suspend fun getRecentSessions(limit: Int = 10): List<ExerciseSession> {
        return try {
            val database = GymBuddyDatabase.getDatabase(context)
            val workouts = database.workoutDao()
                .getRecentCompletedWorkouts(limit)
                .first()
                .filter { it.isCompleted }
            
            workouts.map { workout ->
                ExerciseSession(
                    title = workout.name ?: "Workout",
                    startTimeMillis = workout.date,
                    durationMinutes = workout.duration ?: 0,
                    exerciseType = TYPE_STRENGTH
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun exportSession(session: ExerciseSession): Boolean {
        return try {
            openHealthAppWithSession(session)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun openHealthAppWithSession(session: ExerciseSession): Boolean {
        val packageName = availablePackage ?: findAvailableHealthPackage()
        
        if (packageName == null) {
            openPlayStore()
            return false
        }
        
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
            
            // Try Play Store for this app
            openPlayStoreForApp(packageName)
            return true
            
        } catch (e: Exception) {
            openPlayStore()
            return false
        }
    }

    suspend fun exportWorkouts(workouts: List<ExerciseSession>): Int {
        var exported = 0
        for (workout in workouts) {
            if (exportSession(workout)) {
                exported++
            }
        }
        return exported
    }

    suspend fun connect(): Boolean = isAvailable()

    fun openApp(): Boolean {
        if (!isAvailable()) {
            openPlayStore()
            return false
        }
        
        return availablePackage?.let { pkg ->
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    true
                } else {
                    openPlayStoreForApp(pkg)
                    true
                }
            } catch (e: Exception) {
                openPlayStore()
                false
            }
        } ?: false
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=Google+Fit+health+connect")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.fitness")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
    
    private fun openPlayStoreForApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openPlayStore()
        }
    }
}