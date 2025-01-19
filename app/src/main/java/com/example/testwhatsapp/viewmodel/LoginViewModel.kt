package com.example.testwhatsapp.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val auth: FirebaseAuth, private val database: FirebaseDatabase, private val sharedPref: SharedPreferences) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _loginErrorMessage = MutableLiveData<String>()
    val loginErrorMessage: LiveData<String> get() = _loginErrorMessage

    fun loginUser(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid
                if (userId != null) {
                    val snapshot = database.getReference("users").child(userId).get().await()
                    if (snapshot.exists()) {
                        if (rememberMe) {
                            with(sharedPref.edit()) {
                                putBoolean("rememberMe", true)
                                apply()
                            }
                        }
                        _loginResult.value = true
                    } else {
                        _loginErrorMessage.value = "Username not found"
                    }
                } else {
                    _loginErrorMessage.value = "Could not get User ID"
                }
            } catch (e: Exception) {
                _loginErrorMessage.value = e.message ?: "Login failed"
            }
        }
    }
}