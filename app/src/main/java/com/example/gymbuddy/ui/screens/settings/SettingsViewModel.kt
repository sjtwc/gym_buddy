package com.example.gymbuddy.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.auth.AuthManager
import com.example.gymbuddy.data.local.NotificationPreferences
import com.example.gymbuddy.data.local.dao.PersonalRecordDao
import com.example.gymbuddy.data.local.entity.PersonalRecordEntity
import com.example.gymbuddy.domain.model.FreeTimeSlot
import com.example.gymbuddy.service.CalendarService
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
    val showFreeTimeDialog: Boolean = false,
    val freeTimeSlots: List<FreeTimeSlot> = emptyList(),
    val showResetDialog: Boolean = false,
    val isResetting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences,
    private val authManager: AuthManager,
    private val calendarService: CalendarService,
    private val personalRecordDao: PersonalRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
        observeCalendarAuth()
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
            // TODO: Trigger calendar sync
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