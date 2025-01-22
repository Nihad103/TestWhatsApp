package com.example.testwhatsapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.testwhatsapp.model.User
import com.example.testwhatsapp.repository.ChatListRepository

class ChatListViewModel(private val repository: ChatListRepository) : ViewModel() {

    fun fetchUsers(): LiveData<List<User>> {
        return repository.fetchUsers()
    }
}
