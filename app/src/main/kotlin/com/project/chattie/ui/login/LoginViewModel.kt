package com.project.chattie.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers

class LoginViewModel(private val repository: UserDataSource) : ViewModel() {

    val allUsers =
        liveData<Outcome<List<User>>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                val users = repository.getAllUsers()
                // Check if the database is empty
                if (users.isEmpty()) {
                    repository.initializeUsers()
                    emitSuccess(repository.getAllUsers())
                } else emitSuccess(users)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }

}