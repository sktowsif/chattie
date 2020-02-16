package com.project.chattie.ui.message

import com.google.firebase.database.FirebaseDatabase

interface MessageDataSource {

}

class MessageRepository(private val database: FirebaseDatabase) : MessageDataSource