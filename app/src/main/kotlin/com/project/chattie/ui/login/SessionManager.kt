package com.project.chattie.ui.login

import android.content.Context
import com.project.chattie.data.User
import com.project.chattie.ext.clear
import com.project.chattie.ext.getPrefs
import com.project.chattie.ext.putPrefs

object SessionManager {

    private const val PREFERENCE = "chattie:prefs"

    private const val PREF_USER_UID = "prefKeyUserUid"
    private const val PREF_IS_LOGGED_IN = "prefKeyIsLoggedIn"

    fun setLoggedUser(context: Context, user: User) {
        context.putPrefs(PREFERENCE, PREF_USER_UID, user.uid)
        context.putPrefs(PREFERENCE, PREF_IS_LOGGED_IN, true)
    }

    fun isLoggedIn(context: Context) = context.getPrefs(PREFERENCE, PREF_IS_LOGGED_IN, false)

    fun clear(context: Context) = context.clear(PREFERENCE)

}