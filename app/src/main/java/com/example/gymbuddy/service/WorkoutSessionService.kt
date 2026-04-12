package com.example.gymbuddy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymbuddy.MainActivity
import com.example.gymbuddy.domain.model.*
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
    private var restTimerJob: Job? = null
    
    private val _workoutSession = MutableStateFlow<WorkoutSession?>(null)
    val workoutSession: StateFlow<WorkoutSession?> = _workoutSession.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private val _restTime = MutableStateFlow(0)
    val restTime: StateFlow<Int> = _restTime.asStateFlow()
    
    companion object {
        const val CHANNEL_ID = "workout_session_channel"
        const val NOTIFICATION_ID = 1002
        const val TIMER_END_CHANNEL_ID = "rest_timer_end_channel"
        const val TIMER_END_NOTIFICATION_ID = 1003
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
            workoutName = workoutName.ifEmpty { WorkoutSession.generateDefaultName() },
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
    
    fun addExercise(exercise: Exercise) {
        _workoutSession.value?.let { session ->
            val newExercise = WorkoutExerciseSession(exercise = exercise)
            val updatedExercises = session.exercises + newExercise
            _workoutSession.value = session.copy(exercises = updatedExercises)
            updateNotification()
        }
    }
    
    fun removeExercise(exerciseIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList().apply {
                removeAt(exerciseIndex)
            }
            val newIndex = minOf(session.currentExerciseIndex, updatedExercises.size - 1)
            _workoutSession.value = session.copy(
                exercises = updatedExercises,
                currentExerciseIndex = maxOf(0, newIndex)
            )
            updateNotification()
        }
    }
    
    fun swapExercise(exerciseIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList().apply {
                if (isNotEmpty()) {
                    this[exerciseIndex] = WorkoutExerciseSession(
                        exercise = get(exerciseIndex).exercise
                    )
                }
            }
            _workoutSession.value = session.copy(exercises = updatedExercises)
            updateNotification()
        }
    }
    
    fun addSet(exerciseIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val newSetNumber = exerciseSession.sets.size + 1
                val newSet = WorkoutSetData(setNumber = newSetNumber)
                val updatedSets = exerciseSession.sets + newSet
                updatedExercises[exerciseIndex] = exerciseSession.copy(sets = updatedSets)
                _workoutSession.value = session.copy(exercises = updatedExercises)
                updateNotification()
            }
        }
    }
    
    fun updateSet(exerciseIndex: Int, setIndex: Int, set: WorkoutSetData) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedSets = exerciseSession.sets.toMutableList().apply {
                    if (setIndex < size) this[setIndex] = set
                }
                updatedExercises[exerciseIndex] = exerciseSession.copy(sets = updatedSets)
                _workoutSession.value = session.copy(exercises = updatedExercises)
            }
        }
    }
    
    fun deleteSet(exerciseIndex: Int, setIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedSets = exerciseSession.sets.toMutableList().apply {
                    if (setIndex < size) removeAt(setIndex)
                }
                updatedExercises[exerciseIndex] = exerciseSession.copy(sets = updatedSets)
                _workoutSession.value = session.copy(exercises = updatedExercises)
                updateNotification()
            }
        }
    }
    
    fun toggleSetType(exerciseIndex: Int, setType: SetType) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedSets = exerciseSession.sets.toMutableList()
                if (updatedSets.isNotEmpty()) {
                    // If we have at least one set, update the first (or add new logic for specific set)
                    // For now, just use first set - each exercise shares same first set type in UI
                    val currentSet = updatedSets[0]
                    updatedSets[0] = currentSet.copy(setType = setType)
                    updatedExercises[exerciseIndex] = exerciseSession.copy(sets = updatedSets)
                    _workoutSession.value = session.copy(exercises = updatedExercises)
                }
            }
        }
    }
    
    // New specific toggle function for individual sets
    fun toggleSetTypeForSet(exerciseIndex: Int, setIndex: Int, setType: SetType) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedSets = exerciseSession.sets.toMutableList()
                if (setIndex < updatedSets.size) {
                    // Update the set type
                    val currentSet = updatedSets[setIndex]
                    updatedSets[setIndex] = currentSet.copy(setType = setType)
                    
                    // Renumber: NORMAL sets get 1,2,3..., non-NORMAL use position
                    var normalNum = 1
                    val renumberedSets = updatedSets.mapIndexed { idx, set ->
                        if (set.setType == SetType.NORMAL) {
                            set.copy(setNumber = normalNum++)
                        } else {
                            set.copy(setNumber = idx + 1)
                        }
                    }
                    
                    updatedExercises[exerciseIndex] = exerciseSession.copy(sets = renumberedSets)
                    _workoutSession.value = session.copy(exercises = updatedExercises)
                }
            }
        }
    }
    
    fun completeSet(exerciseIndex: Int, setIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedSets = exerciseSession.sets.toMutableList()
                if (setIndex < updatedSets.size) {
                    val currentSet = updatedSets[setIndex]
                    updatedSets[setIndex] = currentSet.copy(isCompleted = true)
                    updatedExercises[exerciseIndex] = exerciseSession.copy(sets = updatedSets)
                    _workoutSession.value = session.copy(exercises = updatedExercises)
                    
                    exerciseSession.restTimers.find { it.type == currentSet.setType }?.let { timer ->
                        if (timer.durationSeconds > 0) {
                            startRestTimer(timer.durationSeconds)
                        }
                    }
                }
            }
        }
    }
    
    fun addRestTimer(exerciseIndex: Int, type: SetType, durationSeconds: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val newTimer = RestTimer(type = type, durationSeconds = durationSeconds)
                val updatedTimers = exerciseSession.restTimers.toMutableList().apply {
                    val existing = indexOfFirst { it.type == type }
                    if (existing >= 0) this[existing] = newTimer
                    else add(newTimer)
                }
                updatedExercises[exerciseIndex] = exerciseSession.copy(restTimers = updatedTimers)
                _workoutSession.value = session.copy(exercises = updatedExercises)
                updateNotification()
            }
        }
    }
    
    fun updateRestTimer(exerciseIndex: Int, type: SetType, durationSeconds: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedTimers = exerciseSession.restTimers.toMutableList()
                val existingIdx = updatedTimers.indexOfFirst { it.type == type }
                if (existingIdx >= 0) {
                    updatedTimers[existingIdx] = updatedTimers[existingIdx].copy(durationSeconds = durationSeconds)
                } else {
                    updatedTimers.add(RestTimer(type, durationSeconds, false))
                }
                updatedExercises[exerciseIndex] = exerciseSession.copy(restTimers = updatedTimers)
                _workoutSession.value = session.copy(exercises = updatedExercises)
                updateNotification()
            }
        }
    }
    
    fun deleteRestTimer(exerciseIndex: Int) {
        _workoutSession.value?.let { session ->
            val updatedExercises = session.exercises.toMutableList()
            if (exerciseIndex < updatedExercises.size) {
                val exerciseSession = updatedExercises[exerciseIndex]
                val updatedTimers = exerciseSession.restTimers.toMutableList()
                if (updatedTimers.isNotEmpty()) {
                    updatedTimers.removeAt(0)
                    updatedExercises[exerciseIndex] = exerciseSession.copy(restTimers = updatedTimers)
                }
                _workoutSession.value = session.copy(exercises = updatedExercises)
                updateNotification()
            }
        }
    }
    
    fun updateWorkoutName(name: String) {
        _workoutSession.value?.let { session ->
            _workoutSession.value = session.copy(workoutName = name)
            updateNotification()
        }
    }
    
    fun updateStartTime(startTime: Long) {
        _workoutSession.value?.let { session ->
            val newDuration = session.elapsedSeconds
            _workoutSession.value = session.copy(startedAt = startTime)
            _elapsedTime.value = newDuration
            updateNotification()
        }
    }
    
    fun finishWorkout() {
        _workoutSession.value = _workoutSession.value?.copy(isCompleted = true)
        restTimerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun cancelWorkout() {
        stopSession()
    }
    
    private var timerStartTime: Long = 0
    private var timerTotalTime: Int = 0
    
    private fun startRestTimer(seconds: Int, timerType: SetType = SetType.WORK) {
        restTimerJob?.cancel()
        _restTime.value = seconds
        timerStartTime = System.currentTimeMillis()
        timerTotalTime = seconds
        _workoutSession.value = _workoutSession.value?.copy(
            isResting = true,
            restTimerType = timerType
        )
        
        restTimerJob = scope.launch {
            while (_restTime.value > 0) {
                delay(1000)
                _restTime.value -= 1
                updateNotification()
            }
            _workoutSession.value = _workoutSession.value?.copy(isResting = false)
            sendTimerEndNotification()
            playTimerSound()
            updateNotification()
        }
    }
    
    private fun pauseRestTimer() {
        restTimerJob?.cancel()
    }
    
    private fun resumeRestTimer() {
        val remainingTime = _restTime.value
        if (remainingTime > 0) {
            startRestTimer(remainingTime, _workoutSession.value?.restTimerType ?: SetType.WORK)
        }
    }
    
    private fun adjustRestTimer(seconds: Int) {
        val newTime = (_restTime.value + seconds).coerceIn(0, 600)
        _restTime.value = newTime
        timerTotalTime = (timerTotalTime + seconds).coerceIn(0, 600)
    }
    
    private fun stopRestTimer() {
        restTimerJob?.cancel()
        _workoutSession.value = _workoutSession.value?.copy(isResting = false)
        _restTime.value = 0
    }
    
    private fun playTimerSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getNextSetInfo(): String {
        val session = _workoutSession.value ?: return ""
        val exercises = session.exercises
        if (exercises.isEmpty()) return ""
        
        for ((exIdx, exercise) in exercises.withIndex()) {
            for ((setIdx, set) in exercise.sets.withIndex()) {
                if (!set.isCompleted) {
                    val weight = set.weight ?: 0.0
                    val reps = set.reps ?: 0
                    return "${exercise.exercise.name}: ${weight}kg x $reps reps"
                }
            }
        }
        return "Workout complete!"
    }
    
    private fun sendTimerEndNotification() {
        val session = _workoutSession.value
        if (session == null) return
        
        val nextSetInfo = getNextSetInfo()
        
        val channel = NotificationChannel(
            TIMER_END_CHANNEL_ID,
            "Rest Timer Complete",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Rest timer completed"
            enableVibration(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, TIMER_END_CHANNEL_ID)
            .setContentTitle("Rest Timer Complete!")
            .setContentText("Next: $nextSetInfo")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(TIMER_END_NOTIFICATION_ID, notification)
    }
    
    private fun stopSession() {
        timerJob?.cancel()
        restTimerJob?.cancel()
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
        
        val exerciseName = session.currentExercise?.exercise?.name ?: "Starting..."
        val exerciseCount = session.exercises.size
        val elapsedStr = formatTime(_elapsedTime.value.toInt())
        
        val contentText = buildString {
            append("$exerciseName • $exerciseCount exercises • ⏱ $elapsedStr")
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
        restTimerJob?.cancel()
        scope.cancel()
    }
    
    inner class WorkoutSessionBinder : Binder() {
        fun getService(): WorkoutSessionService = this@WorkoutSessionService
    }
}