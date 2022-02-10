package com.example.myapplication.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocationService : Service() {


    private var locationNetwork: Location? = null
    private var locationGps: Location? = null
    private var hasGps = false
    private var hasNetwork = false

    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        getUserLocation()

    }


    private fun sendBroadcast(message: String) {
        val it = Intent("EVENT")

        if (!TextUtils.isEmpty(message)) {
            it.putExtra(EXTRA_RETURN_MESSAGE, message)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(it)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {

        if (hasGps || hasNetwork) {

            if (hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DELAY, 0F
                ) { location ->
                    locationNetwork = location
                    broadCast("${locationNetwork?.latitude},${locationNetwork?.longitude}")
                }

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if (hasGps) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELAY, 0F
                ) { location ->
                    locationGps = location
                    broadCast("${locationGps?.latitude},${locationGps?.longitude}")
                }

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation

            }

            if (locationGps!= null && locationNetwork!= null) {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy)
                    broadCast("${locationNetwork?.latitude},${locationNetwork?.longitude}")
                else
                    broadCast("${locationGps?.latitude},${locationGps?.longitude}")
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun broadCast(message:String) {
        sendBroadcast(message)
    }

    companion object {
        private const val EXTRA_RETURN_MESSAGE = "MSG_FROM_SERVICE"
        private const val DELAY = 15000L
    }

}
