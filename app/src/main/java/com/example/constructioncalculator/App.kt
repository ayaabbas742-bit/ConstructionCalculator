package com.example.constructioncalculator

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NoteManager.init(this)
    }
}