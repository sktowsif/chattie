package com.project.chattie.ui.message

import android.app.Application
import android.graphics.Paint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.Member
import com.project.chattie.data.Message
import com.project.chattie.data.User
import com.project.chattie.ext.*
import com.project.chattie.ui.login.SessionManager
import kotlinx.coroutines.tasks.await
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

interface MessageDataSource {

    /**
     * Find the chat id based on sender and receiver id, or create a
     * new id if does not exists.
     */
    suspend fun findConversationId(senderId: String, receiverId: String): String

    suspend fun fetchMessages(chatId: String): List<Message>

    /**
     * Attach a listener to particular chat id node.
     */
    fun attachMessageListener()

    fun removeMessageListener()

    fun getMessage(): LiveData<Pair<Message.Action, Any>>

    suspend fun sendMessage(receiverName: String, receiverId: String, message: Message)
}

class MessageRepository(
    private val application: Application,
    private val database: FirebaseDatabase
) : MessageDataSource, AnkoLogger {

    private var isListenerAttached = false

    private lateinit var chatId: String

    private val messageNode = MutableLiveData<Pair<Message.Action, Any>>()

    override fun getMessage(): LiveData<Pair<Message.Action, Any>> = messageNode

    private val messageChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildAdded:${snapshot.key}" }
            val senderId = SessionManager.getUserUid(application)
            val newMessage = snapshot.getValue(Message::class.java)!!
            if (newMessage.senderId == senderId) newMessage.align = Paint.Align.RIGHT
            else newMessage.align = Paint.Align.LEFT
            messageNode.value = Message.Action.ADD to newMessage!!
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildChanged: ${snapshot.key}" }
            val newMessage = snapshot.getValue(Message::class.java)
            messageNode.value = Message.Action.CHANGE to newMessage!!
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

    override suspend fun findConversationId(senderId: String, receiverId: String): String {
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

        chatId = if (!result?.first.isNullOrEmpty()) {
            info { "Chat id found: ${result!!.first}" }
            result!!.first!!
        } else {
            val id = database.members().push().key!!
            info { "No existing chat found, creating new id: $id" }
            id
        }

        return chatId

    }

    // Create a generic id based on sender and receiver id
    private fun createQueryParam(senderId: String, receiverId: String) =
        if (senderId > receiverId) senderId + receiverId else receiverId + senderId

    override suspend fun fetchMessages(chatId: String): List<Message> {
        val uid = SessionManager.getUserUid(application)
        val messages = database.messages(chatId).readList<Message>()
        messages.forEach {
            if (it.senderId == uid) it.align = Paint.Align.RIGHT
            else it.align = Paint.Align.LEFT
        }
        return messages
    }


    override fun attachMessageListener() {
        // Taking extra care of adding listener only once
        warn { "Trying to attach listener..." }
        if (!isListenerAttached) {
            debug { "Message node listener attached successfully" }
            database.messages(chatId).addChildEventListener(messageChildEventListener)
            isListenerAttached = true
        }
    }

    override fun removeMessageListener() {
        warn { "Trying to remove listener..." }
        if (isListenerAttached) {
            debug { "Message node listener removed successfully" }
            database.messages(chatId).removeEventListener(messageChildEventListener)
            isListenerAttached = false
        }
    }

    override suspend fun sendMessage(receiverName: String, receiverId: String, message: Message) {
        val senderId = SessionManager.getUserUid(application)
        val senderName = SessionManager.getUsername(application)
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
            title = "$senderName|$receiverName"
            lastMessage = message.message
            timestamp = message.timestamp
        }
        database.chats(chatId).setValue(chat).await()
    }
}