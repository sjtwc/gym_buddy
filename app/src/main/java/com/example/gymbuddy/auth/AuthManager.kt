package com.csci3310.gymbuddy.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.csci3310.gymbuddy.data.local.CalendarPreferences
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AuthState(
    val isConnected: Boolean = false,
    val email: String? = null,
    val error: String? = null
)

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarPreferences: CalendarPreferences
) {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    companion object {
        const val CALENDAR_SCOPE = "oauth2:https://www.googleapis.com/auth/calendar"
    }

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<Intent>) {
        activityResultLauncher = launcher
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CALENDAR_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return getGoogleSignInClient().signInIntent
    }

    fun handleSignInResult(data: Intent?) {
        Log.d("AuthManager", "handleSignInResult called")

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        task.addOnSuccessListener { account ->
            Log.d("AuthManager", "Google sign-in success: ${account.email}")

            // Get access token using GoogleAuthUtil
            try {
                val accessToken = GoogleAuthUtil.getToken(
                    context,
                    account.account!!,
                    CALENDAR_SCOPE
                )

                calendarPreferences.saveAccessToken(accessToken)
                calendarPreferences.saveAccountEmail(account.email ?: "")
                calendarPreferences.saveTokenExpiry(System.currentTimeMillis() + 3600_000)
                calendarPreferences.saveRefreshToken("")

                _authState.value = AuthState(
                    isConnected = true,
                    email = account.email
                )
                Log.d("AuthManager", "Auth state updated - connected: true, email: ${account.email}")
            } catch (e: Exception) {
                Log.e("AuthManager", "Failed to get access token", e)
                _authState.value = AuthState(
                    isConnected = false,
                    email = account.email,
                    error = "Failed to get access token: ${e.message}"
                )
            }
        }.addOnFailureListener { e ->
            Log.e("AuthManager", "Google sign-in failed", e)
            _authState.value = AuthState(error = e.message)
        }
    }

    fun refreshAuthState() {
        val account = GoogleSignIn.getLastSignedInAccount(context)

        if (account != null) {
            try {
                val accessToken = GoogleAuthUtil.getToken(
                    context,
                    account.account!!,
                    CALENDAR_SCOPE
                )

                calendarPreferences.saveAccessToken(accessToken)
                calendarPreferences.saveAccountEmail(account.email ?: "")

                _authState.value = AuthState(
                    isConnected = true,
                    email = account.email
                )
            } catch (e: Exception) {
                _authState.value = AuthState(
                    isConnected = false,
                    error = e.message
                )
            }
        } else {
            _authState.value = AuthState()
        }
    }

    fun getGoogleAccessToken(): String? {
        return calendarPreferences.getAccessToken()
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && calendarPreferences.hasValidTokens()
    }

    fun getCurrentUserEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    fun signOut() {
        getGoogleSignInClient().signOut()
        calendarPreferences.clearTokens()
        _authState.value = AuthState()
        Log.d("AuthManager", "Signed out")
    }

    fun launchSignIn() {
        activityResultLauncher?.launch(getSignInIntent())
    }
}