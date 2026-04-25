package com.csci3310.gymbuddy.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "notification_settings"
        private const val KEY_STREAK_REMINDER_HOUR = "streak_reminder_hour"
        private const val KEY_STREAK_REMINDER_MINUTE = "streak_reminder_minute"
        private const val KEY_STREAK_REMINDER_ENABLED = "streak_reminder_enabled"
        private const val KEY_REST_TIMER_NOTIFICATIONS_ENABLED = "rest_timer_notifications_enabled"

        const val DEFAULT_STREAK_HOUR = 20
        const val DEFAULT_STREAK_MINUTE = 0
        const val DEFAULT_STREAK_ENABLED = true
        const val DEFAULT_REST_TIMER_ENABLED = true
    }

    fun getStreakReminderHour(): Int {
        return prefs.getInt(KEY_STREAK_REMINDER_HOUR, DEFAULT_STREAK_HOUR)
    }

    fun getStreakReminderMinute(): Int {
        return prefs.getInt(KEY_STREAK_REMINDER_MINUTE, DEFAULT_STREAK_MINUTE)
    }

    fun setStreakReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_STREAK_REMINDER_HOUR, hour)
            .putInt(KEY_STREAK_REMINDER_MINUTE, minute)
            .apply()
    }

    fun isStreakReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_STREAK_REMINDER_ENABLED, DEFAULT_STREAK_ENABLED)
    }

    fun setStreakReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STREAK_REMINDER_ENABLED, enabled).apply()
    }

    fun isRestTimerNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_REST_TIMER_NOTIFICATIONS_ENABLED, DEFAULT_REST_TIMER_ENABLED)
    }

    fun setRestTimerNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REST_TIMER_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getStreakReminderTimeFormatted(): String {
        val hour = getStreakReminderHour()
        val minute = getStreakReminderMinute()
        return String.format("%02d:%02d", hour, minute)
    }
}