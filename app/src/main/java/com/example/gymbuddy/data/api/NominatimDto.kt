package com.example.gymbuddy.data.api

import kotlinx.serialization.Serializable

@Serializable
data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String,
    val place_id: Long? = null
)