package com.example.myapplication.application

import android.app.Application
import android.content.Context
import com.example.myapplication.data.SharedPrefManager

public class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        SharedPrefManager.Companion.initializeInstance(this)
        appContext = applicationContext
    }

    companion object {
        lateinit  var appContext: Context
    }
}