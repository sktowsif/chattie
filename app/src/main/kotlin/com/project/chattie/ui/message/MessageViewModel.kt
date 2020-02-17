package com.project.chattie.ui.message

import android.app.Application
import androidx.lifecycle.*
import com.project.chattie.data.Message
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

    private val _userIds = MutableLiveData<Pair<String, String>>()

    private var _receiverName = ""
    private val _receiverId = MutableLiveData<String>()

    val receiverContactDetail = _receiverId.switchMap {
        liveData<Outcome<User>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                val result = userRepo.getUser(it)
                _receiverName = result.name ?: ""
                emitSuccess(result)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }

    private val _chatId = _userIds.switchMap {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(messageRepo.findConversationId(it.first, it.second))
        }
    }

    private val _messageHistory = _chatId.switchMap {
        liveData<Outcome<List<Message>>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                // Add listener of observe any changes in message node,
                // so that we can easily update the UI with new message.
                messageRepo.attachMessageListener()
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }

    fun getMessageHistory() = _messageHistory

    val newMessage = messageRepo.getMessage()

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
                    messageRepo.sendMessage(_receiverName, _receiverId.value!!, message)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Fetch contact details based on uid.
     */
    fun fetchContact(uid: String) {
        _receiverId.value = uid
    }

    /**
     * Find the chat id between the sender or receiver if exists.
     */
    fun findConversation(receiverId: String) {
        _userIds.value = Pair(_senderId, receiverId)
    }

    override fun onCleared() {
        super.onCleared()
        messageRepo.removeMessageListener()
    }


}