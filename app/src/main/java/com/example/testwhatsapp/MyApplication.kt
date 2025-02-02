package com.example.testwhatsapp

import android.app.Application
import com.example.testwhatsapp.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}