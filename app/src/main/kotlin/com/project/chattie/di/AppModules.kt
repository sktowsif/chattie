package com.project.chattie.di

import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.UserDataSource
import com.project.chattie.data.UserRepository
import com.project.chattie.ui.contacts.ContactsActivity
import com.project.chattie.ui.contacts.ContactsFragment
import com.project.chattie.ui.contacts.ContactsViewModel
import com.project.chattie.ui.dashboard.DashboardActivity
import com.project.chattie.ui.dashboard.HomeFragment
import com.project.chattie.ui.login.LoginActivity
import com.project.chattie.ui.login.LoginFragment
import com.project.chattie.ui.login.LoginViewModel
import com.project.chattie.ui.message.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseDatabase.getInstance() }
}

val dataSourceModule = module {
    single { UserRepository(get(), get()) } bind UserDataSource::class
    single { MessageRepository(get(), get()) } bind MessageDataSource::class
}

val uiModule = module {

    viewModel { LoginViewModel(get()) }
    scope<LoginActivity> {
        scoped { LoginFragment() }
    }

    scope<DashboardActivity> {
        scoped { HomeFragment() }
    }

    viewModel { ContactsViewModel(get()) }
    scope<ContactsActivity> {
        scoped { ContactsFragment() }
    }

    viewModel { MessageViewModel(get(), get(), get()) }
    scope<MessageActivity> {
        scoped { MessageFragment() }
    }
}