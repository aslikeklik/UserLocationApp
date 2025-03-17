package com.example.userlocationapp.di

import android.content.Context
import com.example.userlocationapp.data.local.RouteStorage
import com.example.userlocationapp.data.repository.LocationRepository
import com.example.userlocationapp.data.repository.LocationRepositoryImpl
import com.example.userlocationapp.domain.usecase.TrackLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRouteStorage(
        @ApplicationContext context: Context
    ): RouteStorage {
        return RouteStorage(context)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(routeStorage: RouteStorage): LocationRepository {
        return LocationRepositoryImpl(routeStorage)
    }

    @Provides
    @Singleton
    fun provideTrackLocationUseCase(repository: LocationRepository): TrackLocationUseCase {
        return TrackLocationUseCase(repository)
    }
}
