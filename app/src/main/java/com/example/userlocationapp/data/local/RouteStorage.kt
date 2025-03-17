package com.example.userlocationapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "route_prefs")
private val ROUTE_KEY = stringPreferencesKey("saved_route")

class RouteStorage(private val context: Context) {

    suspend fun saveRoute(route: List<LatLng>) {
        val routeString = route.joinToString("|") { "${it.latitude},${it.longitude}" }
        context.dataStore.edit { preferences ->
            preferences[ROUTE_KEY] = routeString
        }
    }

    suspend fun loadRoute(): List<LatLng> {
        val preferences = context.dataStore.data.first()
        val routeString = preferences[ROUTE_KEY] ?: return emptyList()

        return routeString.split("|").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) LatLng(lat, lng) else null
            } else null
        }
    }

    suspend fun clearRoute() {
        context.dataStore.edit { preferences ->
            preferences.remove(ROUTE_KEY)
        }
    }
}
