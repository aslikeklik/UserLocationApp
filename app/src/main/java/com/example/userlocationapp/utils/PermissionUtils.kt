package com.example.userlocationapp.utils

import android.Manifest
import android.os.Build

object PermissionUtils {
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    val locationPermissions: Array<String>
        get() {
            val list = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                list.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            }
            return list.toTypedArray()
        }
}
