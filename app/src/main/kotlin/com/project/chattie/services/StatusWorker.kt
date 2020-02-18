package com.project.chattie.services

import android.content.Context
import androidx.work.*
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.User
import com.project.chattie.ext.users
import com.project.chattie.ext.workManager
import com.project.chattie.ui.login.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get
import java.util.*
import java.util.concurrent.TimeUnit

class StatusWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    companion object {
        private const val EXTRA_ACTIVE_STATUS = "is_active"

        fun enqueue(context: Context, isActive: Boolean) {

            val request = OneTimeWorkRequestBuilder<StatusWorker>()
                .setInputData(workDataOf(EXTRA_ACTIVE_STATUS to isActive))
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .build()

            context.workManager.enqueue(request)
        }
    }

    override suspend fun doWork() = coroutineScope {
        val status = inputData.getBoolean(EXTRA_ACTIVE_STATUS, false)
        val uid = SessionManager.getUserUid(applicationContext)

        if (uid.isEmpty()) Result.failure()

        try {
            // We will retry maximum of 3 times
            if (runAttemptCount > 3) Result.failure()

            // Get the database reference
            val database = get<FirebaseDatabase>()
            val userRef = database.users()

            withContext(Dispatchers.IO) {
                val updates = if (status) mapOf(User.IS_ACTIVE to status)
                else mapOf(User.IS_ACTIVE to status, User.LAST_SEEN to Date().time)
                userRef.child(uid).updateChildren(updates).await()
            }

            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.retry()
        }
    }


}