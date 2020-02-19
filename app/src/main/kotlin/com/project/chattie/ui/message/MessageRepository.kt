package com.project.chattie.ui.message

import android.app.Application
import android.graphics.Paint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.project.chattie.data.Action
import com.project.chattie.data.Member
import com.project.chattie.data.Message
import com.project.chattie.data.User
import com.project.chattie.ext.*
import com.project.chattie.ui.login.SessionManager
import com.project.chattie.util.DateTimeManager
import kotlinx.coroutines.tasks.await
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

interface MessageDataSource {

    suspend fun findConversationId(senderId: String, receiverId: String)

    suspend fun sendMessage(receiverName: String, receiverId: String, message: Message)

    suspend fun getMessageDetail(chatId: String): Pair<List<User>, List<Message>>

    suspend fun onEditMessage(chatId: String, messageId: String, newMessage: String): String

    fun attachMessageListener(uid: String)

    fun removeMessageListener(uid: String)

    fun setConversationId(chatId: String)

    fun messageChannel(): LiveData<Pair<Action, Any>>

    fun statusChangeChannel(): LiveData<Triple<String, Boolean, Long>>


}

class MessageRepository(
    private val application: Application,
    private val database: FirebaseDatabase
) : MessageDataSource, AnkoLogger {

    private lateinit var chatId: String
    private var isListenerAttached = false

    private val messageNode = MutableLiveData<Pair<Action, Any>>()

    private val statusChangeNode = MutableLiveData<Triple<String, Boolean, Long>>()

    private val messageChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildAdded:${snapshot.key}" }
            val senderId = SessionManager.getUserUid(application)
            val newMessage = snapshot.getValue(Message::class.java)!!
            if (newMessage.senderId == senderId) newMessage.align = Paint.Align.RIGHT
            else newMessage.align = Paint.Align.LEFT
            newMessage.strDateTime = DateTimeManager.formatTime(newMessage.timestamp)
            messageNode.value = Action.ADD to newMessage
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildChanged: ${snapshot.key}" }
            //val newMessage = snapshot.getValue(Message::class.java)
            //messageNode.value = Message.Action.CHANGE to newMessage!!
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildMoved:" + snapshot.key }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            info { "onChildRemoved:" + snapshot.key }
        }

        override fun onCancelled(error: DatabaseError) {
            info { "sessions:onCancelled" }
            warn { error.toException().printStackTrace() }
        }
    }

    private val statusChangeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java)!!
            statusChangeNode.value = Triple(user.name ?: "", user.active, user.lastSeen)
        }

        override fun onCancelled(error: DatabaseError) {
            warn { error.toException().printStackTrace() }
        }
    }

    override fun statusChangeChannel(): LiveData<Triple<String, Boolean, Long>> = statusChangeNode

    override fun messageChannel(): LiveData<Pair<Action, Any>> = messageNode

    override fun setConversationId(chatId: String) {
        this.chatId = chatId
    }

    override suspend fun findConversationId(senderId: String, receiverId: String) {
        val queryParam = createQueryParam(senderId, receiverId)
        // Get all entries from members table
        val response = database.members().readList<Member>()
        // Create a list which will identify if chats exists for the particular ids
        val combinations = response.toList().map {
            it.id to createQueryParam(
                it.members.keys.toList()[0],
                it.members.keys.toList()[1]
            )
        }
        val result = combinations.firstOrNull { it.second == queryParam }

        val chatId = if (!result?.first.isNullOrEmpty()) result!!.first!!
        else database.members().push().key!!
        setConversationId(chatId)
    }

    // Create a generic id based on sender and receiver id
    private fun createQueryParam(senderId: String, receiverId: String) =
        if (senderId > receiverId) senderId + receiverId else receiverId + senderId

    override fun attachMessageListener(uid: String) {
        // Taking extra care of adding listener only once
        warn { "Trying to attach listener..." }
        if (!isListenerAttached) {
            info { "Message node listener attached successfully" }
            // Attach listener to look for any change in message node
            database.messages(chatId).addChildEventListener(messageChildEventListener)
            // Attach listener to connected user to look for status change
            database.users(uid).addValueEventListener(statusChangeListener)
            isListenerAttached = true
        }
    }

    override fun removeMessageListener(uid: String) {
        warn { "Trying to remove listener..." }
        if (isListenerAttached) {
            info { "Message node listener removed successfully" }
            // Clean channel data
            messageNode.value = null
            statusChangeNode.value = null
            // Remove message node listener
            database.messages(chatId).removeEventListener(messageChildEventListener)
            database.users(uid).removeEventListener(statusChangeListener)

            isListenerAttached = false
        }
    }

    override suspend fun sendMessage(rName: String, receiverId: String, message: Message) {
        val senderId = SessionManager.getUserUid(application)
        //val sName = SessionManager.getUsername(application)
        message.id = database.messages().push().key!!

        // Update the member node about the user involved in this chat node
        val member = memberEntity {
            id = chatId
            members = mutableMapOf(senderId to true, receiverId to true)
        }
        database.members(chatId).setValue(member).await()

        // Insert message to message node, this will trigger
        // listener which in turn update the UI
        database.messages(chatId).child(message.id!!).setValue(message).await()

        // Update the user node with chat id for later reference
        database.users(senderId).child(User.CHATS)
            .updateChildren(mapOf(chatId to true)).await()
        database.users(receiverId).child(User.CHATS)
            .updateChildren(mapOf(chatId to true)).await()

        // Update the chat node with last message
        val chat = chatEntity {
            id = chatId
            lastMessage = message.message
            timestamp = message.timestamp
        }
        database.chats(chatId).setValue(chat).await()
    }

    override suspend fun getMessageDetail(chatId: String): Pair<List<User>, List<Message>> {
        val messages = database.messages(chatId).readList<Message>()
        val members = database.members(chatId).readValue<Member>()
        val users = members.members.map { database.users(it.key).readValue<User>() }
        messages.forEach {
            it.strDateTime = DateTimeManager.formatTime(it.timestamp)
            if (it.name == users.first().name) it.align = Paint.Align.LEFT
            else it.align = Paint.Align.RIGHT
        }
        return Pair(users, messages.sortedBy { it.timestamp })
    }

    override suspend fun onEditMessage(
        chatId: String,
        messageId: String,
        newMessage: String
    ): String {
        database.messages(chatId).child(messageId)
            .updateChildren(mapOf(Message.MESSAGE to newMessage))
            .await()
        return newMessage
    }

}