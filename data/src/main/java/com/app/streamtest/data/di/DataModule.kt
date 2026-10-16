package com.app.streamtest.data.di

import com.app.streamtest.data.catalog.AssetCatalogRepository
import com.app.streamtest.data.device.AndroidDeviceInfoRepository
import com.app.streamtest.data.results.RoomResultsRepository
import com.app.streamtest.domain.catalog.CatalogRepository
import com.app.streamtest.domain.device.DeviceInfoRepository
import com.app.streamtest.domain.results.ResultsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Tells Hilt which :data class to hand out when someone asks for a :domain interface.
// SingletonComponent = these live as long as the app.
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindCatalogRepository(repository: AssetCatalogRepository): CatalogRepository

    @Binds
    abstract fun bindDeviceInfoRepository(repository: AndroidDeviceInfoRepository): DeviceInfoRepository

    @Binds
    abstract fun bindResultsRepository(repository: RoomResultsRepository): ResultsRepository
}
