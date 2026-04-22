package com.example.gymbuddy.domain.model

data class GymLocation(
    val name: String,
    val chain: GymChain,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)

enum class GymChain(val displayName: String) {
    GYM247("247"),
    ANYTIME("Anytime"),
    SNAP_FITNESS("Snap Fitness"),
    EFX_FITNESS("EFX Fitness")
}