package com.example.testwhatsapp.model

data class Message(
    val id: String = "",
    val content: String = "",
    val sender: String = "",
    val receiver: String = "",
    val timestamp: Long = 0,
)
