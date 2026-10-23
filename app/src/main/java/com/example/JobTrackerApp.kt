package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.example.util.InterviewNotificationHelper
import com.example.util.LanguageManager
import com.example.util.LocalAutoBackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JobTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        LanguageManager.init(this)
        InterviewNotificationHelper.createNotificationChannel(this)

        // Automatically recover previous applications if database is empty on fresh install
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LocalAutoBackupManager.autoRestoreIfEmpty(
                    context = this@JobTrackerApp,
                    repository = container.jobRepository
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
