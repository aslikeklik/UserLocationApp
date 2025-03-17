package com.example.userlocationapp.domain.model

import com.google.android.gms.maps.model.LatLng

data class Route(
    val points: List<LatLng> = emptyList()
)
