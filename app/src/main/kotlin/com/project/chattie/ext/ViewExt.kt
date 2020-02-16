package com.project.chattie.ext

import android.text.SpannedString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes

inline fun View.show() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

inline fun View.hide() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

inline fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}

inline fun <T : View> ViewGroup.inflate(@LayoutRes layoutResId: Int) =
    LayoutInflater.from(context).inflate(layoutResId, this, false) as T

inline fun TextView.setSpannedText(text: SpannedString) {
    setText(text, TextView.BufferType.SPANNABLE)
}
