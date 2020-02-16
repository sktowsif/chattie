package com.project.chattie.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.Outcome
import com.project.chattie.data.Role
import com.project.chattie.data.User
import com.project.chattie.ext.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginViewModel(private val database: FirebaseDatabase) : ViewModel() {

    private val _users = MutableLiveData<Outcome<List<User>>>()

    init {
        initUser()
    }

    private fun initUser() {
        viewModelScope.launch {
            _users.loading()
            val userRef = database.users()
            // Check if the database is empty
            if (userRef.readList<User>().isEmpty()) initializeUsers(userRef)
            fetchUsers(userRef)
        }
    }

    private suspend fun fetchUsers(userRef: DatabaseReference) {
        try {
            val result = withContext(Dispatchers.IO) { userRef.readList<User>() }
            _users.success(result)
        } catch (ex: Exception) {
            ex.printStackTrace()
            _users.failure(ex)
        }
    }

    fun getUsers() = _users

    /**
     * Since the requirement was to have three hardcoded users as John Doe, Jane Doe and Admin.
     * We create and insert those users here.
     */
    private suspend fun initializeUsers(userRef: DatabaseReference) = withContext(Dispatchers.IO) {
        val userJohn = userEntity {
            uid = userRef.push().key
            name = "John Doe"
        }
        val userJane = userEntity {
            uid = userRef.push().key
            name = "Jane Doe"
        }
        val admin = userEntity {
            uid = userRef.push().key
            name = "Admin"
            role = Role.ADMIN.name
        }

        userRef.child(userJohn.uid!!).setValue(userJohn).await()
        userRef.child(userJane.uid!!).setValue(userJane).await()
        userRef.child(admin.uid!!).setValue(admin).await()
    }

}