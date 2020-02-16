package com.project.chattie.data

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

enum class Role {

    ADMIN, USER
}

@IgnoreExtraProperties
data class User(
    var uid: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var role: String? = Role.USER.name,
    var lastSeen: Date? = null,
    var active: Boolean = false
)