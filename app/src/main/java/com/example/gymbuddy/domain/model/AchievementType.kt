package com.example.gymbuddy.domain.model

enum class AchievementType(
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val targetValue: Int
) {
    FIRST_WORKOUT("First Steps", "Complete your first workout", "🎯", 50, 1),
    STREAK_7_DAYS("Consistent", "Work out 7 days in a row", "🔥", 200, 7),
    HEAVY_LIFTER("Heavy Lifter", "Lift 10,000kg total volume", "🏋️", 100, 10000),
    EARLY_BIRD("Early Bird", "Complete a workout before 7am", "🌅", 50, 1),
    NIGHT_OWL("Night Owl", "Complete a workout after 9pm", "🌙", 50, 1),
    MARATHONER("Marathoner", "Complete 50 workouts", "🏆", 500, 50),
    STRENGTH_MASTER("Strength Master", "Reach level 10", "💪", 300, 10),
    SOCIAL_BUTTERFLY("Social Butterfly", "Share a workout", "🤝", 50, 1),
    PERFECTIONIST("Perfectionist", "Complete workout without skipping", "✨", 100, 1),
    VARIETY("Variety", "Try 20 different exercises", "🎨", 150, 20),
    BEAST_MODE("Beast Mode", "Complete 100 workouts", "🦍", 1000, 100),
    CHAMPIONSHIP("Championship", "Win a monthly challenge", "🥇", 500, 1),
    WEEK_2_WORKOUTS("Week Warrior 0", "Complete 2 workouts this week", "🏃", 50, 2),
    WEEK_3_WORKOUTS("Week Warrior I", "Complete 3 workouts this week", "💪", 75, 3),
    WEEK_4_WORKOUTS("Week Warrior II", "Complete 4 workouts this week", "🔥", 100, 4),
    WEEK_5_WORKOUTS("Week Warrior III", "Complete 5 workouts this week", "🌟", 150, 5),
    WEEK_6_WORKOUTS("Week Warrior IV", "Complete 6 workouts this week", "👑", 200, 6)
}

enum class UserTitle(val minLevel: Int, val title: String) {
    NOVICE(1, "New Year's Resolution"),
    TRAINEE(5, "Gym Rat"),
    ATHLETE(10, "Iron Pumper"),
    WARRIOR(20, "Beast"),
    CHAMPION(30, "Swolefather"),
    LEGEND(50, "Greek God"),
    MYTHIC(75, "Mountain"),
    GODLIKE(100, "Immortal");

    companion object {
        fun fromLevel(level: Int): UserTitle {
            return entries.sortedByDescending { it.minLevel }
                .firstOrNull { level >= it.minLevel }
                ?: NOVICE
        }
    }
}

object XpConfig {
    const val XP_PER_WORKOUT = 100
    const val XP_PER_LEVEL = 1000
    
    fun xpForLevel(level: Int): Int = level * XP_PER_LEVEL
    
    fun calculateLevel(totalXp: Int): Int {
        var level = 1
        var xpNeeded = XP_PER_LEVEL
        var remainingXp = totalXp
        
        while (remainingXp >= xpNeeded) {
            remainingXp -= xpNeeded
            level++
            xpNeeded = level * XP_PER_LEVEL
        }
        return level
    }
    
    fun xpInCurrentLevel(totalXp: Int): Int {
        var level = 1
        var xpNeeded = XP_PER_LEVEL
        var remainingXp = totalXp
        
        while (remainingXp >= xpNeeded) {
            remainingXp -= xpNeeded
            level++
            xpNeeded = level * XP_PER_LEVEL
        }
        return remainingXp
    }
}