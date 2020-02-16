package com.project.chattie.data

import android.app.Application
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.ext.readList
import com.project.chattie.ext.readValue
import com.project.chattie.ext.userEntity
import com.project.chattie.ext.users
import com.project.chattie.ui.login.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface UserDataSource {

    suspend fun initializeUsers()

    suspend fun getAllUsers(): List<User>

    suspend fun getContacts(): List<User>

    suspend fun getUser(uid: String): User

}

class UserRepository(
    private val application: Application,
    private val database: FirebaseDatabase
) : UserDataSource {

    private val userRef by lazy { database.users() }

    override suspend fun getAllUsers(): List<User> = userRef.readList()

    override suspend fun getContacts(): List<User> = filterBasedOnRole(getAllUsers())

    private fun filterBasedOnRole(users: List<User>): List<User> {
        val uid = SessionManager.getUserUid(application)
        val role = SessionManager.getUserRole(application)
        return users.filter {
            // Filter out admin from the list
            if (role == Role.ADMIN) it.role != Role.ADMIN.name
            // Filter our current user
            else it.uid != uid && it.role != Role.ADMIN.name
        }
    }

    override suspend fun getUser(uid: String): User = userRef.child(uid).readValue()

    /**
     * Since the requirement was to have three hardcoded users as John Doe, Jane Doe and Admin.
     * We create and insert those users here.
     */
    override suspend fun initializeUsers() {
        val userJohn = userEntity {
            uid = userRef.push().key
            name = "John Doe"
            imageUrl = "https://i.picsum.photos/id/1005/5760/3840.jpg"
        }
        val userJane = userEntity {
            uid = userRef.push().key
            name = "Jane Doe"
            imageUrl = "https://i.picsum.photos/id/1011/5472/3648.jpg"
        }
        val admin = userEntity {
            uid = userRef.push().key
            name = "Admin"
            imageUrl = "https://i.picsum.photos/id/1012/3973/2639.jpg"
            role = Role.ADMIN.name
        }

        userRef.child(userJohn.uid!!).setValue(userJohn).await()
        userRef.child(userJane.uid!!).setValue(userJane).await()
        userRef.child(admin.uid!!).setValue(admin).await()
    }
}