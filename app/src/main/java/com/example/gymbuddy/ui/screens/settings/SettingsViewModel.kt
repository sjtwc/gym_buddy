package com.example.gymbuddy.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.local.NotificationPreferences
import com.example.gymbuddy.domain.model.FreeTimeSlot
import com.example.gymbuddy.service.CalendarAuthManager
import com.example.gymbuddy.service.CalendarService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val freeTimeSlots: List<FreeTimeSlot> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences,
    private val calendarAuthManager: CalendarAuthManager,
    private val calendarService: CalendarService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
        checkCalendarConnection()
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

    private fun checkCalendarConnection() {
        viewModelScope.launch {
            val isConnected = calendarAuthManager.isSignedIn()
            val email = if (isConnected) calendarAuthManager.getSignedInAccountEmail() else null
            _uiState.update {
                it.copy(
                    isCalendarConnected = isConnected,
                    googleAccountEmail = email
                )
            }
        }
    }

    fun setDefaultRestTimer(seconds: Int) {
        // TODO: Save to preferences
        _uiState.update { it.copy(defaultRestTimerSeconds = seconds) }
    }

    fun setWeightUnit(unit: String) {
        // TODO: Save to preferences
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
        viewModelScope.launch {
            calendarAuthManager.signIn()
            checkCalendarConnection()
        }
    }

    fun signOutFromCalendar() {
        viewModelScope.launch {
            calendarAuthManager.signOut()
            _uiState.update {
                it.copy(
                    isCalendarConnected = false,
                    googleAccountEmail = null
                )
            }
        }
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
}