package com.project.chattie.ui.message

import android.app.Application
import androidx.lifecycle.*
import com.project.chattie.data.Message
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.ext.*
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
                messageRepo.attachMessageListener(receiverId)
            }
        }
    }

    fun attachMessageNodeListener(chatId: String, receiverId: String) {
        messageRepo.setConversationId(chatId)
        messageRepo.attachMessageListener(receiverId)
    }

    private val _chatId = MutableLiveData<String>()

    val messages = _chatId.switchMap {
        liveData<Outcome<Pair<List<User>, List<Message>>>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                emitSuccess(messageRepo.getMessageDetail(it))
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }

    fun fetchMessages(chatId: String) {
        _chatId.value = chatId
    }

    private val _updatedMessage = MutableLiveData<Outcome<Pair<String, String>>>()

    fun onMessageUpdate() = _updatedMessage

    fun onEditMessage(chatId: String, messageId: String, newMessage: String) {
        viewModelScope.launch {
            _updatedMessage.loading()
            try {
                val result = withContext(Dispatchers.IO) {
                    messageRepo.onEditMessage(chatId, messageId, newMessage)
                }
                _updatedMessage.success(Pair(messageId, result))
            } catch (ex: Exception) {
                ex.printStackTrace()
                _updatedMessage.failure(ex)
            }
        }
    }

    val statusChange = messageRepo.statusChangeChannel()

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
        messageRepo.removeMessageListener(_receiverId.value!!)
    }


}