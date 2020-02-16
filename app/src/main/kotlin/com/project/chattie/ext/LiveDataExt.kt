package com.project.chattie.ext

import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MutableLiveData
import com.project.chattie.data.Outcome

suspend inline fun <T> LiveDataScope<Outcome<T>>.emitLoading() {
    emit(Outcome.loading(true))
}

suspend inline fun <T> LiveDataScope<Outcome<T>>.emitSuccess(data: T) {
    emit(Outcome.loading(false))
    emit(Outcome.success(data))
}

suspend inline fun <T> LiveDataScope<Outcome<T>>.emitFailure(ex: Throwable) {
    emit(Outcome.loading(false))
    emit(Outcome.failure(ex))
}

inline fun <T> MutableLiveData<Outcome<T>>.loading() {
    value = Outcome.loading(true)
}

inline fun <T> MutableLiveData<Outcome<T>>.success(data: T) {
    value = Outcome.loading(false)
    value = Outcome.success(data)
}

inline fun <T> MutableLiveData<Outcome<T>>.failure(ex: Throwable) {
    value = Outcome.loading(false)
    value = Outcome.failure(ex)
}