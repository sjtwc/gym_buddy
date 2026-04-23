package com.example.gymbuddy.service

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.gymbuddy.data.local.CalendarPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val calendarPreferences by lazy { CalendarPreferences(context) }

    companion object {
        const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"
        const val CALENDAR_READ_ONLY_SCOPE = "https://www.googleapis.com/auth/calendar.readonly"
    }

    private var signInClient: GoogleSignInClient? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun getSignInClient(): GoogleSignInClient {
        if (signInClient == null) {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(CALENDAR_SCOPE))
                .build()
            signInClient = GoogleSignIn.getClient(context, options)
        }
        return signInClient!!
    }

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<Intent>) {
        activityResultLauncher = launcher
    }

    suspend fun signIn(): Result<GoogleSignInAccount> = withContext(Dispatchers.Main) {
        try {
            val client = getSignInClient()
            val intent = client.signInIntent
            activityResultLauncher?.launch(intent)
            Result.failure(Exception("Sign-in requires activity result"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun handleSignInResult(
        intent: Intent,
        onSuccess: (GoogleSignInAccount) -> Unit,
        onFailure: (Exception) -> Unit
    ) = withContext(Dispatchers.Main) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.addOnSuccessListener { account ->
                val authCode = account.serverAuthCode
                if (authCode != null) {
                    calendarPreferences.saveAccessToken(authCode)
                    calendarPreferences.saveRefreshToken("")
                    calendarPreferences.saveTokenExpiry(System.currentTimeMillis() + 3600_000)
                    calendarPreferences.saveAccountEmail(account.email ?: "")
                    onSuccess(account)
                } else {
                    onFailure(Exception("No server auth code"))
                }
            }.addOnFailureListener { e ->
                onFailure(e)
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private suspend fun exchangeAuthCodeForTokens(authCode: String): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                calendarPreferences.saveAccessToken(authCode)
                Pair(authCode, "")
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun refreshTokenIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        if (!calendarPreferences.isTokenExpired()) {
            return@withContext true
        }

        val refreshToken = calendarPreferences.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            return@withContext false
        }

        false
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && calendarPreferences.hasValidTokens()
    }

    fun getSignedInAccountEmail(): String? {
        return calendarPreferences.getAccountEmail() ?: GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    fun getAccessToken(): String? {
        return calendarPreferences.getAccessToken()
    }

    suspend fun signOut(): Boolean = withContext(Dispatchers.Main) {
        try {
            getSignInClient().signOut()
            calendarPreferences.clearTokens()
            true
        } catch (e: Exception) {
            calendarPreferences.clearTokens()
            false
        }
    }
}