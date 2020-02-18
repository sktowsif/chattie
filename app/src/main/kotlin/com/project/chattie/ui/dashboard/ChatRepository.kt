package com.project.chattie.ui.dashboard

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.Action
import com.project.chattie.data.Chat
import com.project.chattie.data.Member
import com.project.chattie.data.User
import com.project.chattie.ext.chats
import com.project.chattie.ext.members
import com.project.chattie.ext.readValue
import com.project.chattie.ext.users
import com.project.chattie.ui.login.SessionManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

interface ChatDataSource {

    suspend fun getChats(): List<Chat>

    fun attachChatNodeListener()

    fun removeChatNodeListener()

    fun chatChangeChannel(): LiveData<Pair<Action, Chat>>
}

class ChatRepository(
    private val application: Application,
    private val database: FirebaseDatabase
) : ChatDataSource, AnkoLogger {

    private var isListenerActive = false

    private val uid = SessionManager.getUserUid(application)

    private val chatNode = MutableLiveData<Pair<Action, Chat>>()

    private val chatListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {
            warn { error.toException().printStackTrace() }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val updatedChat = snapshot.getValue(Chat::class.java)
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val newChat = snapshot.getValue(Chat::class.java)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val removedChat = snapshot.getValue(Chat::class.java)
        }
    }

    override fun chatChangeChannel(): LiveData<Pair<Action, Chat>> = chatNode

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

    override fun attachChatNodeListener() {
        if (!isListenerActive) {
            database.chats().addChildEventListener(chatListener)
            isListenerActive = true
        }
    }

    override fun removeChatNodeListener() {
        if (isListenerActive) {
            database.chats().removeEventListener(chatListener)
            isListenerActive = false
        }
    }

}