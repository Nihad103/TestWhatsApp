package com.example.testwhatsapp.model

data class Call(
    val callId: String = "",
    val callerId: String = "",
    val receiverId: String = "",
    val channelName: String = "",
    val token: String = "",
    val status: String = "ringing",
    val timestamp: Long = 0L
)

