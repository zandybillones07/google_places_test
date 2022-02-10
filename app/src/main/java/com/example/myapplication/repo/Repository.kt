package com.example.myapplication.repo

import com.example.myapplication.data.SharedPrefManager
import com.google.firebase.database.FirebaseDatabase

class Repository {

    private var sharedPrefManager = SharedPrefManager.instance
    private val ref = FirebaseDatabase.getInstance().reference

    fun getFirebaseRefs() = ref

    companion object {
        private const val TAG = "MINE"
    }


}
