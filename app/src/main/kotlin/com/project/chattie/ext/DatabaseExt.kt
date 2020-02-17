package com.project.chattie.ext

import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.FirebaseDatabaseConfig

inline fun FirebaseDatabase.users() = reference.child(FirebaseDatabaseConfig.COLLECTION_USERS)

inline fun FirebaseDatabase.messages(childId: String) =
    reference.child(FirebaseDatabaseConfig.COLLECTION_MESSAGES).child(childId)