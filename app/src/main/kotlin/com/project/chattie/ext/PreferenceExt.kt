package com.project.chattie.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

abstract class Preference<T>(private val context: Context, val name: String, val key: String, val default: T) {

    private val prefs: SharedPreferences by lazy { context.getSharedPreferences(name, Context.MODE_PRIVATE) }

    /**
     * The callback which is called after the change of the property is made. The value of the property
     * has already been changed when this callback is invoked.
     */
    protected open fun afterChange(property: KProperty<*>, newValue: T): Unit {}

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        findPreference(prefs, key, default)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(prefs, key, value)
        afterChange(property, value)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> findPreference(prefs: SharedPreferences, key: String, default: T): T = with(prefs) {
    val res: Any? = when (default) {
        is Long -> getLong(key, default)
        is String -> getString(key, default)
        is Int -> getInt(key, default)
        is Boolean -> getBoolean(key, default)
        is Float -> getFloat(key, default)
        is Double -> {
            val bits = getLong(key, default.toRawBits())
            Double.fromBits(bits)
        }
        else -> throw IllegalArgumentException("This type can be saved into Preferences")
    }
    res as T
}

@SuppressLint("CommitPrefEdits")
fun <T> putPreference(prefs: SharedPreferences, key: String, value: T) = with(prefs.edit()) {
    when (value) {
        is Long -> putLong(key, value)
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Boolean -> putBoolean(key, value)
        is Float -> putFloat(key, value)
        is Double -> putLong(key, value.toRawBits())
        else -> throw IllegalArgumentException("Type $value can't be saved into Preferences")
    }.apply()
}