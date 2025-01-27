package com.example.testwhatsapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testwhatsapp.model.ChatList
import com.example.testwhatsapp.repository.ChatListRepository

class ChatListViewModel(private val repository: ChatListRepository) : ViewModel() {

    private val _chatList = MutableLiveData<List<ChatList>>()
    val chatList: LiveData<List<ChatList>> get() = _chatList

    fun loadChatList(userId: String) {
        repository.loadChatList(userId, { list ->
            _chatList.value = list
        }, { error ->
            Log.e("ChatListViewModel", "Error loading chat list: ${error.message}")
        })
    }
}

