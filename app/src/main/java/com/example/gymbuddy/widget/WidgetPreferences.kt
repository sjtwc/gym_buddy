package com.example.gymbuddy.widget

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object WidgetPreferences {
    
    private const val PREFS_NAME = "gym_buddy_widget_prefs"
    
    private const val KEY_STREAK = "current_streak"
    private const val KEY_WORKOUTS_THIS_WEEK = "workouts_this_week"
    private const val KEY_TODAY_DONE = "today_done"
    private const val KEY_LAST_WORKOUT_DATE = "last_workout_date"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getStreak(context: Context): Int {
        return getPrefs(context).getInt(KEY_STREAK, 0)
    }
    
    fun getWorkoutsThisWeek(context: Context): Int {
        return getPrefs(context).getInt(KEY_WORKOUTS_THIS_WEEK, 0)
    }
    
    fun getTodayDone(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TODAY_DONE, false)
    }
    
    fun updateFromDatabase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = getPrefs(context)
                val database = com.example.gymbuddy.data.local.GymBuddyDatabase.getDatabase(context)
                
                // Get user profile for streak
                val profile = database.userProfileDao().getUserProfileSync()
                val streak = profile?.currentStreak ?: 0
                
                // Get workouts this week
                val now = System.currentTimeMillis()
                val weekStart = now - (7 * 24 * 60 * 60 * 1000L)
                val workoutsThisWeek = database.workoutDao().getWorkoutsByDateRangeSync(weekStart, now).size
                
                // Check if today's workout is done
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val recentWorkouts = database.workoutDao().getRecentCompletedWorkouts(1)
                val workoutList = recentWorkouts.first()
                val lastWorkoutDate = workoutList.firstOrNull()?.date ?: 0L
                val todayDone = lastWorkoutDate >= today
                
                prefs.edit()
                    .putInt(KEY_STREAK, streak)
                    .putInt(KEY_WORKOUTS_THIS_WEEK, workoutsThisWeek)
                    .putBoolean(KEY_TODAY_DONE, todayDone)
                    .apply()
                
                // Update all widgets
                updateAllWidgets(context)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        
        // Update streak widgets
        val streakComponent = android.content.ComponentName(context, StreakWidgetProvider::class.java)
        val streakIds = appWidgetManager.getAppWidgetIds(streakComponent)
        for (appWidgetId in streakIds) {
            StreakWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)
        }
        
        // Update today status widgets
        val todayComponent = android.content.ComponentName(context, TodayStatusWidgetProvider::class.java)
        val todayIds = appWidgetManager.getAppWidgetIds(todayComponent)
        for (appWidgetId in todayIds) {
            TodayStatusWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)
        }
        
        // Update weekly progress widgets
        val weeklyComponent = android.content.ComponentName(context, WeeklyProgressWidgetProvider::class.java)
        val weeklyIds = appWidgetManager.getAppWidgetIds(weeklyComponent)
        for (appWidgetId in weeklyIds) {
            WeeklyProgressWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
}