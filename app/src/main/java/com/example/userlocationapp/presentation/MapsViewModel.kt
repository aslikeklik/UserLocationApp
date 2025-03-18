package com.example.userlocationapp.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.userlocationapp.data.service.LocationEventManager
import com.example.userlocationapp.data.service.LocationService
import com.example.userlocationapp.domain.model.Route
import com.example.userlocationapp.domain.usecase.TrackLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val trackLocationUseCase: TrackLocationUseCase
) : ViewModel() {

    private val _route = MutableStateFlow(Route())
    val route: StateFlow<Route> = _route

    init {
        observeLocationUpdates()
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            LocationEventManager.locationUpdates.collectLatest { latLng ->
                val updatedPoints = _route.value.points + latLng
                val newRoute = Route(updatedPoints)
                _route.value = newRoute
                trackLocationUseCase.saveRoute(updatedPoints)
            }
        }
    }

    fun loadSavedRoute() {
        viewModelScope.launch {
            val saved = trackLocationUseCase.loadRoute()
            _route.value = Route(saved)
        }
    }

    fun clearRoute() {
        viewModelScope.launch {
            trackLocationUseCase.clearRoute()
            _route.value = Route()
        }
    }

    fun startLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
}
