package com.example.userlocationapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.userlocationapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val viewModel: MapsViewModel by viewModels { MapsViewModelFactory(applicationContext) }
    private var tracking = false
    private var lastLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.mapFragmentContainer.id, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.loadSavedRoute()

        lifecycleScope.launchWhenStarted {
            viewModel.route.collectLatest { route ->
                if (::mMap.isInitialized) {
                    mMap.clear()
                    route.points.forEach {
                        mMap.addMarker(MarkerOptions().position(it).title("Konum"))
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener { marker ->
            val latLng = marker.position
            val address = getAddressFromLatLng(latLng)
            marker.remove()
            val newMarker = mMap.addMarker(MarkerOptions().position(latLng).title(address))
            newMarker?.showInfoWindow()
            true
        }

        checkAndRequestLocation()

        viewModel.route.value.points.forEach {
            mMap.addMarker(MarkerOptions().position(it).title("Önceki Konum"))
        }
    }

    private fun checkAndRequestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            mMap.isMyLocationEnabled = true
            val locationRequest = LocationRequest.create().apply {
                priority = Priority.PRIORITY_HIGH_ACCURACY
                numUpdates = 1
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                    binding.progressBar.visibility = View.GONE
                    binding.mapFragmentContainer.visibility = View.VISIBLE
                }
            }, mainLooper)
        }
    }

    private fun startTracking() {
        tracking = true

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val currentLatLng = LatLng(location.latitude, location.longitude)

                if (lastLocation == null || distanceBetween(lastLocation!!, currentLatLng) >= 10) {
                    viewModel.addLocationPoint(currentLatLng)
                    lastLocation = currentLatLng
                }
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopTracking() {
        tracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun resetRoute() {
        stopTracking()
        viewModel.clearRoute()
        lastLocation = null
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            startTracking()
            toggleButtons(start = false, stop = true, reset = true)
        }

        binding.stopButton.setOnClickListener {
            stopTracking()
            toggleButtons(start = true, stop = false, reset = true)
        }

        binding.resetButton.setOnClickListener {
            resetRoute()
            toggleButtons(start = true, stop = false, reset = false)
        }

        toggleButtons(start = true, stop = false, reset = false)
    }

    private fun toggleButtons(start: Boolean, stop: Boolean, reset: Boolean) {
        binding.startButton.setCustomEnabled(start)
        binding.stopButton.setCustomEnabled(stop)
        binding.resetButton.setCustomEnabled(reset)
    }

    private fun View.setCustomEnabled(enabled: Boolean) {
        isEnabled = enabled
        alpha = 1f
        isClickable = enabled
        isFocusable = enabled
    }

    private fun getAddressFromLatLng(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            address?.firstOrNull()?.getAddressLine(0) ?: "Adres bulunamadı"
        } catch (e: Exception) {
            "Adres alınamadı"
        }
    }

    private fun distanceBetween(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0]
    }
}
