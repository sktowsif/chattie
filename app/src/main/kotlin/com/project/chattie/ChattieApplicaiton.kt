package com.project.chattie

import android.app.Application
import com.project.chattie.di.firebaseModule
import com.project.chattie.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin

class ChattieApplicaiton : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ChattieApplicaiton)
            fragmentFactory()
            modules(firebaseModule, uiModule)
        }
    }
}