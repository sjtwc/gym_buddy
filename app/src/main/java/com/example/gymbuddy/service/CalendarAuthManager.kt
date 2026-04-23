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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

data class CalendarAuthState(
    val isConnected: Boolean = false,
    val email: String? = null,
    val error: String? = null
)

@Singleton
class CalendarAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val calendarPreferences by lazy { CalendarPreferences(context) }

    private val _authState = MutableStateFlow(CalendarAuthState())
    val authState: StateFlow<CalendarAuthState> = _authState.asStateFlow()

    companion object {
        const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"
        const val CALENDAR_READ_ONLY_SCOPE = "https://www.googleapis.com/auth/calendar.readonly"
        private const val SECRETS_FILE = "secrets.properties"
        private const val CLIENT_ID_KEY = "CLIENT_ID"
    }

    private var signInClient: GoogleSignInClient? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private fun getClientId(): String {
        return try {
            val secretsFile = File(context.filesDir.parent, SECRETS_FILE)
            if (secretsFile.exists()) {
                val properties = Properties()
                FileInputStream(secretsFile).use { fis ->
                    properties.load(fis)
                }
                properties.getProperty(CLIENT_ID_KEY, "")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getSignInClient(): GoogleSignInClient {
        if (signInClient == null) {
            val clientId = getClientId()
            val optionsBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(CALENDAR_SCOPE))

            if (clientId.isNotEmpty()) {
                optionsBuilder.requestServerAuthCode(clientId)
            }

            signInClient = GoogleSignIn.getClient(context, optionsBuilder.build())
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

    suspend fun handleSignInResult(intent: Intent): Boolean = withContext(Dispatchers.Main) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            var success = false
            task.addOnSuccessListener { account ->
                kotlinx.coroutines.runBlocking {
                    try {
                        val serverAuthCode = account.serverAuthCode
                        if (serverAuthCode != null) {
                            calendarPreferences.saveAccessToken(serverAuthCode)
                            calendarPreferences.saveRefreshToken("")
                            calendarPreferences.saveTokenExpiry(System.currentTimeMillis() + 3600_000)
                            calendarPreferences.saveAccountEmail(account.email ?: "")

                            _authState.value = CalendarAuthState(
                                isConnected = true,
                                email = account.email
                            )
                            success = true
                        } else {
                            _authState.value = CalendarAuthState(error = "No server auth code received")
                        }
                    } catch (e: Exception) {
                        _authState.value = CalendarAuthState(error = e.message)
                    }
                }
            }.addOnFailureListener { e ->
                kotlinx.coroutines.runBlocking {
                    _authState.value = CalendarAuthState(error = e.message)
                }
            }
            success
        } catch (e: Exception) {
            _authState.value = CalendarAuthState(error = e.message)
            false
        }
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
            _authState.value = CalendarAuthState()
            true
        } catch (e: Exception) {
            calendarPreferences.clearTokens()
            _authState.value = CalendarAuthState()
            false
        }
    }

    fun refreshAuthState() {
        val isConnected = isSignedIn()
        val email = getSignedInAccountEmail()
        _authState.value = CalendarAuthState(
            isConnected = isConnected,
            email = email
        )
    }
}