package com.example.myapplication.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityStoreDetailsBinding
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.ui.viewmodel.StoreDetailsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class StoreDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityStoreDetailsBinding
    private lateinit var viewModel: StoreDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoreDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupViewModel()
    }

    private fun initUI() {
        if (intent.hasExtra("storeInfo")) {
            val storeInfo = intent.getParcelableExtra<StoreInfo>("storeInfo")
            storeInfo?.let { viewModel.getDetails(it) }
        }
        binding.backBt.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getDirection(origin:String, destination:String) {
        viewModel.getDirection(origin, destination)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(StoreDetailsViewModel::class.java).apply {

            getPolylines().observe(this@StoreDetailsActivity, {
                val opts = PolylineOptions().addAll(it).color(Color.RED).width(5f)
                mMap.addPolyline(opts)
            })

            getStoreInfo().observe(this@StoreDetailsActivity, {
                renderStoreInfo(it)
            })

            getCheckinSuccessMessage().observe(this@StoreDetailsActivity, { str ->
                AlertDialog.Builder(this@StoreDetailsActivity).apply {
                    setMessage(str)
                    setPositiveButton("Ok"
                    ) { p0, _ ->
                        p0.dismiss()
                        updateVisited()
                    }
                }.show()
            })

        }
    }

    private fun updateVisited() {
        val newVisit = binding.totalGuestTv.text.toString().toInt() + 1
        binding.totalGuestTv.text = "$newVisit"
    }

    private fun renderStoreInfo(storeInfo: StoreInfo) {
        binding.nameTv.text = storeInfo.name
        binding.totalGuestTv.text = storeInfo.visited.toString()
        binding.appCompatTextView.text = storeInfo.details
        binding.menuTv.text = storeInfo.menus

        val lat = storeInfo.l?.get(0)
        val lng = storeInfo.l?.get(1)

        val destination = lat?.let { lng?.let { it1 -> LatLng(it, it1) } }
        val origin = intent.getParcelableExtra<LatLng>("origin")

        destination?.let {
            mMap.addMarker(MarkerOptions().position(it).title(storeInfo.name))
            origin?.let { orig ->
                val markerOption = MarkerOptions().position(orig).title("You are here!")
                markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                mMap.addMarker(markerOption)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(orig, DEFAULT_ZOOM.toFloat()))
            }

            getDirection("${origin?.latitude}, ${origin?.longitude}", "$lat, $lng")

        }

        binding.checkinBt.setOnClickListener {
            it.visibility = View.GONE
            storeInfo.let { it1 -> viewModel.checkin(it1) }
        }
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
        initUI()
    }

    companion object {
        private const val DEFAULT_ZOOM = 14f
        private const val TAG = "MINE"
    }
}