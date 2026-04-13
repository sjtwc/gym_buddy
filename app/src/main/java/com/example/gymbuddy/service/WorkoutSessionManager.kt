package com.example.gymbuddy.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.gymbuddy.data.repository.WorkoutRepository
import com.example.gymbuddy.data.repository.UserProfileRepository
import com.example.gymbuddy.data.repository.AchievementRepository
import com.example.gymbuddy.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutRepository: WorkoutRepository,
    private val userProfileRepository: UserProfileRepository,
    private val notificationService: NotificationService
) {
    private var service: WorkoutSessionService? = null
    private var bound = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()
    
    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()
    
    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()
    
    private val _isTimerMinimized = MutableStateFlow(false)
    val isTimerMinimized: StateFlow<Boolean> = _isTimerMinimized.asStateFlow()
    
    private val _timerTotalTime = MutableStateFlow(0)
    val timerTotalTime: StateFlow<Int> = _timerTotalTime.asStateFlow()
    
    private val _activeRestExerciseIndex = MutableStateFlow(-1)
    val activeRestExerciseIndex: StateFlow<Int> = _activeRestExerciseIndex.asStateFlow()
    
    private val _activeRestSetIndex = MutableStateFlow(-1)
    val activeRestSetIndex: StateFlow<Int> = _activeRestSetIndex.asStateFlow()
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as WorkoutSessionService.WorkoutSessionBinder
            service = serviceBinder.getService()
            bound = true
            observeServiceState()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }
    
    private fun observeServiceState() {
        service?.let { svc ->
            scope.launch {
                svc.workoutSession.collect { session ->
                    _currentSession.value = session
                    _isActive.value = session != null && session?.isCompleted != true
                }
            }
            scope.launch {
                svc.elapsedTime.collect { time ->
                    _elapsedTime.value = time
                }
            }
            scope.launch {
                svc.restTime.collect { time ->
                    _restTimeRemaining.value = time
                    _isResting.value = time > 0
                }
            }
            scope.launch {
                svc.isTimerMinimized.collect { minimized ->
                    _isTimerMinimized.value = minimized
                }
            }
            scope.launch {
                svc.timerTotalTime.collect { total ->
                    _timerTotalTime.value = total
                }
            }
            scope.launch {
                svc.activeRestExerciseIndex.collect { index ->
                    _activeRestExerciseIndex.value = index
                }
            }
            scope.launch {
                svc.activeRestSetIndex.collect { index ->
                    _activeRestSetIndex.value = index
                }
            }
        }
    }
    
    fun startSession(workoutId: Long, workoutName: String) {
        _isExpanded.value = true
        val defaultName = workoutName.ifEmpty { WorkoutSession.generateDefaultName() }
        val intent = Intent(context, WorkoutSessionService::class.java).apply {
            action = WorkoutSessionService.ACTION_START
            putExtra(WorkoutSessionService.EXTRA_WORKOUT_ID, workoutId)
            putExtra(WorkoutSessionService.EXTRA_WORKOUT_NAME, defaultName)
        }
        context.startForegroundService(intent)
        bindService()
    }
    
    private fun bindService() {
        val intent = Intent(context, WorkoutSessionService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun startRestTimer(seconds: Int) {
        val intent = Intent(context, WorkoutSessionService::class.java).apply {
            action = WorkoutSessionService.ACTION_START_REST
            putExtra(WorkoutSessionService.EXTRA_REST_TIME, seconds)
        }
        context.startService(intent)
    }
    
    fun stopSession() {
        val intent = Intent(context, WorkoutSessionService::class.java).apply {
            action = WorkoutSessionService.ACTION_STOP
        }
        context.startService(intent)
        resetState()
    }
    
    fun toggleExpanded() {
        _isExpanded.value = !_isExpanded.value
    }
    
    fun collapse() {
        _isExpanded.value = false
    }
    
    fun expand() {
        _isExpanded.value = true
    }
    
    fun showExercisePicker() {
        _isExpanded.value = true
    }
    
    fun addExercise(exercise: Exercise) {
        service?.addExercise(exercise)
    }
    
    fun removeExercise(exerciseIndex: Int) {
        service?.removeExercise(exerciseIndex)
    }
    
    fun swapExercise(exerciseIndex: Int) {
        service?.swapExercise(exerciseIndex)
    }
    
    fun addSet(exerciseIndex: Int) {
        service?.addSet(exerciseIndex)
    }
    
    fun updateSet(exerciseIndex: Int, setIndex: Int, set: WorkoutSetData) {
        service?.updateSet(exerciseIndex, setIndex, set)
    }
    
    fun deleteSet(exerciseIndex: Int, setIndex: Int) {
        service?.deleteSet(exerciseIndex, setIndex)
    }
    
    fun toggleSetType(exerciseIndex: Int, setType: SetType) {
        service?.toggleSetType(exerciseIndex, setType)
    }
    
    fun toggleSetTypeForSet(exerciseIndex: Int, setIndex: Int, setType: SetType) {
        service?.toggleSetTypeForSet(exerciseIndex, setIndex, setType)
    }
    
    fun completeSet(exerciseIndex: Int, setIndex: Int) {
        service?.completeSet(exerciseIndex, setIndex)
    }
    
    fun addRestTimer(type: SetType, durationSeconds: Int) {
        service?.addRestTimer(0, type, durationSeconds)
    }
    
    fun updateRestTimer(exerciseIndex: Int, type: SetType, durationSeconds: Int) {
        service?.updateRestTimer(exerciseIndex, type, durationSeconds)
    }
    
    fun deleteRestTimer(type: SetType) {
        service?.deleteRestTimer(0)
    }
    
    fun minimizeTimer() {
        service?.minimizeTimer()
    }
    
    fun restoreTimer() {
        service?.restoreTimer()
    }
    
    fun adjustTimerTime(seconds: Int) {
        service?.adjustTimerTime(seconds)
    }
    
    fun enableRestTimer(exerciseIndex: Int) {
        service?.enableRestTimer(exerciseIndex)
    }
    
    fun saveRestTimers(exerciseIndex: Int, timers: List<com.example.gymbuddy.domain.model.RestTimer>) {
        service?.saveRestTimers(exerciseIndex, timers)
    }
    
    fun disableRestTimer(exerciseIndex: Int) {
        service?.disableRestTimer(exerciseIndex)
    }
    
    fun resetExercise(exerciseIndex: Int) {
        service?.resetExercise(exerciseIndex)
    }
    
    fun finishWorkout(feeling: Int? = null) {
        val session = _currentSession.value ?: return
        val duration = (_elapsedTime.value / 60).toInt()
        service?.finishWorkout()
        
        scope.launch {
            val volume = workoutRepository.saveCompletedWorkout(session, duration, feeling)
            val earnedAchievements = userProfileRepository.recordWorkout(totalVolume = volume)
            
            val profile = userProfileRepository.getUserProfileSync()
            profile?.let {
                StreakAlarmReceiver.updateStreak(context, it.currentStreak)
                StreakAlarmReceiver.scheduleAlarm(context, StreakAlarmReceiver.INTERVAL_MINUTES)
            }
            
            if (earnedAchievements.isNotEmpty()) {
                if (earnedAchievements.size == 1) {
                    notificationService.showAchievementNotification(earnedAchievements.first())
                } else {
                    notificationService.showMultipleAchievementsNotification(earnedAchievements)
                }
            }
            
            com.example.gymbuddy.widget.WidgetPreferences.updateFromDatabase(context)
        }
        
        resetState()
    }
    
    fun cancelWorkout() {
        service?.cancelWorkout()
        resetState()
    }
    
    fun updateWorkoutName(name: String) {
        service?.updateWorkoutName(name)
    }
    
    fun updateStartTime(startTime: Long) {
        service?.updateStartTime(startTime)
    }
    
    private fun resetState() {
        _isActive.value = false
        _isExpanded.value = false
        _currentSession.value = null
        _elapsedTime.value = 0
        _isResting.value = false
        _restTimeRemaining.value = 0
    }
    
    fun onCleared() {
        scope.cancel()
    }
}