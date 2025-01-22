package com.example.testwhatsapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testwhatsapp.model.User
import com.google.firebase.database.*

class ChatListRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun fetchUsers(): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()
        database.orderByChild("lastMessageTimestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        if (user.id.isNotEmpty() && user.name.isNotEmpty()) {
                            users.add(user)
                            Log.e("ChatListRepository", "$user != null")

                        } else {
                            Log.e("ChatListRepository", "Invalid user data: $user")
                        }
                    } else {
                        Log.e("ChatListRepository", "User is null")
                    }
                }
                users.sortByDescending { it.lastMessageTimestamp }
                usersLiveData.value = users
                Log.d("ChatListRepository", "Users fetched: ${users.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatListRepository", "onCancelled")
            }
        })

        return usersLiveData
    }
}
