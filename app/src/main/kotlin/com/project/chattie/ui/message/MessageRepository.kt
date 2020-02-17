package com.project.chattie.ui.message

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.Message
import com.project.chattie.ext.messages
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

interface MessageDataSource {

    fun attachListener()

    fun removeListener()

    fun sendMessage(message: Message)

}

class MessageRepository(private val chatId: String, private val database: FirebaseDatabase) :
    MessageDataSource, AnkoLogger {

    private val messageRef by lazy { database.messages(chatId) }

    private val messageChildEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildAdded:${snapshot.key}" }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            info { "onChildChanged: ${snapshot.key}" }
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

    override fun attachListener() {
        messageRef.addChildEventListener(messageChildEventListener)
    }

    override fun sendMessage(message: Message) {

    }

    override fun removeListener() {
        messageRef.removeEventListener(messageChildEventListener)
    }


}