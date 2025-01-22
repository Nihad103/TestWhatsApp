package com.example.testwhatsapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.testwhatsapp.model.Message
import com.example.testwhatsapp.repository.MessageRepository

class ChatViewModel(private val repository: MessageRepository) : ViewModel() {

    fun fetchMessages(chatId: String): LiveData<List<Message>> {
        return repository.fetchMessages(chatId)
    }

    fun sendMessage(chatId: String, message: Message, receiverId: String) {
        repository.sendMessage(chatId, message, receiverId)
    }
}
