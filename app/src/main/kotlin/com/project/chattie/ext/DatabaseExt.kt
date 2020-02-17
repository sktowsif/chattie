package com.project.chattie.ext

import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.FirebaseDatabaseConfig

inline fun FirebaseDatabase.users() = reference.child(FirebaseDatabaseConfig.COLLECTION_USERS)

inline fun FirebaseDatabase.users(uid: String) =
    reference.child(FirebaseDatabaseConfig.COLLECTION_USERS).child(uid)

inline fun FirebaseDatabase.chats() =
    reference.child(FirebaseDatabaseConfig.COLLECTION_CHATS)

inline fun FirebaseDatabase.chats(id: String) =
    reference.child(FirebaseDatabaseConfig.COLLECTION_CHATS).child(id)

inline fun FirebaseDatabase.members() =
    reference.child(FirebaseDatabaseConfig.COLLECTION_MEMBERS)

inline fun FirebaseDatabase.members(chatId: String) =
    reference.child(FirebaseDatabaseConfig.COLLECTION_MEMBERS).child(chatId)

inline fun FirebaseDatabase.messages() =
    reference.child(FirebaseDatabaseConfig.COLLECTION_MESSAGES)

inline fun FirebaseDatabase.messages(chatId: String) =
    reference.child(FirebaseDatabaseConfig.COLLECTION_MESSAGES).child(chatId)