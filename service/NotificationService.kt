package com.example.gymbuddy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gymbuddy.R
import com.example.gymbuddy.domain.model.AchievementType
import com.example.gymbuddy.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val achievementChannel = NotificationChannel(
                ACHIEVEMENT_CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Achievement unlock notifications"
                enableVibration(true)
            }
            
            val streakChannel = NotificationChannel(
                STREAK_CHANNEL_ID,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Motivational notifications to keep your workout streak alive"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(achievementChannel)
            notificationManager.createNotificationChannel(streakChannel)
        }
    }
    
    fun showAchievementNotification(achievementType: AchievementType) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "achievement")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            achievementType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("🎉 Achievement Unlocked!")
            .setContentText("${achievementType.icon} ${achievementType.title} - +${achievementType.xpReward} XP")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${achievementType.icon} ${achievementType.title}\n\n${achievementType.description}\n\n+${achievementType.xpReward} XP earned!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        notificationManager.notify(ACHIEVEMENT_NOTIFICATION_BASE_ID + achievementType.ordinal, notification)
    }
    
    fun showMultipleAchievementsNotification(achievements: List<AchievementType>) {
        if (achievements.isEmpty()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "achievement")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val achievementsText = achievements.joinToString("\n") { 
            "${it.icon} ${it.title} (+${it.xpReward} XP)" 
        }
        val totalXp = achievements.sumOf { it.xpReward }
        
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("🎉 ${achievements.size} Achievements Unlocked!")
            .setContentText("+$totalXp XP earned!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You've earned ${achievements.size} achievements:\n\n$achievementsText\n\nTotal: +$totalXp XP"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        notificationManager.notify(ACHIEVEMENT_NOTIFICATION_BASE_ID + 999, notification)
    }
    
    companion object {
        const val ACHIEVEMENT_CHANNEL_ID = "achievement_channel"
        const val STREAK_CHANNEL_ID = "streak_reminder_channel"
        const val ACHIEVEMENT_NOTIFICATION_BASE_ID = 2000
    }
}