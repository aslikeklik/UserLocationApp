package com.example.userlocationapp.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.userlocationapp.domain.usecase.TrackLocationUseCase
import com.example.userlocationapp.data.local.RouteStorage
import com.example.userlocationapp.data.repository.LocationRepositoryImpl

class MapsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            val storage = RouteStorage(context)
            val repository = LocationRepositoryImpl(storage)
            val useCase = TrackLocationUseCase(repository)
            return MapsViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
