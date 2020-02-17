package com.project.chattie.ui.message

import androidx.lifecycle.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.data.UserRepository
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

class MessageViewModel(
    private val userRepo: UserDataSource,
    private val messageRepo: MessageDataSource
) : ViewModel() {

    init {
        messageRepo.attachListener()
    }

    private val _connectedContactUid = MutableLiveData<String>()

    val connectedContact = _connectedContactUid.switchMap {
        liveData<Outcome<User>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                emitSuccess(userRepo.getUser(it))
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }

    fun fetchContact(uid: String) {
        _connectedContactUid.value = uid
    }

    private val _chatId = MutableLiveData<String>()

    fun fetchConversation(chatId: String) {
        _chatId.value = chatId
    }

    override fun onCleared() {
        super.onCleared()
        messageRepo.removeListener()
    }

}