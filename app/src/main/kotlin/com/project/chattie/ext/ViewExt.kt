package com.project.chattie.ext

import android.view.View

inline fun View.show() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

inline fun View.hide() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

inline fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}