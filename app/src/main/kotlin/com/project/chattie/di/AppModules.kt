package com.project.chattie.di

import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.ui.login.LoginActivity
import com.project.chattie.ui.login.LoginFragment
import com.project.chattie.ui.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseDatabase.getInstance() }
}

val uiModule = module {
    viewModel { LoginViewModel(get()) }
    scope<LoginActivity> {
        scoped { LoginFragment() }
    }
}