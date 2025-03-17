package com.example.userlocationapp.data.repository

import com.example.userlocationapp.data.local.RouteStorage
import com.google.android.gms.maps.model.LatLng

class LocationRepositoryImpl(
    private val routeStorage: RouteStorage
) : LocationRepository {

    override suspend fun saveRoute(route: List<LatLng>) {
        routeStorage.saveRoute(route)
    }

    override suspend fun loadRoute(): List<LatLng> {
        return routeStorage.loadRoute()
    }

    override suspend fun clearRoute() {
        routeStorage.clearRoute()
    }
}
