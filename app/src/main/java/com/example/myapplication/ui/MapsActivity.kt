package com.example.myapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMapsBinding
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.util.LocationService
import com.example.myapplication.ui.viewmodel.MapViewModel
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var lastKnownLocation:Location? = null
    private val defaultLocation = LatLng(10.317986799476893, 123.90489825329787) // cebu
    private var currentLocation = defaultLocation
    private var isShowAll = true
    private var isManual = false

    private lateinit var messageReceiver: BroadcastReceiver;

    private lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupViewModel()
        setupUI()
        initPlacesApi()
        initAutoComplete()


        viewModel.showAllStores()


        initLocationServiceReceiver()

    }

    private fun setupUI() {
        autocomplete_fragment.view?.visibility = View.GONE
        binding.inputChb.visibility = View.GONE
        binding.holderRdo.setOnCheckedChangeListener { _, i ->
            when (i) {
                binding.allRdo.id -> {
                    isShowAll = true
                    isManual = false
                    autocomplete_fragment.view?.visibility = View.GONE
                    binding.inputChb.visibility = View.GONE
                }
                binding.nearbyRdo.id -> {
                    if (!locationPermissionGranted) {
                        getLocationPermission()
                    }
                    isShowAll = false
                    isManual = false
                    binding.inputChb.isChecked = false
                    autocomplete_fragment.view?.visibility = View.GONE
                    binding.inputChb.visibility = View.VISIBLE
                }
            }
            clearMarkers()
            if (isShowAll) {
                viewModel.showAllStores()
            } else {
                viewModel.getNearByStores(GeoLocation(currentLocation.latitude, currentLocation.longitude))
            }

        }

        binding.inputChb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isShowAll = false
                isManual = true
                autocomplete_fragment.view?.visibility = View.VISIBLE
            } else {
                isManual = false
                autocomplete_fragment.view?.visibility = View.GONE
            }
        }

        report_bt.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }
    }



    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java).apply {
            getNearByStores().observe(this@MapsActivity, {
                renderStores(it, true)
            })

            getPolylines().observe(this@MapsActivity, {
                val opts = PolylineOptions().addAll(it).color(Color.RED).width(5f)
                mMap.addPolyline(opts)
            })

            getAllStores().observe(this@MapsActivity, {
                renderStores(it)
            })
        }
    }

    private fun initLocationServiceReceiver() {

        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra("MSG_FROM_SERVICE")
                message?.let {
                    val lat = it.split(",")[0]
                    val lng = it.split(",")[1]
                    currentLocation = LatLng(lat.toDouble(),lng.toDouble())
                    if (!isShowAll && !isManual) {
                        clearMarkers()
                        viewModel.getNearByStores(GeoLocation(lat.toDouble(), lng.toDouble()))
                    }
                }

            }
        }
    }


    private fun renderStores(list: List<StoreInfo>, isNearby: Boolean = false) {
        list.forEach {
            it.let {
                val lat = it.l?.get(0)
                val lng = it.l?.get(1)
                val latLng = lat?.let { it1 -> lng?.let { it2 -> LatLng(it1, it2) } }
                it.name?.let { it1 -> latLng?.let { it2 ->
                    addMarker(it2, it1, id = it.g, isNearby, storeInfo = it)
                } }
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
    }


    private fun initPlacesApi() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            initService()
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            }
        } catch (e: SecurityException) { e.printStackTrace()}
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            lastKnownLocation = task.result
                            lastKnownLocation?.let {
                                currentLocation = LatLng(it.latitude,it.longitude)
                                if (!isShowAll && isManual) {
                                    clearMarkers()
                                    updateLocationUI()
                                    viewModel.getNearByStores(GeoLocation(it.latitude, it.longitude))
                                }

                            }

                        }

                    } else {
                        currentLocation = defaultLocation
                        moveAndZoomMap()
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun moveAndZoomMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM.toFloat()))
    }

    private fun initAutoComplete() {

        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            .setCountry("PH")
            .setHint("Type your location")

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                clearMarkers()
                currentLocation = place.latLng
                moveAndZoomMap()
                viewModel.getNearByStores(GeoLocation(place.latLng.latitude, place.latLng.longitude))
            }

            override fun onError(status: Status) {}
        })

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        getLocationPermission()


    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun addMarker(latLng: LatLng, title:String, id:String? = null, isNearby:Boolean = false, storeInfo: StoreInfo? = null) {

        val markerOptions = MarkerOptions().position(latLng).title(title)

        if (isNearby)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))



        val marker = mMap.addMarker(markerOptions)
        marker?.tag = storeInfo

        mMap.setOnInfoWindowClickListener {
            val intent = Intent(this, StoreDetailsActivity::class.java)
            intent.putExtra("storeInfo", it.tag as StoreInfo)
            intent.putExtra("origin", currentLocation)
            startActivity(intent)
        }
    }

    private fun clearMarkers() {
        mMap.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    updateLocationUI()
                    getDeviceLocation()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        //updateLocationUI()
    }

    private fun initService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, IntentFilter("EVENT"))
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15
        private const val TAG = "MINE"
    }
}