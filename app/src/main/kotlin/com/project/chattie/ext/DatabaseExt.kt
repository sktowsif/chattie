package com.project.chattie.ext

import com.google.firebase.database.FirebaseDatabase
import com.project.chattie.data.FirebaseDatabaseConfig

inline fun FirebaseDatabase.users() = reference.child(FirebaseDatabaseConfig.COLLECTION_USERS)