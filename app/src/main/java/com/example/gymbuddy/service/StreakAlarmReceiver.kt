package com.csci3310.gymbuddy.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.csci3310.gymbuddy.MainActivity

class StreakAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notificationCount = prefs.getInt(KEY_NOTIFICATION_COUNT, 0)
        val realStreak = prefs.getInt(KEY_STREAK, 0)
        val newNotificationCount = notificationCount + 1

        prefs.edit().putInt(KEY_NOTIFICATION_COUNT, newNotificationCount).apply()

        val (title, message) = getEscalatingContent(newNotificationCount, realStreak)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        scheduleAlarm(context, INTERVAL_MINUTES)
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Motivational notifications to keep your workout streak alive"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getEscalatingContent(notificationCount: Int, realStreak: Int): Pair<String, String> {
        val fakeStreak = notificationCount
        val realStreakDisplay = if (realStreak > 0) " ($realStreak day real streak)" else ""

        return when {
            fakeStreak >= 60 -> Pair(
                "💀💀💀 $fakeStreak DAYS OF SILENCE 💀💀💀",
                "Your streak has been dead for an HOUR. We gave up counting. Just... go workout. Please."
            )
            fakeStreak >= 45 -> Pair(
                "🚨🔥 THE STREAK IS CRYING 🔥🚨",
                "Your streak went from '🔥 45 day beast 🔥' to this. It's embarrassed. Fix it."
            )
            fakeStreak >= 30 -> Pair(
                "🔥🔥🔥 DAY $fakeStreak - THIS IS PATHETIC 🔥🔥🔥",
                "A whole month of reminders and you're STILL reading this instead of working out?!"
            )
            fakeStreak >= 20 -> Pair(
                "💀 DAY $fakeStreak - YOUR POTENTIAL DIED",
                "Your muscles have forgotten you exist. Reconnect. GO GYM NOW."
            )
            fakeStreak >= 14 -> Pair(
                "😭 TWO WEEKS OF REGRETS 😭",
                "You've seen 200+ of these notifications. Your body is judging you. Time to prove it wrong!"
            )
            fakeStreak >= 10 -> Pair(
                "🚨 DAY $fakeStreak - DON'T BE A COWARD 🚨",
                "Double digits! But for what? To quit? NO. Keep going. ONE. MORE. DAY."
            )
            fakeStreak >= 7 -> Pair(
                "😤 ONE WEEK... ALMOST... DON'T QUIT NOW 😤",
                "Day $fakeStreak fake streak$realStreakDisplay but hey, at least you're consistent at reading these!"
            )
            fakeStreak >= 5 -> Pair(
                "😰 Day $fakeStreak - Your streak is embarrassed 😰",
                "It doesn't want to show its face. Give it something to be proud of!"
            )
            fakeStreak >= 3 -> Pair(
                "⚡ Day $fakeStreak - Momentum is BUILDING ⚡",
                "Almost a week! Can you feel the gains? They're waiting. Don't stop now!"
            )
            fakeStreak >= 2 -> Pair(
                "🏃 Day $fakeStreak - Your streak is WAITING 🏃",
                "Just 2 more days and it's officially a streak. Is that so hard?!"
            )
            else -> Pair(
                "🏃 Start your streak today! 🏃",
                "Day 1$realStreakDisplay - Every legend started somewhere. That somewhere is the gym. GO!"
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "streak_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val PREFS_NAME = "streak_prefs"
        const val KEY_STREAK = "current_streak"
        const val KEY_NOTIFICATION_COUNT = "notification_count"
        const val INTERVAL_MINUTES = 2L

        fun scheduleAlarm(context: Context, intervalMinutes: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, StreakAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intervalMillis = intervalMinutes * 60 * 1000
            val triggerTime = System.currentTimeMillis() + intervalMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, StreakAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        fun updateStreak(context: Context, streak: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_STREAK, streak)
                .putInt(KEY_NOTIFICATION_COUNT, 0)
                .apply()
        }

        fun resetNotificationCount(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_NOTIFICATION_COUNT, 0)
                .apply()
        }
    }
}
