package com.example.testwhatsapp.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
)

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList()
)

data class ChatList(
    val chatId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val receiverName: String = ""
)