package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.application.MyApplication
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.repo.Repository
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext

class MapViewModel(): ViewModel() {

    private val repo = Repository()
    private val geoFire = GeoFire(repo.getFirebaseRefs().child("geofire"))

    private var nearByStores = MutableLiveData<List<StoreInfo>>()
    private var polylines = MutableLiveData<List<LatLng>>()

    private var allStoreContainer = ArrayList<StoreInfo>()
    private var allStore = MutableLiveData<List<StoreInfo>>()

    fun getNearByStores():LiveData<List<StoreInfo>> = nearByStores
    fun getPolylines():LiveData<List<LatLng>> = polylines
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

                allStore.postValue(allStoreContainer)


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

    fun getNearByStores(location:GeoLocation) {
        allStoreContainer.clear()

        val geoQuery = geoFire.queryAtLocation(location,0.2)

        geoQuery.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
            override fun onDataEntered(dataSnapshot: DataSnapshot, location: GeoLocation) {
                location.let {
                    val item = dataSnapshot.getValue(StoreInfo::class.java)
                    item?.let {
                        allStoreContainer.add(item)
                    }

                }
            }

            override fun onDataExited(dataSnapshot: DataSnapshot?) {}

            override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {}

            override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {}

            override fun onGeoQueryReady() {
                nearByStores.postValue(allStoreContainer)
            }

            override fun onGeoQueryError(error: DatabaseError?) {}

        })
    }



    companion object {
        private const val TAG = "MINE"
    }

}