package com.example.testwhatsapp.model

data class Message(
    var id: String = "",
    val content: String = "",
    val sender: String = "",
    var timestamp: Long = 0,
)
