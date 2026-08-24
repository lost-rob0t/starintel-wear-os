package actor.starintel.wear

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class StarIntelWearApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val request =
            PeriodicWorkRequestBuilder<StatsRefreshWorker>(15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "starintel.stats.refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
