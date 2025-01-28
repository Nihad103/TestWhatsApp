package com.example.testwhatsapp.model

data class Message(
    val messageId: String = "",
    val content: String = "",
    val sender: String = "",
    val receiver: String = "",
    val timestamp: Long = 0L,
    val messageType: String = "text",
    val mediaContent: String? = null
)
