package com.example.gymbuddy.domain.model

data class UserProfile(
    val id: Long = 1,
    val name: String = "Trainer",
    val level: Int = 1,
    val xp: Int = 0,
    val title: String = "Novice",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalVolume: Float = 0f,
    val lastWorkoutDate: Long? = null,
    val pet: VirtualPet = VirtualPet(),
    val gender: String? = null,
    val age: Int? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val avatarUri: String? = null
)

data class VirtualPet(
    val name: String = "GymBot",
    val happiness: Int = 50,
    val mood: PetMood = PetMood.NEUTRAL,
    val level: Int = 1,
    val xp: Int = 0
) {
    companion object {
        fun calculateMood(lastWorkoutDate: Long?): PetMood {
            val daysSince = calculateDaysSince(lastWorkoutDate)
            return when (daysSince) {
                0 -> PetMood.EXCITED
                1 -> PetMood.HAPPY
                in 2..3 -> PetMood.NEUTRAL
                in 4..6 -> PetMood.SAD
                in 7..13 -> PetMood.DISAPPOINTED
                else -> PetMood.WAITING
            }
        }

        fun calculateHappiness(lastWorkoutDate: Long?, baseHappiness: Int): Int {
            val daysSince = calculateDaysSince(lastWorkoutDate)
            return when (daysSince) {
                0 -> minOf(100, baseHappiness + 20)
                1 -> (baseHappiness * 0.95).toInt().coerceIn(10, 100)
                2 -> (baseHappiness * 0.85).toInt().coerceIn(10, 100)
                3 -> (baseHappiness * 0.70).toInt().coerceIn(10, 100)
                in 4..6 -> (baseHappiness * 0.50).toInt().coerceIn(10, 100)
                in 7..13 -> (baseHappiness * 0.30).toInt().coerceIn(10, 100)
                else -> (baseHappiness * 0.10).toInt().coerceIn(10, 100)
            }
        }

        private fun calculateDaysSince(lastWorkoutDate: Long?): Int {
            if (lastWorkoutDate == null) return Int.MAX_VALUE
            val diff = System.currentTimeMillis() - lastWorkoutDate
            return (diff / (24 * 60 * 60 * 1000)).toInt()
        }
    }
}

enum class PetMood(val displayName: String, val emoji: String, val message: String) {
    HAPPY("Happy", "😊", "Great workout! Let's keep the momentum going!"),
    EXCITED("Excited", "🎉", "Amazing! New personal record! I'm so proud!"),
    NEUTRAL("Neutral", "😐", "Ready for another workout? Let's go!"),
    SAD("Sad", "😢", "I've been waiting for you... Don't let me down!"),
    DISAPPOINTED("Disappointed", "😔", "It's been a while... I miss our workouts!"),
    WAITING("Waiting", "⏳", "Take your time... but not too long!")
}

data class PersonalRecord(
    val id: Long = 0,
    val exerciseId: Long,
    val type: RecordType,
    val value: Float,
    val reps: Int,
    val weight: Float,
    val date: Long,
    val workoutId: Long? = null
)

enum class RecordType(val displayName: String) {
    ONE_REP_MAX("1RM"),
    VOLUME("Volume"),
    REPS("Reps"),
    WEIGHT("Weight")
}

data class BodyMeasurement(
    val id: Long = 0,
    val date: Long,
    val weight: Float? = null,
    val bodyFat: Float? = null,
    val chest: Float? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val biceps: Float? = null,
    val thighs: Float? = null,
    val calves: Float? = null,
    val shoulders: Float? = null,
    val notes: String? = null
)