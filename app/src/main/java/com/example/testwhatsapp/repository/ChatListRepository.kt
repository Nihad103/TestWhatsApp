package com.example.testwhatsapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testwhatsapp.model.User
import com.google.firebase.database.*

class ChatListRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun fetchUser(currentUserId: String): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (data in snapshot.children) {
                    try {
                        val user = data.getValue(User::class.java)
                        if (user != null && user.id.isNotBlank() && user.name.isNotBlank()) {
                            // if lastMesssage isn't empty
                            val userChats = user.chats?.filter { (_, chat) ->
                                chat.lastMessage?.isNotBlank() ?: true
                            }

                            if (userChats != null && userChats.isNotEmpty()) {
                                users.add(user.copy(chats = userChats))
                            } else {
                                users.add(user.copy(chats = userChats ?: emptyMap()))
                            }

                        } else {
                            Log.w("ChatListRepository", "Invalid or incomplete user data: $user")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatListRepository", "Error parsing user data: ${e.message}")
                    }
                }

                users.sortByDescending { user ->
                    user.chats!!.values.maxOfOrNull { it.lastMessageTimestamp } ?: 0L
                }

                usersLiveData.value = users
                Log.d("ChatListRepository", "Users fetched and sorted: ${users.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatListRepository", "Database error: ${error.message}")
            }
        })

        return usersLiveData
    }
}