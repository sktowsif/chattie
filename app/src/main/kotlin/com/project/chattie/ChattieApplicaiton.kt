package com.project.chattie

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.project.chattie.di.dataSourceModule
import com.project.chattie.di.firebaseModule
import com.project.chattie.di.uiModule
import com.project.chattie.ext.workManager
import com.project.chattie.services.StatusWorker
import com.project.chattie.ui.login.SessionManager
import org.jetbrains.anko.toast
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin

class ChattieApplicaiton : Application(), Application.ActivityLifecycleCallbacks {

    private var totalActiveActivities = 0

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ChattieApplicaiton)
            fragmentFactory()
            modules(firebaseModule, dataSourceModule, uiModule)
        }

        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (totalActiveActivities == 0) {
            toast("App in foreground")
            if (SessionManager.isLoggedIn(this))
                StatusWorker.enqueue(this, true)
        }
        totalActiveActivities++
    }

    override fun onActivityStopped(activity: Activity) {
        totalActiveActivities--
        if (totalActiveActivities == 0) {
            toast("App in background")
            if (SessionManager.isLoggedIn(this))
                StatusWorker.enqueue(this, false)
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
}