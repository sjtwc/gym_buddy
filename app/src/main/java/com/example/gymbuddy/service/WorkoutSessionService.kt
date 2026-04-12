package com.example.gymbuddy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymbuddy.MainActivity
import com.example.gymbuddy.R
import com.example.gymbuddy.domain.model.WorkoutSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class WorkoutSessionService : Service() {
    
    private val binder = WorkoutSessionBinder()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var timerJob: Job? = null
    
    private val _workoutSession = MutableStateFlow<WorkoutSession?>(null)
    val workoutSession: StateFlow<WorkoutSession?> = _workoutSession.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private val _restTime = MutableStateFlow(0)
    val restTime: StateFlow<Int> = _restTime.asStateFlow()
    
    companion object {
        const val CHANNEL_ID = "workout_session_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "com.example.gymbuddy.START_SESSION"
        const val ACTION_STOP = "com.example.gymbuddy.STOP_SESSION"
        const val ACTION_COMPLETE_SET = "com.example.gymbuddy.COMPLETE_SET"
        const val ACTION_START_REST = "com.example.gymbuddy.START_REST"
        
        const val EXTRA_WORKOUT_ID = "extra_workout_id"
        const val EXTRA_WORKOUT_NAME = "extra_workout_name"
        const val EXTRA_REST_TIME = "extra_rest_time"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, 0)
                val workoutName = intent.getStringExtra(EXTRA_WORKOUT_NAME) ?: "Workout"
                startSession(workoutId, workoutName)
            }
            ACTION_STOP -> {
                stopSession()
            }
            ACTION_START_REST -> {
                val restTime = intent.getIntExtra(EXTRA_REST_TIME, 90)
                startRestTimer(restTime)
            }
        }
        return START_STICKY
    }
    
    fun startSession(workoutId: Long, workoutName: String) {
        val session = WorkoutSession(
            workoutId = workoutId,
            workoutName = workoutName,
            startedAt = System.currentTimeMillis()
        )
        _workoutSession.value = session
        
        startForeground(NOTIFICATION_ID, createNotification())
        startElapsedTimer()
    }
    
    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_workoutSession.value != null) {
                _elapsedTime.value = (System.currentTimeMillis() - (_workoutSession.value?.startedAt ?: 0)) / 1000
                updateNotification()
                delay(1000)
            }
        }
    }
    
    fun updateSession(
        exerciseName: String? = null,
        exerciseIndex: Int? = null,
        totalExercises: Int? = null,
        setIndex: Int? = null,
        totalSets: Int? = null
    ) {
        _workoutSession.value?.let { session ->
            _workoutSession.value = session.copy(
                currentExercise = if (exerciseName != null) {
                    com.example.gymbuddy.domain.model.Exercise(
                        id = 0,
                        name = exerciseName,
                        description = "",
                        targetMuscle = "",
                        equipmentType = ""
                    )
                } else session.currentExercise,
                currentExerciseIndex = exerciseIndex ?: session.currentExerciseIndex,
                totalExercises = totalExercises ?: session.totalExercises,
                currentSetIndex = setIndex ?: session.currentSetIndex,
                totalSets = totalSets ?: session.totalSets
            )
            updateNotification()
        }
    }
    
    private fun startRestTimer(seconds: Int) {
        _restTime.value = seconds
        _workoutSession.value = _workoutSession.value?.copy(isResting = true)
        
        scope.launch {
            while (_restTime.value > 0) {
                delay(1000)
                _restTime.value -= 1
                updateNotification()
            }
            _workoutSession.value = _workoutSession.value?.copy(isResting = false)
            updateNotification()
        }
    }
    
    fun completeWorkout() {
        _workoutSession.value = _workoutSession.value?.copy(isCompleted = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun stopSession() {
        timerJob?.cancel()
        _workoutSession.value = null
        _elapsedTime.value = 0
        _restTime.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows workout progress"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val session = _workoutSession.value ?: return createBasicNotification()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, WorkoutSessionService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val exerciseName = session.currentExercise?.name ?: "Starting..."
        val setProgress = if (session.totalSets > 0) "Set ${session.currentSetIndex + 1}/${session.totalSets}" else "..."
        val elapsedStr = formatTime(_elapsedTime.value.toInt())
        
        val contentText = buildString {
            append("$exerciseName • $setProgress • ⏱ $elapsedStr")
            if (session.isResting && _restTime.value > 0) {
                append(" • Rest: ${formatTime(_restTime.value)}")
            }
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🏋️ ${session.workoutName}")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "End Workout", stopPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .build()
    }
    
    private fun createBasicNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🏋️ Workout")
            .setContentText("Starting workout...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
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
    
    inner class WorkoutSessionBinder : Binder() {
        fun getService(): WorkoutSessionService = this@WorkoutSessionService
    }
}