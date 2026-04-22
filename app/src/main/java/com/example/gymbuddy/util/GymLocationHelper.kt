package com.example.gymbuddy.util

import android.location.Location
import com.example.gymbuddy.domain.model.GymChain
import com.example.gymbuddy.domain.model.GymLocation
import com.example.gymbuddy.service.GeocodingService
import com.example.gymbuddy.service.LatLng
import kotlinx.coroutines.flow.first
import javax.inject.Inject

object GymLocationHelper {

    private val gymLocations = listOf(
        GymLocation("HK - Central", GymChain.HK, "Central, Hong Kong", 22.2818, 114.1587),
        GymLocation("HK - Causeway Bay", GymChain.HK, "Causeway Bay, Hong Kong", 22.2823, 114.1857),
        GymLocation("HK - Mong Kok", GymChain.HK, "Mong Kok, Hong Kong", 22.3108, 114.1683),
        GymLocation("HK - Tsim Sha Tsui", GymChain.HK, "Tsim Sha Tsui, Hong Kong", 22.2948, 114.1669),
        GymLocation("HK - Sheung Wan", GymChain.HK, "Sheung Wan, Hong Kong", 22.2859, 114.1494),
        GymLocation("HK - Wan Chai", GymChain.HK, "Wan Chai, Hong Kong", 22.2764, 114.1724),
        GymLocation("HK - Quarry Bay", GymChain.HK, "Quarry Bay, Hong Kong", 22.2866, 114.2134),
        GymLocation("HK - North Point", GymChain.HK, "North Point, Hong Kong", 22.2863, 114.2024),

        GymLocation("247 - Mong Kok", GymChain.GYM247, "Mong Kok, Hong Kong", 22.3113, 114.1722),
        GymLocation("247 - Tsim Sha Tsui", GymChain.GYM247, "Tsim Sha Tsui, Hong Kong", 22.2950, 114.1680),
        GymLocation("247 - Causeway Bay", GymChain.GYM247, "Causeway Bay, Hong Kong", 22.2798, 114.1857),
        GymLocation("247 - Central", GymChain.GYM247, "Central, Hong Kong", 22.2819, 114.1569),
        GymLocation("247 - Sham Shui Po", GymChain.GYM247, "Sham Shui Po, Hong Kong", 22.3300, 114.1621),
        GymLocation("247 - Kwun Tong", GymChain.GYM247, "Kwun Tong, Hong Kong", 22.3104, 114.2213),
        GymLocation("247 - Sha Tin", GymChain.GYM247, "Sha Tin, Hong Kong", 22.3875, 114.2031),
        GymLocation("247 - Tuen Mun", GymChain.GYM247, "Tuen Mun, Hong Kong", 22.3908, 113.9720),

        GymLocation("Anytime - Central", GymChain.ANYTIME, "Central, Hong Kong", 22.2823, 114.1581),
        GymLocation("Anytime - Causeway Bay", GymChain.ANYTIME, "Causeway Bay, Hong Kong", 22.2793, 114.1859),
        GymLocation("Anytime - Mong Kok", GymChain.ANYTIME, "Mong Kok, Hong Kong", 22.3108, 114.1718),
        GymLocation("Anytime - Tsim Sha Tsui", GymChain.ANYTIME, "Tsim Sha Tsui, Hong Kong", 22.2956, 114.1688),
        GymLocation("Anytime - Kowloon Tong", GymChain.ANYTIME, "Kowloon Tong, Hong Kong", 22.3277, 114.1909),
        GymLocation("Anytime - Sha Tin", GymChain.ANYTIME, "Sha Tin, Hong Kong", 22.3879, 114.2065),
        GymLocation("Tin Shui Wai", GymChain.ANYTIME, "Tin Shui Wai, Hong Kong", 22.4477, 114.0061),
        GymLocation("Tuen Mun", GymChain.ANYTIME, "Tuen Mun, Hong Kong", 22.3911, 113.9761),

        GymLocation("Snap Fitness - Central", GymChain.SNAP_FITNESS, "Central, Hong Kong", 22.2833, 114.1578),
        GymLocation("Snap Fitness - Wan Chai", GymChain.SNAP_FITNESS, "Wan Chai, Hong Kong", 22.2771, 114.1731),
        GymLocation("Snap Fitness - Causeway Bay", GymChain.SNAP_FITNESS, "Causeway Bay, Hong Kong", 22.2805, 114.1862),
        GymLocation("Snap Fitness - Mong Kok", GymChain.SNAP_FITNESS, "Mong Kok, Hong Kong", 22.3125, 114.1725),
        GymLocation("Snap Fitness - Tsim Sha Tsui", GymChain.SNAP_FITNESS, "Tsim Sha Tsui, Hong Kong", 22.2949, 114.1678),
        GymLocation("Snap Fitness - Jordan", GymChain.SNAP_FITNESS, "Jordan, Hong Kong", 22.3039, 114.1714),
        GymLocation("Snap Fitness - Yau Ma Tei", GymChain.SNAP_FITNESS, "Yau Ma Tei, Hong Kong", 22.3044, 114.1641),
        GymLocation("Snap Fitness - Kowloon Bay", GymChain.SNAP_FITNESS, "Kowloon Bay, Hong Kong", 22.3228, 114.2026),

        GymLocation("EFX - Central", GymChain.EFX_FITNESS, "Central, Hong Kong", 22.2820, 114.1575),
        GymLocation("EFX - Sheung Wan", GymChain.EFX_FITNESS, "Sheung Wan, Hong Kong", 22.2861, 114.1505),
        GymLocation("EFX - Causeway Bay", GymChain.EFX_FITNESS, "Causeway Bay, Hong Kong", 22.2797, 114.1855),
        GymLocation("EFX - Tin Hau", GymChain.EFX_FITNESS, "Tin Hau, Hong Kong", 22.2863, 114.1911),
        GymLocation("EFX - North Point", GymChain.EFX_FITNESS, "North Point, Hong Kong", 22.2859, 114.2014),
        GymLocation("EFX - Quarry Bay", GymChain.EFX_FITNESS, "Quarry Bay, Hong Kong", 22.2870, 114.2141),
        GymLocation("EFX - Tai Koo", GymChain.EFX_FITNESS, "Tai Koo, Hong Kong", 22.2856, 114.2203),
        GymLocation("EFX - Sai Wan Ho", GymChain.EFX_FITNESS, "Sai Wan Ho, Hong Kong", 22.2835, 114.2278)
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
        if (chainLocations.isEmpty()) return null

        val userLocation = Location("user").apply {
            latitude = userLat
            longitude = userLng
        }

        val geocodingService = this.geocodingService
        if (geocodingService != null) {
            val locationsWithCoords = chainLocations.map { location ->
                var latLng: LatLng? = null
                if (location.latitude != null && location.longitude != null) {
                    latLng = LatLng(location.latitude, location.longitude)
                } else {
                    val geocodeResult = geocodingService.geocodeAddress(location.address).first()
                    geocodeResult.getOrNull()?.let { latLng = it }
                }
                location to latLng
            }.filter { it.second != null }

            if (locationsWithCoords.isNotEmpty()) {
                return locationsWithCoords.minByOrNull { (location, latLng) ->
                    val gymLocation = Location("gym").apply {
                        latitude = latLng!!.latitude
                        longitude = latLng!!.longitude
                    }
                    userLocation.distanceTo(gymLocation)
                }?.first
            }
        }

        return chainLocations
            .filter { it.latitude != null && it.longitude != null }
            .minByOrNull { gym ->
                val gymLocation = Location("gym").apply {
                    latitude = gym.latitude!!
                    longitude = gym.longitude!!
                }
                userLocation.distanceTo(gymLocation)
            }
    }

    fun getAllChains(): List<GymChain> = GymChain.entries
}