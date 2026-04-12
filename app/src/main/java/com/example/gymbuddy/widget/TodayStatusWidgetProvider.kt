package com.example.gymbuddy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.gymbuddy.R
import com.example.gymbuddy.MainActivity

class TodayStatusWidgetProvider : AppWidgetProvider() {
    
    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_today)
            
            val todayDone = WidgetPreferences.getTodayDone(context)
            
            if (todayDone) {
                views.setTextViewText(R.id.tvTodayStatus, "✓ Done")
                views.setTextColor(R.id.tvTodayStatus, 0xFF4ECDC4.toInt())
            } else {
                views.setTextViewText(R.id.tvTodayStatus, "○ Pending")
                views.setTextColor(R.id.tvTodayStatus, 0xFFFF6B6B.toInt())
            }
            
            val pendingIntent = createOpenAppIntent(context)
            views.setOnClickPendingIntent(R.id.tvTodayStatus, pendingIntent)
            views.setOnClickPendingIntent(R.id.tvTodayLabel, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        private fun createOpenAppIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
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