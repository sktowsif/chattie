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
import com.project.chattie.ext.*
import com.project.chattie.ui.login.SessionManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

interface ChatDataSource {

    val newChatLiveData: MutableLiveData<Pair<Action, Chat>>

    suspend fun getChat(id: String): Chat

    suspend fun getChats(): List<Chat>

    fun attachChatNodeListener()

    fun removeChatNodeListener()
}

class ChatRepository(
    application: Application,
    private val database: FirebaseDatabase
) : ChatDataSource, AnkoLogger {

    private var isListenerActive = false

    private val uid = SessionManager.getUserUid(application)
    private val role = SessionManager.getUserRole(application)

    private val chatListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {
            warn { error.toException().printStackTrace() }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val updatedChat = snapshot.getValue(Chat::class.java)!!
            newChatLiveData.value = Pair(Action.CHANGE, updatedChat)
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val newChat = snapshot.getValue(Chat::class.java)!!
            newChatLiveData.value = Pair(Action.ADD, newChat)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val removedChat = snapshot.getValue(Chat::class.java)
        }
    }

    override val newChatLiveData: MutableLiveData<Pair<Action, Chat>> = MutableLiveData()

    override suspend fun getChat(id: String): Chat {
        val chat = database.chats(id).readValue<Chat>()
        val members = database.members(id).readValue<Member>()
        // We set the name and image of the contact who is not the current user
        val contactId = members.members.keys.first { it != uid }
        val contact = database.users(contactId).readValue<User>()
        chat.title = contact.name
        chat.imageUrl = contact.imageUrl
        chat.uid = contactId
        return chat
    }

    override suspend fun getChats(): List<Chat> = database.chats().readList()

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