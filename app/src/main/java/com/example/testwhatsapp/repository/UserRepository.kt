package com.example.testwhatsapp.repository

import com.example.testwhatsapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun loadAllUsers(onResult: (List<User>) -> Unit, onError: (DatabaseError) -> Unit) {
        val usersRef = database
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }
                onResult(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
}
