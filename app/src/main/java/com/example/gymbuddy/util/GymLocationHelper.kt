package com.csci3310.gymbuddy.util

import android.location.Location
import com.csci3310.gymbuddy.domain.model.GymChain
import com.csci3310.gymbuddy.domain.model.GymLocation
import com.csci3310.gymbuddy.service.GeocodingService
import com.csci3310.gymbuddy.service.LatLng
import kotlinx.coroutines.flow.first
import javax.inject.Inject

object GymLocationHelper {

    private val gymLocations = listOf(
        GymLocation("247 - Sha Tin", GymChain.GYM247, "Sha Tin, Hong Kong", 22.3765, 114.1870),
        GymLocation("247 - Tai Wai", GymChain.GYM247, "Tai Wai, Hong Kong", 22.3610, 114.2100),

        GymLocation("Anytime - Sha Tin", GymChain.ANYTIME, "Sha Tin, Hong Kong", 22.3790, 114.1890),
        GymLocation("Anytime - Ma On Shan", GymChain.ANYTIME, "Ma On Shan, Hong Kong", 22.3940, 114.2150),

        GymLocation("Snap Fitness - Sha Tin", GymChain.SNAP_FITNESS, "Sha Tin, Hong Kong", 22.3740, 114.1850),
        GymLocation("Snap Fitness - Ma On Shan", GymChain.SNAP_FITNESS, "Ma On Shan, Hong Kong", 22.3960, 114.2130),

        GymLocation("EFX - Sha Tin", GymChain.EFX_FITNESS, "Sha Tin, Hong Kong", 22.3810, 114.1910),
        GymLocation("EFX - Ma On Shan", GymChain.EFX_FITNESS, "Ma On Shan, Hong Kong", 22.3920, 114.2100)
    )

    private var geocodingService: GeocodingService? = null

    fun inject(geocodingService: GeocodingService) {
        this.geocodingService = geocodingService
    }

    fun getLocationsByChain(chain: GymChain): List<GymLocation> {
        return gymLocations.filter { it.chain == chain }
    }

    fun getHardcodedCoordinates(address: String): LatLng? {
        val location = gymLocations.find { it.address == address }
        return if (location?.latitude != null && location.longitude != null) {
            LatLng(location.latitude, location.longitude)
        } else null
    }

    suspend fun findNearestGym(userLat: Double, userLng: Double, chain: GymChain): GymLocation? {
        val chainLocations = getLocationsByChain(chain)
        return sortGymsByDistance(userLat, userLng, chainLocations).firstOrNull()
    }

    suspend fun sortGymsByDistance(
        userLat: Double,
        userLng: Double,
        locations: List<GymLocation>
    ): List<GymLocation> {
        if (locations.isEmpty()) return emptyList()

        val userLocation = Location("user").apply {
            latitude = userLat
            longitude = userLng
        }

        val geocodingService = this.geocodingService
        val locationsWithCoords = locations.map { location ->
            var latLng: LatLng? = null
            if (location.latitude != null && location.longitude != null) {
                latLng = LatLng(location.latitude, location.longitude)
            } else if (geocodingService != null) {
                val geocodeResult = geocodingService.geocodeAddress(location.address).first()
                geocodeResult.getOrNull()?.let { latLng = it }
            }
            location to latLng
        }.filter { it.second != null }

        return locationsWithCoords
            .sortedBy { (_, latLng) ->
                val gymLocation = Location("gym").apply {
                    latitude = latLng!!.latitude
                    longitude = latLng!!.longitude
                }
                userLocation.distanceTo(gymLocation)
            }
            .map { it.first }
    }

    fun getAllChains(): List<GymChain> = GymChain.entries
}