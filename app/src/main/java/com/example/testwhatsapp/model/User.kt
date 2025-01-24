package com.example.testwhatsapp.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val chats: Map<String, Chat>? = null,
    var lastMessageTimestamp: Long = 0L
)

data class Chat(
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = 0L
)

