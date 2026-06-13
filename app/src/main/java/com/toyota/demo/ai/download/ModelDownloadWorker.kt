package com.toyota.demo.ai.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.toyota.demo.ai.model.ModelDownloadManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ModelDownloadWorkerEntryPoint {
        fun modelDownloadManager(): ModelDownloadManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ModelDownloadWorkerEntryPoint::class.java
        )
        val manager = entryPoint.modelDownloadManager()

        return try {
            manager.downloadModel().fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
