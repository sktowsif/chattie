package com.project.chattie.ext

import com.project.chattie.data.Chat
import com.project.chattie.data.Member
import com.project.chattie.data.Message
import com.project.chattie.data.User

fun userEntity(block: User.() -> Unit): User =
    User().apply(block)

fun messageEntity(block: Message.() -> Unit): Message =
    Message().apply(block)

fun chatEntity(block: Chat.() -> Unit): Chat =
    Chat().apply(block)

fun memberEntity(block: Member.() -> Unit): Member =
    Member().apply(block)