package com.project.chattie.base

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    abstract fun isProcessing(isLoading: Boolean)

    abstract fun handleError(ex: Throwable)

}