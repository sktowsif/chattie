package com.project.chattie.ui.login

import android.content.Context
import com.project.chattie.data.Role
import com.project.chattie.data.User
import com.project.chattie.ext.clear
import com.project.chattie.ext.getPrefs
import com.project.chattie.ext.putPrefs

object SessionManager {

    private const val PREFERENCE = "chattie:prefs"

    private const val PREF_USER_UID = "prefKeyUserUid"
    private const val PREF_IS_LOGGED_IN = "prefKeyIsLoggedIn"
    private const val PREF_USER_ROLE = "prefKeyUserRole"

    fun setLoggedUser(context: Context, user: User) {
        context.putPrefs(PREFERENCE, PREF_USER_UID, user.uid)
        context.putPrefs(PREFERENCE, PREF_USER_ROLE, user.role)
        context.putPrefs(PREFERENCE, PREF_IS_LOGGED_IN, true)
    }

    fun getUserUid(context: Context) = context.getPrefs(PREFERENCE, PREF_USER_UID, "")

    fun getUserRole(context: Context): Role {
        val role = context.getPrefs(PREFERENCE, PREF_USER_ROLE, Role.USER.name)
        return Role.valueOf(role)
    }

    fun isLoggedIn(context: Context) = context.getPrefs(PREFERENCE, PREF_IS_LOGGED_IN, false)

    fun clear(context: Context) = context.clear(PREFERENCE)

}