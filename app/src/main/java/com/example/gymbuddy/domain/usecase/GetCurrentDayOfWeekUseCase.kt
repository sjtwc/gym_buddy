package com.csci3310.gymbuddy.domain.usecase

import java.util.Calendar
import javax.inject.Inject

class GetCurrentDayOfWeekUseCase @Inject constructor() {
    operator fun invoke(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }
}