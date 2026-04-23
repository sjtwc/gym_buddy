package com.example.gymbuddy.service

import android.content.Context
import com.example.gymbuddy.data.local.CalendarPreferences
import com.example.gymbuddy.domain.model.FreeTimeSlot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val calendarPreferences by lazy { CalendarPreferences(context) }

    companion object {
        private const val CALENDAR_API_BASE = "https://www.googleapis.com/calendar/v3"
    }

    suspend fun findFreeTimeSlots(): List<FreeTimeSlot> = withContext(Dispatchers.IO) {
        try {
            val accessToken = calendarPreferences.getAccessToken() ?: return@withContext emptyList()

            val now = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }

            val timeMin = dateFormat.format(now.time)
            now.add(Calendar.DAY_OF_YEAR, 7)
            val timeMax = dateFormat.format(now.time)

            val url = URL("$CALENDAR_API_BASE/freeBusy?key=AIzaSyDEMO_KEY")

            val body = """
            {
              "timeMin": "$timeMin",
              "timeMax": "$timeMax",
              "items": [{"id": "primary"}]
            }
            """.trimIndent()

            val response = makePostRequest(url, body, accessToken)
            parseFreeBusyResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun makePostRequest(url: URL, body: String, accessToken: String): String {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        connection.outputStream.use { os ->
            os.write(body.toByteArray())
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        reader.close()

        return response
    }

    private fun parseFreeBusyResponse(response: String): List<FreeTimeSlot> {
        val freeSlots = mutableListOf<FreeTimeSlot>()
        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        val startCal = Calendar.getInstance()
        for (dayOffset in 0..6) {
            startCal.add(Calendar.DAY_OF_YEAR, if (dayOffset > 0) 1 else 0)
            val dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames[dayOfWeek]

            freeSlots.add(FreeTimeSlot(
                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(startCal.time),
                dayName = dayName,
                startHour = 6,
                endHour = 22,
                durationHours = 16
            ))
        }

        return freeSlots.filter { it.durationHours >= 2 }
    }

    suspend fun createEvent(
        title: String,
        date: String,
        description: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val accessToken = calendarPreferences.getAccessToken() ?: return@withContext false

            val url = URL("$CALENDAR_API_BASE/calendars/primary/events")

            val eventJson = """
            {
              "summary": "$title",
              "description": ${description.replace("\"", "\\\"")},
              "start": {
                "date": "$date",
                "timeZone": "UTC"
              },
              "end": {
                "date": "$date",
                "timeZone": "UTC"
              },
              "reminders": {
                "useDefault": false,
                "overrides": [
                  {"method": "popup", "minutes": 30}
                ]
              }
            }
            """.trimIndent()

            val response = makePostRequest(url, eventJson, accessToken)
            response.contains("id")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}