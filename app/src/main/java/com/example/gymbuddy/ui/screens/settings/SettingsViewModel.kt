package com.example.gymbuddy.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.auth.AuthManager
import com.example.gymbuddy.data.local.CalendarPreferences
import com.example.gymbuddy.data.local.NotificationPreferences
import com.example.gymbuddy.data.local.dao.PersonalRecordDao
import com.example.gymbuddy.data.local.dao.ScheduledWorkoutDao
import com.example.gymbuddy.data.local.entity.PersonalRecordEntity
import com.example.gymbuddy.domain.model.FreeTimeSlot
import com.example.gymbuddy.service.CalendarEventExporter
import com.example.gymbuddy.service.CalendarService
import com.example.gymbuddy.service.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val defaultRestTimerSeconds: Int = 90,
    val weightUnit: String = "kg",
    val streakReminderEnabled: Boolean = true,
    val streakReminderHour: Int = 20,
    val streakReminderMinute: Int = 0,
    val streakReminderTime: String = "20:00",
    val restTimerNotificationsEnabled: Boolean = true,
    val isCalendarConnected: Boolean = false,
    val googleAccountEmail: String? = null,
    val autoSyncEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncResult: String? = null,
    val showFreeTimeDialog: Boolean = false,
    val freeTimeSlots: List<FreeTimeSlot> = emptyList(),
    val isHealthConnectAvailable: Boolean = false,
    val isExportingHealth: Boolean = false,
    val showResetDialog: Boolean = false,
    val isResetting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences,
    private val calendarPreferences: CalendarPreferences,
    private val authManager: AuthManager,
    private val calendarService: CalendarService,
    private val calendarEventExporter: CalendarEventExporter,
    private val healthConnectManager: HealthConnectManager,
    private val personalRecordDao: PersonalRecordDao,
    private val scheduledWorkoutDao: ScheduledWorkoutDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
        loadCalendarSettings()
        checkHealthConnect()
        observeCalendarAuth()
    }

    private fun checkHealthConnect() {
        _uiState.update {
            it.copy(isHealthConnectAvailable = healthConnectManager.isAvailable())
        }
    }

    private fun loadCalendarSettings() {
        _uiState.update {
            it.copy(autoSyncEnabled = calendarPreferences.isAutoSyncEnabled())
        }
    }

    private fun observeCalendarAuth() {
        viewModelScope.launch {
            authManager.authState.collect { state ->
                _uiState.update {
                    it.copy(
                        isCalendarConnected = state.isConnected,
                        googleAccountEmail = state.email
                    )
                }
            }
        }
    }

    private fun loadNotificationSettings() {
        _uiState.update {
            it.copy(
                streakReminderEnabled = notificationPreferences.isStreakReminderEnabled(),
                streakReminderHour = notificationPreferences.getStreakReminderHour(),
                streakReminderMinute = notificationPreferences.getStreakReminderMinute(),
                streakReminderTime = notificationPreferences.getStreakReminderTimeFormatted(),
                restTimerNotificationsEnabled = notificationPreferences.isRestTimerNotificationsEnabled()
            )
        }
    }

    fun setDefaultRestTimer(seconds: Int) {
        _uiState.update { it.copy(defaultRestTimerSeconds = seconds) }
    }

    fun setWeightUnit(unit: String) {
        _uiState.update { it.copy(weightUnit = unit) }
    }

    fun setStreakReminderTime(hour: Int, minute: Int, enabled: Boolean) {
        notificationPreferences.setStreakReminderTime(hour, minute)
        notificationPreferences.setStreakReminderEnabled(enabled)
        val timeFormatted = String.format("%02d:%02d", hour, minute)
        _uiState.update {
            it.copy(
                streakReminderHour = hour,
                streakReminderMinute = minute,
                streakReminderTime = timeFormatted,
                streakReminderEnabled = enabled
            )
        }
    }

    fun setRestTimerNotificationsEnabled(enabled: Boolean) {
        notificationPreferences.setRestTimerNotificationsEnabled(enabled)
        _uiState.update { it.copy(restTimerNotificationsEnabled = enabled) }
    }

    fun signInToCalendar() {
        authManager.launchSignIn()
    }

    fun signOutFromCalendar() {
        authManager.signOut()
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        calendarPreferences.setAutoSyncEnabled(enabled)
        _uiState.update { it.copy(autoSyncEnabled = enabled) }
    }

    fun showFreeTimeDialog() {
        viewModelScope.launch {
            val freeSlots = calendarService.findFreeTimeSlots()
            _uiState.update {
                it.copy(
                    showFreeTimeDialog = true,
                    freeTimeSlots = freeSlots
                )
            }
        }
    }

    fun hideFreeTimeDialog() {
        _uiState.update { it.copy(showFreeTimeDialog = false) }
    }

    fun syncToCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, lastSyncResult = null) }
            try {
                val exportedCount = calendarEventExporter.exportWeeklySchedule()
                val message = if (exportedCount > 0) {
                    "Successfully exported $exportedCount workout(s) to calendar"
                } else {
                    "No workouts to export. Make sure you have a weekly schedule set up."
                }
                _uiState.update { it.copy(isSyncing = false, lastSyncResult = message) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSyncing = false, lastSyncResult = "Sync failed: ${e.message}") 
                }
            }
        }
    }
    
    fun clearSyncResult() {
        _uiState.update { it.copy(lastSyncResult = null) }
    }

    fun exportToHealthConnect() {
        viewModelScope.launch {
            if (!healthConnectManager.isAvailable()) {
                val appName = healthConnectManager.getAvailableAppName()
                _uiState.update { 
                    it.copy(lastSyncResult = "No health app found. Please install Google Fit from Play Store.") 
                }
                // Try to open Play Store
                healthConnectManager.openApp()
                return@launch
            }
            
            _uiState.update { it.copy(isExportingHealth = true) }
            
            try {
                val appName = healthConnectManager.getAvailableAppName()
                val sessions = healthConnectManager.getRecentSessions(10)
                
                if (sessions.isEmpty()) {
                    _uiState.update { 
                        it.copy(isExportingHealth = false, lastSyncResult = "No completed workouts to export") 
                    }
                    return@launch
                }
                
                // Open the health app for user to view workouts
                healthConnectManager.openApp()
                
                val message = "Opened $appName with your workout data"
                _uiState.update { it.copy(isExportingHealth = false, lastSyncResult = message) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isExportingHealth = false, lastSyncResult = "Export failed: ${e.message}") 
                }
            }
        }
    }

    fun showResetConfirmation() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun hideResetConfirmation() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetAllRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }
            try {
                val allRecords = personalRecordDao.getAllRecords(Int.MAX_VALUE).first()
                allRecords.forEach { record ->
                    personalRecordDao.deleteRecord(record)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
            _uiState.update { it.copy(isResetting = false, showResetDialog = false) }
        }
    }
}