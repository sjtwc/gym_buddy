package com.csci3310.gymbuddy.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import androidx.work.WorkManager
import com.csci3310.gymbuddy.data.local.GymBuddyDatabase
import com.csci3310.gymbuddy.data.local.entity.WorkoutEntity
import com.csci3310.gymbuddy.provider.UserStatsContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import java.util.Calendar

data class WidgetStats(
    val currentStreak: Int = 0,
    val workoutsThisWeek: Int = 0,
    val todayCompleted: Boolean = false,
    val weeklyTarget: Int = 4,
    val level: Int = 1,
    val title: String = "Novice"
)

object WidgetDataManager {
    
    private const val PREFS_NAME = "gym_buddy_prefs"
    
    fun queryWidgetStatsFromProvider(context: Context): WidgetStats {
        return try {
            val cursor = context.contentResolver.query(
                UserStatsContract.DailySummary.CONTENT_URI,
                null, null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val streakIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_CURRENT_STREAK)
                    val weekIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_WORKOUTS_THIS_WEEK)
                    val todayIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_TODAY_COMPLETED)
                    val targetIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_WEEKLY_TARGET)
                    val levelIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_LEVEL)
                    val titleIdx = it.getColumnIndex(UserStatsContract.DailySummary.COLUMN_TITLE)

                    WidgetStats(
                        currentStreak = if (streakIdx >= 0) it.getInt(streakIdx) else 0,
                        workoutsThisWeek = if (weekIdx >= 0) it.getInt(weekIdx) else 0,
                        todayCompleted = if (todayIdx >= 0) it.getInt(todayIdx) == 1 else false,
                        weeklyTarget = if (targetIdx >= 0) it.getInt(targetIdx) else 4,
                        level = if (levelIdx >= 0) it.getInt(levelIdx) else 1,
                        title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Novice" else "Novice"
                    )
                } else {
                    WidgetStats()
                }
            } ?: WidgetStats()
        } catch (e: Exception) {
            e.printStackTrace()
            WidgetStats()
        }
    }
    
    fun getWidgetStatsFromDatabase(context: Context): WidgetStats {
        return try {
            val database = GymBuddyDatabase.getDatabase(context)
            val profile = runBlocking { database.userProfileDao().getUserProfileSync() }
            
            val now = System.currentTimeMillis()
            val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
            val workoutsThisWeek = runBlocking { database.workoutDao().getWorkoutsByDateRangeSync(weekStart, now) }
                .filter { it.isCompleted }
            
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val todayCompleted = workoutsThisWeek.any { it.date >= today }
            
            WidgetStats(
                currentStreak = profile?.currentStreak ?: 0,
                workoutsThisWeek = workoutsThisWeek.size,
                todayCompleted = todayCompleted,
                weeklyTarget = 4,
                level = profile?.level ?: 1,
                title = profile?.title ?: "Novice"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            WidgetStats()
        }
    }
    
    fun updateWidgetData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = GymBuddyDatabase.getDatabase(context)
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                
                // Get user profile for streak
                val profile = database.userProfileDao().getUserProfileSync()
                val streak = profile?.currentStreak ?: 0
                prefs.edit().putInt("current_streak", streak).apply()
                
                // Get workouts this week
                val now = System.currentTimeMillis()
                val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
                val workoutsThisWeek = database.workoutDao().getWorkoutsByDateRangeSync(weekStart, now)
                
                // Check if today's workout is done
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val todayDone = workoutsThisWeek.any { workout ->
                    workout.date >= today
                }
                
                prefs.edit()
                    .putInt("workouts_this_week", workoutsThisWeek.size)
                    .putBoolean("today_done", todayDone)
                    .apply()
                
                // Update widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetComponent = ComponentName(context, WorkoutWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
                
                for (appWidgetId in appWidgetIds) {
                    WorkoutWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun scheduleWeeklyReset(context: Context) {
        // Schedule a weekly reset on Sunday midnight
        // For now, just reset the weekly count when app opens on a new week
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastResetWeek = prefs.getLong("last_reset_week", 0L)
            val now = System.currentTimeMillis()
            
            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            
            calendar.timeInMillis = lastResetWeek
            val lastResetWeekNum = calendar.get(Calendar.WEEK_OF_YEAR)
            
            if (currentWeek != lastResetWeekNum) {
                // New week - will be reset on next workout
                prefs.edit().putLong("last_reset_week", now).apply()
            }
        }
    }
}