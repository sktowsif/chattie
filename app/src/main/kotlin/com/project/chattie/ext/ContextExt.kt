package com.project.chattie.ext

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

inline fun <T> Context.getPrefs(@NonNull name: String, @NonNull key: String, default: T) =
    findPreference(getSharedPreferences(name, Context.MODE_PRIVATE), key, default)

inline fun <T> Context.putPrefs(@NonNull name: String, @NonNull key: String, value: T) =
    putPreference(getSharedPreferences(name, Context.MODE_PRIVATE), key, value)

inline fun Context.clear(prefName: String) =
    getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()


inline fun <T : Fragment> FragmentActivity.addFragment(
    @IdRes containerId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    args: Bundle? = null,
    backStack: Boolean = false
) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(containerId, fragmentClass, args, tag)
    if (backStack) transaction.addToBackStack(null)
    transaction.commit()
}

inline fun <T : Fragment> FragmentActivity.replaceFragment(
    @IdRes containerId: Int,
    fragmentClass: Class<T>,
    tag: String? = null,
    args: Bundle? = null,
    backStack: Boolean = false
) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(containerId, fragmentClass, args, tag)
    if (backStack) transaction.addToBackStack(null)
    transaction.commit()
}