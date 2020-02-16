package com.project.chattie.ui.contacts

import androidx.lifecycle.*
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.data.UserDataSource
import com.project.chattie.ext.emitFailure
import com.project.chattie.ext.emitLoading
import com.project.chattie.ext.emitSuccess
import kotlinx.coroutines.Dispatchers

class ContactsViewModel(private val repository: UserDataSource) : ViewModel() {

    private val _fetchContacts = MutableLiveData<Boolean>()

    init {
        fetchContacts()
    }

    val contacts = _fetchContacts.switchMap {
        liveData<Outcome<List<User>>>(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitLoading()
            try {
                emitSuccess(repository.getContacts())
            } catch (ex: Exception) {
                ex.printStackTrace()
                emitFailure(ex)
            }
        }
    }


    fun fetchContacts() {
        _fetchContacts.value = true
    }

}