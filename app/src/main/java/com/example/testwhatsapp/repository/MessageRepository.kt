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
        database.child("messages").child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                val lastMessage = snapshot.child("lastMessage").getValue(String::class.java) ?: "No last message"
                val lastMessageTimestamp = snapshot.child("lastMessageTimestamp").getValue(Long::class.java) ?: 0L

                Log.d("FetchMessages", "Last message: $lastMessage at $lastMessageTimestamp")

                for (messageSnapshot in snapshot.children) {
                    try {
                        if (messageSnapshot.key != "lastMessage" && messageSnapshot.key != "lastMessageTimestamp") {
                            val message = messageSnapshot.getValue(Message::class.java)
                            if (message != null) {
                                messages.add(message)
                                Log.d("FetchMessages", "Message added: $message")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FetchMessages", "Error converting message: ${e.message}")
                    }
                }
                messagesLiveData.value = messages
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchMessages", "OnCancelled: ${error.message}")
            }
        })
        return messagesLiveData
    }

    fun sendMessage(chatId: String, message: Message, receiverId: String) {
        val messageId = database.child("chats").child(chatId).push().key
        if (messageId != null) {
            val timestamp = System.currentTimeMillis()
            val senderId = message.sender
            val newMessage = message.copy(messageId = messageId, timestamp = timestamp, receiver = receiverId)
            database.child("messages").child(chatId).child(messageId).setValue(newMessage)
            updateLastMessage(senderId, receiverId, chatId, newMessage.content, timestamp)
        }
    }

    private fun updateLastMessage(senderId: String, receiverId: String, chatId: String, lastMessage: String, timestamp: Long) {
        val userUpdates = mapOf(
            "lastMessage" to lastMessage,
            "lastMessageTimestamp" to timestamp
        )
        database.child("users").child(senderId).child("chats").child(chatId).updateChildren(userUpdates)
        database.child("users").child(receiverId).child("chats").child(chatId).updateChildren(userUpdates)
    }

}
