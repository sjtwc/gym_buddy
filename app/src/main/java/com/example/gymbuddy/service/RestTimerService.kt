package com.csci3310.gymbuddy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.csci3310.gymbuddy.MainActivity
import com.csci3310.gymbuddy.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class RestTimerService : Service() {
    
    private val binder = RestTimerBinder()
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private var totalTime = 90
    
    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.csci3310.gymbuddy.START_TIMER"
        const val ACTION_STOP = "com.csci3310.gymbuddy.STOP_TIMER"
        const val EXTRA_TIME = "extra_time"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val time = intent.getIntExtra(EXTRA_TIME, 90)
                startTimer(time)
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_STICKY
    }
    
    fun startTimer(seconds: Int) {
        totalTime = seconds
        _timeRemaining.value = seconds
        _isRunning.value = true
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
                updateNotification()
            }
            _isRunning.value = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        _isRunning.value = false
        _timeRemaining.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun addTime(seconds: Int) {
        _timeRemaining.value += seconds
        totalTime += seconds
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rest Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows remaining rest time"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Timer")
            .setContentText(formatTime(_timeRemaining.value))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .build()
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", mins, secs)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        scope.cancel()
    }
    
    inner class RestTimerBinder : Binder() {
        fun getService(): RestTimerService = this@RestTimerService
    }
}