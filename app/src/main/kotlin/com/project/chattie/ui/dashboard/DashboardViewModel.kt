package com.project.chattie.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.project.chattie.data.Chat
import com.project.chattie.data.Outcome
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers

class DashboardViewModel(private val chatRepo: ChatDataSource) : ViewModel() {

//    val chats =
//        liveData<Outcome<List<Chat>>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
//            emitLoading()
//            try {
//                emitSuccess(chatRepo.getChats())
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                emitFailure(ex)
//            }
//        }

    init {
        chatRepo.attachChatNodeListener()
    }

    override fun onCleared() {
        super.onCleared()
        chatRepo.removeChatNodeListener()
    }

}