package com.example.userlocationapp.domain.usecase

import com.example.userlocationapp.data.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng

class TrackLocationUseCase(
    private val locationRepository: LocationRepository
) {

    suspend fun saveRoute(route: List<LatLng>) {
        locationRepository.saveRoute(route)
    }

    suspend fun loadRoute(): List<LatLng> {
        return locationRepository.loadRoute()
    }

    suspend fun clearRoute() {
        locationRepository.clearRoute()
    }
}
