package com.csci3310.gymbuddy.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String,
    val place_id: Long? = null
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

@Singleton
class GeocodingService @Inject constructor() {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val geocodeCache = mutableMapOf<String, LatLng>()
    private val reverseGeocodeCache = mutableMapOf<String, String>()

    private val baseUrl = "https://nominatim.openstreetmap.org"

    fun geocodeAddress(address: String): Flow<Result<LatLng>> = flow {
        geocodeCache[address]?.let { cached ->
            emit(Result.success(cached))
            return@flow
        }

        delay(1000)

        try {
            val result: List<NominatimResult> = httpClient.get("$baseUrl/search") {
                parameter("q", address)
                parameter("format", "json")
                parameter("limit", "1")
                header("User-Agent", "GymBuddyApp/1.0")
            }.body()

            if (result.isNotEmpty()) {
                val latLng = LatLng(
                    latitude = result[0].lat.toDouble(),
                    longitude = result[0].lon.toDouble()
                )
                geocodeCache[address] = latLng
                emit(Result.success(latLng))
            } else {
                emit(Result.failure(Exception("Address not found: $address")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun reverseGeocode(lat: Double, lng: Double): Flow<Result<String>> = flow {
        val cacheKey = "$lat,$lng"
        reverseGeocodeCache[cacheKey]?.let { cached ->
            emit(Result.success(cached))
            return@flow
        }

        delay(1000)

        try {
            val result: List<NominatimResult> = httpClient.get("$baseUrl/reverse") {
                parameter("lat", lat)
                parameter("lon", lng)
                parameter("format", "json")
                header("User-Agent", "GymBuddyApp/1.0")
            }.body()

            if (result.isNotEmpty()) {
                val address = result[0].display_name
                reverseGeocodeCache[cacheKey] = address
                emit(Result.success(address))
            } else {
                emit(Result.failure(Exception("Location not found: $lat, $lng")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getCachedCoordinates(address: String): LatLng? = geocodeCache[address]

    fun clearCache() {
        geocodeCache.clear()
        reverseGeocodeCache.clear()
    }
}