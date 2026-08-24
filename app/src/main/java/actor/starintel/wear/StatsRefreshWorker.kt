package actor.starintel.wear

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import actor.starintel.wear.data.StarIntelRepository

class StatsRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            StarIntelRepository(applicationContext).refreshStats()
            Result.success()
        }.getOrElse {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
