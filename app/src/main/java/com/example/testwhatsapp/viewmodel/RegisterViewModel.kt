package com.example.testwhatsapp.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testwhatsapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel(private val auth: FirebaseAuth, private val database: FirebaseDatabase, private val sharedPref: SharedPreferences) : ViewModel() {

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> get() = _registerResult

    private val _registerErrorMessage = MutableLiveData<String>()
    val registerErrorMessage: LiveData<String> get() = _registerErrorMessage

    fun registerUser(email: String, password: String, userName: String, rememberMe: Boolean) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid
                if (userId != null) {
                    createUser(userId, userName, email)
                    if (rememberMe) {
                        with(sharedPref.edit()) {
                            putBoolean("rememberMe", true)
                            apply()
                        }
                    }
                    _registerResult.value = true
                } else {
                    _registerErrorMessage.value = "Could not get User ID"
                }
            } catch (e: Exception) {
                _registerErrorMessage.value = e.message ?: "Registration failed"
            }
        }
    }

    private fun createUser(id: String, name: String, email: String) {
        if (id.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty()) {
            val user = User(id, name, email, "Welcome to the chat!", System.currentTimeMillis())
            database.getReference("users").child(id).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("CreateUser", "User created successfully")
                    } else {
                        Log.e("CreateUser", "User creation failed: ${task.exception?.message}")
                    }
                }
        } else {
            Log.e("CreateUser", "Invalid user data: id, name or email is empty")
        }
    }
}