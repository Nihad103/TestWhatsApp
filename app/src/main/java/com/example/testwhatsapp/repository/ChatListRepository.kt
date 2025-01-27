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

                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        val lastMessage = chatSnapshot.child("lastMessage").value as? String ?: ""
                        val lastMessageTimestamp = chatSnapshot.child("lastMessageTimestamp").value as? Long ?: 0L

                        val receiverId = getReceiverIdFromChatId(chatId, currentUserId) ?: continue

                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId)
                        userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(receiverSnapshot: DataSnapshot) {
                                val receiverName = receiverSnapshot.value as? String ?: "Unknown"

                                val chat = ChatList(
                                    chatId = chatId,
                                    lastMessage = lastMessage,
                                    lastMessageTimestamp = lastMessageTimestamp,
                                    receiverName = receiverName
                                )
                                chatList.add(chat)

                                if (chatList.size == snapshot.childrenCount.toInt()) {
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