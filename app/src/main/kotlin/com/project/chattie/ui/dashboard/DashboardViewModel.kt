package com.project.chattie.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.project.chattie.data.Action
import com.project.chattie.data.Chat
import com.project.chattie.data.Outcome
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers

class DashboardViewModel(private val chatRepo: ChatDataSource) : ViewModel() {

    init {
        chatRepo.attachChatNodeListener()
    }

    fun newChat() = chatRepo.newChatLiveData.switchMap {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                val result = chatRepo.getChat(it.second.id!!)
                emit(Pair(it.first, result))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepo.removeChatNodeListener()
    }

}