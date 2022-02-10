package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.application.MyApplication
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.repo.Repository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext




class StoreDetailsViewModel() : ViewModel() {

    private val repo = Repository()
    private var polylines = MutableLiveData<List<LatLng>>()
    private var storeInfoData = MutableLiveData<StoreInfo>()
    private var successMessage = MutableLiveData<String>()

    fun getStoreInfo(): LiveData<StoreInfo> = storeInfoData

    fun getCheckinSuccessMessage() : LiveData<String> = successMessage

    fun checkin(storeInfo: StoreInfo) {
        val updateFbDb: HashMap<String, Any> = HashMap()
        storeInfo.name?.let { updateFbDb.put("name", it) }
        storeInfo.g?.let { updateFbDb.put("g", it) }
        storeInfo.l?.let { updateFbDb.put("l", it) }
        storeInfo.visited?.let {
            updateFbDb["visited"] = it + 1
        }
        repo.getFirebaseRefs().child("geofire").child("store-${storeInfo.g}") .updateChildren(updateFbDb).addOnSuccessListener {
            successMessage.postValue("Thank you for visiting!")
        }
    }

    fun getDetails(storeInfo: StoreInfo) {
        repo.getFirebaseRefs().child("geofire").child("store-${storeInfo.g}").get().addOnSuccessListener {
            val item = it.getValue(StoreInfo::class.java)
            storeInfoData.postValue(item)
        }

    }

    fun getPolylines(): LiveData<List<LatLng>> = polylines

    fun getDirection(origin:String, destination:String) {
        val geoApiContext = GeoApiContext.Builder().apiKey(MyApplication.appContext.getString(R.string.google_maps_key)).build()
        val req = DirectionsApi.getDirections(geoApiContext, origin, destination)
        val path = ArrayList<LatLng>()

        try {
            val res = req.await()
            res?.let {
                val route = res.routes[0]
                route.legs?.let {  legs ->
                    for (i in legs.indices) {
                        val leg = legs[i]
                        leg.steps.let { steps ->
                            for (j in steps.indices) {
                                val step = steps[j]
                                if (step.steps != null && step.steps.isNotEmpty()) {
                                    for (k in step.steps.indices) {
                                        val step1 = step.steps[k]
                                        step1.polyline?.let { points ->
                                            for (coord in points.decodePath()) {
                                                path.add(LatLng(coord.lat, coord.lng))
                                            }
                                        }
                                    }
                                } else {
                                    step.polyline?.let { points ->
                                        for (coord in points.decodePath()) {
                                            path.add(LatLng(coord.lat, coord.lng))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        } catch (e:Exception) {e.printStackTrace() }

        //Draw the polyline
        if (path.size > 0) {
            polylines.postValue(path)
        }

    }

    companion object {
        private const val TAG = "MINE"
    }
}