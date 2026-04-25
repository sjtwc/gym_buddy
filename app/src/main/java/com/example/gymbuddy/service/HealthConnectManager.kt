package com.csci3310.gymbuddy.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.csci3310.gymbuddy.data.local.GymBuddyDatabase
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
        
        val HEALTH_PACKAGES = listOf(
            "com.google.android.apps.fitness",
            "com.google.android.apps.healthdata",
            "com.samsung.android.shealth",
            "com.huawei.health",
            "com.oneplus.health",
            "com.mi.health",
            "com.oppo.health"
        )
        
        const val GOOGLE_FIT = "com.google.android.apps.fitness"
    }

    private var availablePackage: String? = null
    
    private fun findPackage(): String? {
        for (pkg in HEALTH_PACKAGES) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    fun isAvailable(): Boolean {
        if (availablePackage == null) {
            availablePackage = findPackage()
        }
        return availablePackage != null
    }
    
    fun getAppName(): String {
        return when (availablePackage) {
            GOOGLE_FIT -> "Google Fit"
            else -> "Health App"
        }
    }

    fun hasAllPermissions(): Boolean = isAvailable()

    suspend fun getRecentSessions(limit: Int = 10): List<ExerciseSession> {
        return try {
            val db = GymBuddyDatabase.getDatabase(context)
            db.workoutDao().getRecentCompletedWorkouts(limit).first()
                .filter { it.isCompleted }
                .map { workout ->
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

    suspend fun exportSession(session: ExerciseSession): Boolean = openApp()

    private fun openGoogleFit(): Boolean {
        val intents = listOf(
            Intent().setPackage(GOOGLE_FIT),
            Intent(Intent.ACTION_MAIN).setPackage(GOOGLE_FIT),
            Intent(Intent.ACTION_VIEW).setPackage(GOOGLE_FIT)
        )
        
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                continue
            }
        }
        return false
    }

    fun openApp(): Boolean {
        // Try Google Fit first directly
        val fitOpened = openGoogleFit()
        if (fitOpened) return true
        
        // Check if any health app is available
        if (!isAvailable()) {
            openPlayStore()
            return false
        }
        
        // Try the available package
        availablePackage?.let { pkg ->
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
                
                // Try opening with VIEW intent
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(viewIntent)
                    return true
                } catch (e: Exception) {
                    openPlayStoreForApp(pkg)
                    return true
                }
            } catch (e: Exception) {
                openPlayStoreForApp(pkg)
                return true
            }
        }
        
        openPlayStore()
        return false
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=Google+Fit")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val web = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=" + GOOGLE_FIT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(web)
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
            try {
                val web = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(web)
            } catch (e2: Exception) {
                openPlayStore()
            }
        }
    }
}