package com.project.chattie.ui.message

import androidx.lifecycle.*
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.data.UserRepository
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers

class MessageViewModel(
    private val userRepo: UserDataSource,
    private val messageRepo: MessageDataSource
) : ViewModel() {

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

}