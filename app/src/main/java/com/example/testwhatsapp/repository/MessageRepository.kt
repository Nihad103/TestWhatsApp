package com.example.testwhatsapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testwhatsapp.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun fetchMessages(chatId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        database.child("chats").child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    try {
                        // Verilerin doğru formatta olup olmadığını kontrol et
                        val messageMap = messageSnapshot.value as? Map<String, Any>
                        if (messageMap != null && messageMap.containsKey("content") && messageMap.containsKey("timestamp")) {
                            val message = messageSnapshot.getValue(Message::class.java)
                            if (message != null) {
                                messages.add(message)
                            } else {
                                Log.e("FetchMessages", "Message is null or could not be converted")
                            }
                        } else {
                            Log.e("FetchMessages", "Message data is not in expected format: ${messageSnapshot.value}")
                        }
                    } catch (e: Exception) {
                        Log.e("FetchMessages", "Error converting message: ${e.message}")
                    }
                }
                messagesLiveData.value = messages
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchMessages", "Error: ${error.message}")
            }
        })
        return messagesLiveData
    }

    fun sendMessage(chatId: String, message: Message, receiverId: String) {
        val messageId = database.child("chats").child(chatId).push().key
        if (messageId != null) {
            val timestamp = System.currentTimeMillis()
            message.id = messageId
            message.timestamp = timestamp
            database.child("chats").child(chatId).child(messageId).setValue(message)
            updateLastMessage(senderId = message.sender, receiverId = receiverId, chatId = chatId, lastMessage = message.content, timestamp = timestamp)        }
    }

    private fun updateLastMessage(senderId: String, receiverId: String, chatId: String, lastMessage: String, timestamp: Long) {
        val userUpdates = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to timestamp
        )

        // Göndərən və alıcı üçün yeniləmələri əlavə et
        database.child("users").child(senderId).child("chats").child(chatId).updateChildren(userUpdates)
        database.child("users").child(receiverId).child("chats").child(chatId).updateChildren(userUpdates)

        // Çatın özünü də yenilə
        val chatUpdates = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to timestamp
        )
        database.child("chats").child(chatId).updateChildren(chatUpdates)
    }

}