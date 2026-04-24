package com.example.gymbuddy.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymbuddy.data.repository.UserSummaryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UserSummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userSummaryGenerator: UserSummaryGenerator,
    private val userSummaryRepository: UserSummaryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val summary = userSummaryGenerator.generateSummary()
            userSummaryRepository.saveUserSummary(summary)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "user_summary_work"
    }
}