package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import java.lang.IllegalStateException

class SharedPrefManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE)

    private fun putLong(key: String, value:Long) {
        prefs.edit().putLong(key, value).apply()
    }
    private fun putBoolean(key: String, value:Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    private fun getLong(key: String, default: Long) = prefs.getLong(key, default)
    private fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)

    fun getLastTimerValue() = getLong(VALUE_TIMER, 0)
    fun isRunning() = getBoolean(VALUE_IS_RUNNING, false)

    fun saveTimer(value:Long) {
        putLong(VALUE_TIMER, value)
    }
    fun isRunning(value:Boolean) {
        putBoolean(VALUE_IS_RUNNING, value)
    }

    companion object {
        private const val KEY_PREFS = "key_prefs"
        private const val VALUE_TIMER = "val_timer"
        private const val VALUE_IS_RUNNING = "val_should_resume"

        private var sInstance: SharedPrefManager? = null

        @Synchronized
        fun initializeInstance(context: Context) {
            if (sInstance == null) sInstance = SharedPrefManager(context)
        }

        val instance: SharedPrefManager
        @Synchronized get() {
            if (sInstance == null) throw IllegalStateException(SharedPrefManager::class.java.simpleName + " is not initialized")
            return sInstance as SharedPrefManager
        }
    }

}