package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.repo.Repository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class ReportsViewModel() : ViewModel() {

    private val repo = Repository()
    private var allStoreContainer = ArrayList<StoreInfo>()
    private var allStore = MutableLiveData<List<StoreInfo>>()

    fun getAllStores(): MutableLiveData<List<StoreInfo>> = allStore


    fun showAllStores() {
        allStoreContainer.clear()

        val query = repo.getFirebaseRefs().child("geofire")

        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val item = snapshot.getValue(StoreInfo::class.java)
                item?.let {
                    allStoreContainer.add(item)
                }

                val sorted = allStoreContainer.sortedByDescending { it.visited }
                allStore.postValue(sorted)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "im in @ onChildMoved")
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    companion object {
        private const val TAG = "MINE"
    }
}