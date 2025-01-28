package com.example.testwhatsapp.repository

import android.util.Log
import com.example.testwhatsapp.model.ChatList
import com.google.firebase.database.*

class ChatListRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun loadChatList(currentUserId: String, onResult: (List<ChatList>) -> Unit, onError: (DatabaseError) -> Unit) {
        val chatListRef = database.child(currentUserId).child("chats")
        chatListRef.orderByChild("lastMessageTimestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<ChatList>()
                    var processedCount = 0 // İşlenen chat sayısını takip etmek için

                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        val lastMessage = chatSnapshot.child("lastMessage").value as? String ?: ""
                        val lastMessageTimestamp = chatSnapshot.child("lastMessageTimestamp").value as? Long ?: 0L

                        val receiverId = getReceiverIdFromChatId(chatId, currentUserId) ?: continue

                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId)
                        userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(receiverSnapshot: DataSnapshot) {
                                processedCount++
                                val receiverName = receiverSnapshot.value as? String

                                if (!receiverSnapshot.exists() || receiverName == null || receiverName == "Unknown") {
                                    if (processedCount == snapshot.childrenCount.toInt()) {
                                        chatList.sortByDescending { it.lastMessageTimestamp }
                                        onResult(chatList)
                                    }
                                    return
                                }

                                val chat = ChatList(
                                    chatId = chatId,
                                    lastMessage = lastMessage,
                                    lastMessageTimestamp = lastMessageTimestamp,
                                    receiverName = receiverName
                                )
                                chatList.add(chat)

                                if (processedCount == snapshot.childrenCount.toInt()) {
                                    chatList.sortByDescending { it.lastMessageTimestamp }
                                    onResult(chatList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ChatListRepository", "Error fetching receiver name: ${error.message}")
                                onError(error)
                            }
                        })
                    }

                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        onResult(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatListRepository", "Error loading chat list: ${error.message}")
                    onError(error)
                }
            })
    }

    private fun getReceiverIdFromChatId(chatId: String, currentUserId: String): String? {
        val participants = chatId.split("-")
        return if (participants.size == 2) {
            participants.firstOrNull { it != currentUserId }
        } else {
            null
        }
    }

}