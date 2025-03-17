package com.example.userlocationapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.userlocationapp.domain.model.Route
import com.example.userlocationapp.domain.usecase.TrackLocationUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val trackLocationUseCase: TrackLocationUseCase
) : ViewModel() {

    private val _route = MutableStateFlow(Route())
    val route: StateFlow<Route> = _route

    fun addLocationPoint(latLng: LatLng) {
        val updated = _route.value.copy(points = _route.value.points + latLng)
        _route.value = updated
        saveRoute(updated)
    }

    private fun saveRoute(route: Route) {
        viewModelScope.launch {
            trackLocationUseCase.saveRoute(route.points)
        }
    }

    fun loadSavedRoute() {
        viewModelScope.launch {
            val savedPoints = trackLocationUseCase.loadRoute()
            _route.value = Route(savedPoints)
        }
    }

    fun clearRoute() {
        viewModelScope.launch {
            trackLocationUseCase.clearRoute()
            _route.value = Route()
        }
    }
}