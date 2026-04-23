package com.example.gymbuddy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.gymbuddy.R
import com.example.gymbuddy.MainActivity

class WeeklyProgressWidgetProvider : AppWidgetProvider() {
    
    companion object {
        private const val WEEKLY_TARGET = 4
        
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_weekly)
            
            val workoutsThisWeek = WidgetPreferences.getWorkoutsThisWeek(context)
            
            views.setTextViewText(R.id.tvWeeklyCount, workoutsThisWeek.toString())
            views.setTextViewText(R.id.tvWeeklyTarget, "/$WEEKLY_TARGET")
            
            val progress = if (WEEKLY_TARGET > 0) (workoutsThisWeek * 100 / WEEKLY_TARGET) else 0
            views.setProgressBar(R.id.progressWeekly, 100, progress, false)
            
            val pendingIntent = createOpenAppIntent(context)
            views.setOnClickPendingIntent(R.id.tvWeeklyCount, pendingIntent)
            views.setOnClickPendingIntent(R.id.progressWeekly, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        private fun createOpenAppIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("action", "navigate_progress")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
}