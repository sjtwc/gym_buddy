package com.example.gymbuddy.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.WorkManager
import com.example.gymbuddy.data.local.GymBuddyDatabase
import com.example.gymbuddy.data.local.entity.WorkoutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object WidgetDataManager {
    
    private const val PREFS_NAME = "gym_buddy_prefs"
    
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