package com.example.gymbuddy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymbuddy.R
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userProfileRepository: UserProfileRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val profile = userProfileRepository.getUserProfileSync()
            
            if (profile != null) {
                val streak = profile.currentStreak
                
                if (streak > 0) {
                    val (title, message) = getMotivationalMessage(streak)
                    showNotification(title, message, streak)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getMotivationalMessage(streak: Int): Pair<String, String> {
        return when {
            streak >= 30 -> "🔥 $streak DAY STREAK! Don't let it die!" to "You've built an incredible habit! One workout today keeps the beast alive!"
            streak >= 14 -> "💪 $streak days - You're on fire!" to "Two weeks strong! Your future self is begging you to keep going!"
            streak >= 7 -> "🎯 $streak day streak - Almost a week!" to "Don't quit now! You're so close to a full week. Just one workout!"
            streak >= 3 -> "⚡ $streak day streak building!" to "Momentum is building! Keep the chain alive!"
            else -> "🏃 Start your streak today!" to "Your first workout starts the journey!"
        }
    }

    private fun showNotification(title: String, message: String, streak: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
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
    }

    companion object {
        const val CHANNEL_ID = "streak_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "streak_reminder_work"
    }
}