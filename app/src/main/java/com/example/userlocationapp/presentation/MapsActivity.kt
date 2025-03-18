package com.example.userlocationapp.presentation

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.userlocationapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale
import com.example.userlocationapp.R
import com.example.userlocationapp.utils.PermissionUtils
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private val viewModel: MapsViewModel by viewModels()
    private var mapInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        setupMap()
        viewModel.loadSavedRoute()
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapInitialized = true
        observeRoute()
        checkAndRequestLocationPermissions()

        mMap.setOnMarkerClickListener { marker ->
            val address = getAddressFromLatLng(marker.position)
            marker.remove()
            mMap.addMarker(MarkerOptions().position(marker.position).title(address))?.showInfoWindow()
            true
        }
    }

    private fun observeRoute() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.route.collectLatest { route ->
                    if (mapInitialized) {
                        mMap.clear()
                        route.points.forEach {
                            mMap.addMarker(MarkerOptions().position(it).title(getString(R.string.location)))
                        }
                        route.points.lastOrNull()?.let {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                        }
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (hasLocationPermissions()) {
                viewModel.startLocationService(applicationContext)
                toggleButtons(start = false, stop = true, reset = true)
            } else {
                requestLocationPermissions()
            }
        }

        binding.stopButton.setOnClickListener {
            viewModel.stopLocationService(applicationContext)
            toggleButtons(start = true, stop = false, reset = true)
        }

        binding.resetButton.setOnClickListener {
            viewModel.clearRoute()
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
        alpha = if (enabled) 1f else 0.5f
        isClickable = enabled
    }

    private fun getAddressFromLatLng(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            address?.firstOrNull()?.getAddressLine(0) ?: getString(R.string.address_not_found)
        } catch (e: Exception) {
            getString(R.string.address_failed)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return PermissionUtils.locationPermissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            PermissionUtils.locationPermissions,
            PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkAndRequestLocationPermissions() {
        if (hasLocationPermissions()) {
            onLocationPermissionGranted()
        } else {
            requestLocationPermissions()
        }
    }

    private fun onLocationPermissionGranted() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        binding.progressBar.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onLocationPermissionGranted()
            } else {
                Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
