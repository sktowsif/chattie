package com.project.chattie.data

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

enum class Role {

    ADMIN, USER;
}

@IgnoreExtraProperties
data class User(
    var uid: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var role: String? = Role.USER.name,
    var lastSeen: Long? = null,
    var active: Boolean = false
) {
    companion object {

        const val IS_ACTIVE = "active"
        const val LAST_SEEN = "lastSeen"

    }
}

data class Message(
    var id: String? = "",
    var message: String? = "",
    var timestamp: Long? = Date().time
)