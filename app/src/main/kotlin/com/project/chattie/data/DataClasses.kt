package com.project.chattie.data

import android.graphics.Paint
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*
import kotlin.collections.HashMap

enum class Role {

    ADMIN, USER;
}

@IgnoreExtraProperties
data class User(
    var uid: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var role: String? = Role.USER.name,
    var lastSeen: Long = 0L,
    var active: Boolean = false,
    var chats: MutableMap<String, Boolean> = HashMap()
) {
    companion object {

        const val IS_ACTIVE = "active"
        const val LAST_SEEN = "lastSeen"
        const val CHATS = "chats"

    }
}

@IgnoreExtraProperties
data class Member(
    var id: String? = null,
    var members: MutableMap<String, Boolean> = HashMap()
)

@IgnoreExtraProperties
data class Chat(
    var id: String? = "",
    var lastMessage: String? = "",
    var timestamp: Long = Date().time
) {

    @get:Exclude
    @set:Exclude
    var title: String? = null

    @get:Exclude
    @set:Exclude
    var imageUrl: String? = null

    @get:Exclude
    @set:Exclude
    var uid: String? = null

}

@IgnoreExtraProperties
data class Message(
    var id: String? = "",
    var name: String? = "",
    var message: String? = "",
    var senderId: String? = "",
    var timestamp: Long = Date().time
) {

    companion object {
        const val MESSAGE = "message"
    }

    @set:Exclude
    @get:Exclude
    var align: Paint.Align = Paint.Align.LEFT

    @set:Exclude
    @get:Exclude
    var strDateTime: String? = null
}

enum class Action {
    ADD, DELETE, CHANGE, ERROR
}