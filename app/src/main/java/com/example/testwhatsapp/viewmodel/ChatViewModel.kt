package com.example.testwhatsapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.testwhatsapp.model.Message
import com.example.testwhatsapp.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth

class ChatViewModel(private val repository: MessageRepository) : ViewModel() {

    fun fetchMessages(chatId: String): LiveData<List<Message>> {
        return repository.fetchMessages(chatId)
    }

    fun sendMessage(chatId: String, message: Message, receiverId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val senderId = currentUser.uid
            val messageToSend = message.copy(sender = senderId)
            repository.sendMessage(chatId, messageToSend, receiverId)
            Log.d("ChatViewModel", "currentuser is not null")
        } else {
            Log.e("ChatViewModel", "No current user found for sending message")
        }
    }

}
