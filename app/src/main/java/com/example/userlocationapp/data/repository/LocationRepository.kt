package com.example.userlocationapp.data.repository

import com.google.android.gms.maps.model.LatLng

interface LocationRepository {
    suspend fun saveRoute(route: List<LatLng>)
    suspend fun loadRoute(): List<LatLng>
    suspend fun clearRoute()
}
