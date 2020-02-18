package com.project.chattie.ui.dashboard

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.Chat
import com.project.chattie.data.Member
import com.project.chattie.data.User
import com.project.chattie.ext.chats
import com.project.chattie.ext.members
import com.project.chattie.ext.readValue
import com.project.chattie.ext.users
import com.project.chattie.ui.login.SessionManager

interface ChatDataSource {
    suspend fun getChats(): List<Chat>
}

class ChatRepository(
    private val application: Application,
    private val database: FirebaseDatabase
) : ChatDataSource {

    private val uid = SessionManager.getUserUid(application)

    override suspend fun getChats(): List<Chat> {
        val uid = SessionManager.getUserUid(application)
        val user = database.users(uid).readValue<User>()
        return user.chats.map { it.key }.map { getChatDetail(it) }
    }

    private suspend fun getChatDetail(chatId: String): Chat {
        val chat = database.chats(chatId).readValue<Chat>()
        val members = database.members(chatId).readValue<Member>()
        // We set the name and image of the contact who is not the current user
        val contactId = members.members.keys.first { it != uid }
        val contact = database.users(contactId).readValue<User>()
        chat.title = contact.name
        chat.imageUrl = contact.imageUrl
        chat.uid = contactId
        return chat
    }

}