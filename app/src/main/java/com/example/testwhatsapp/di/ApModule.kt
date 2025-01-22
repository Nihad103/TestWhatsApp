package com.example.testwhatsapp.di

import android.content.Context
import android.content.SharedPreferences
import com.example.testwhatsapp.repository.ChatListRepository
import com.example.testwhatsapp.repository.MessageRepository
import com.example.testwhatsapp.viewmodel.ChatListViewModel
import com.example.testwhatsapp.viewmodel.ChatViewModel
import com.example.testwhatsapp.viewmodel.LoginViewModel
import com.example.testwhatsapp.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single<SharedPreferences> { androidContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { RegisterViewModel(get(), get(), get()) }
    single { MessageRepository() }
    viewModel { ChatViewModel(get()) }
    single { ChatListRepository() }
    viewModel { ChatListViewModel(get()) }
}