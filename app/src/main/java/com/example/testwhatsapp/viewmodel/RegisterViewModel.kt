package com.example.testwhatsapp.viewmodel

import android.content.SharedPreferences
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
                    val user = User(id = userId, name = userName, email = email)
                    database.getReference("users").child(userId).setValue(user).await()
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
}
