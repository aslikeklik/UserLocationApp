package com.example.userlocationapp.data.service

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LocationEventManager {

    private val _locationUpdates = MutableSharedFlow<LatLng>(replay = 1)
    val locationUpdates = _locationUpdates.asSharedFlow()

    suspend fun emitLocation(location: LatLng) {
        _locationUpdates.emit(location)
    }
}

