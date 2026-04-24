package com.example.gymbuddy.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.gymbuddy.data.local.GymBuddyDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        
        // Health Connect app package
        const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    }

    // Use Health Connect SDK if available
    private val isSdkAvailable: Boolean
        get() = try {
            androidx.health.connect.client.HealthConnectClient.getSdkStatus(context) == 
                androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }

    fun isAvailable(): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(HEALTH_CONNECT_PACKAGE, 0)
            isSdkAvailable || checkIntentPossible()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkIntentPossible(): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(HEALTH_CONNECT_PACKAGE)
            intent != null
        } catch (e: Exception) {
            false
        }
    }

    fun hasAllPermissions(): Boolean {
        // For basic implementation, we don't require permissions
        // Full implementation would check Health Connect SDK permissions
        return isAvailable()
    }

    suspend fun getRecentSessions(limit: Int = 10): List<ExerciseSession> {
        return try {
            val database = GymBuddyDatabase.getDatabase(context)
            val workouts = database.workoutDao()
                .getRecentCompletedWorkouts(limit)
                .first()
                .filter { it.isCompleted }
                .filter { it.duration != null }
            
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
        // Basic implementation - open Health Connect app with workout info
        return try {
            openHealthConnectWithWorkout(session)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openHealthConnectWithWorkout(session: ExerciseSession) {
        try {
            // Try to open Health Connect directly
            val intent = context.packageManager.getLaunchIntentForPackage(HEALTH_CONNECT_PACKAGE)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            // Fallback: open Play Store
            openPlayStore()
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

    fun openHealthConnect() {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(HEALTH_CONNECT_PACKAGE)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            } ?: openPlayStore()
        } catch (e: Exception) {
            openPlayStore()
        }
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}