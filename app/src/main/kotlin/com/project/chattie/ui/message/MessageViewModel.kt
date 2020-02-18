package com.project.chattie.ui.message

import android.app.Application
import androidx.lifecycle.*
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import com.project.chattie.ext.messageEntity
import com.project.chattie.ui.login.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MessageViewModel(
    application: Application,
    private val userRepo: UserDataSource,
    private val messageRepo: MessageDataSource
) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _senderName = SessionManager.getUsername(context)
    private val _senderId = SessionManager.getUserUid(context)

    private val _receiverName = MutableLiveData<String>()
    private val _receiverId = MutableLiveData<String>()

    val receiverContactDetail = _receiverId.switchMap {
        liveData<Outcome<User>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                val result = userRepo.getUser(it)
                _receiverName.postValue(result.name ?: "")
                emitSuccess(result)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }

    /**
     * Fetch contact details based on uid.
     */
    fun fetchReceiverContactDetail(uid: String) {
        _receiverId.value = uid
    }

    /**
     * Find the chat id between the sender or receiver if exists.
     */
    fun findConversation(receiverId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Find the conversation id based on sender and receiver id
                messageRepo.findConversationId(_senderId, receiverId)
                // Add listener of observe any changes in message node,
                // so that we can easily update the UI with new message.
                messageRepo.attachMessageListener()
            }
        }
    }

    fun attachMessageNodeListener(chatId: String) {
        messageRepo.setConversationId(chatId)
        messageRepo.attachMessageListener()
    }

    val newMessage = messageRepo.messageChannel()

    fun sendMessage(text: String) {
        val message = messageEntity {
            senderId = _senderId
            name = _senderName
            message = text
            timestamp = Date().time
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    messageRepo.sendMessage(
                        _receiverName.value!!,
                        _receiverId.value!!,
                        message
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageRepo.removeMessageListener()
    }


}