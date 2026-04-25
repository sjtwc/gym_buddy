package com.csci3310.gymbuddy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.app.Activity
import com.csci3310.gymbuddy.R
import com.csci3310.gymbuddy.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkoutWidgetProvider : AppWidgetProvider() {
    
    companion object {
        const val ACTION_QUICK_START = "com.csci3310.gymbuddy.ACTION_QUICK_START"
        
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.workout_widget)
            
            // Set click to open app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnQuickStart, openAppPendingIntent)
            
            // Load data
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = context.getSharedPreferences("gym_buddy_prefs", Context.MODE_PRIVATE)
                    val streak = prefs.getInt("current_streak", 0)
                    val workoutsThisWeek = prefs.getInt("workouts_this_week", 0)
                    val todayDone = prefs.getBoolean("today_done", false)
                    val weeklyTarget = 4
                    
                    views.setTextViewText(R.id.tvStreakCount, streak.toString())
                    views.setTextViewText(R.id.tvWeeklyCount, workoutsThisWeek.toString())
                    views.setTextViewText(R.id.tvWeeklyTarget, "/ $weeklyTarget workouts")
                    
                    val progress = if (weeklyTarget > 0) (workoutsThisWeek * 100 / weeklyTarget) else 0
                    views.setProgressBar(R.id.progressWeekly, 100, progress, false)
                    
                    if (todayDone) {
                        views.setTextViewText(R.id.tvTodayStatus, "✓ Done")
                        views.setTextColor(R.id.tvTodayStatus, 0xFF4ECDC4.toInt())
                    } else {
                        views.setTextViewText(R.id.tvTodayStatus, "○ Pending")
                        views.setTextColor(R.id.tvTodayStatus, 0xFFFF6B6B.toInt())
                    }
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Widget added for the first time
    }
    
    override fun onDisabled(context: Context) {
        // Widget removed
    }
}