package com.example.testwhatsapp.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = 0L
)
