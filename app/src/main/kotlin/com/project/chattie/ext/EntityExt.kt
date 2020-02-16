package com.project.chattie.ext

import com.project.chattie.data.User

fun userEntity(block: User.() -> Unit): User =
    User().apply(block)