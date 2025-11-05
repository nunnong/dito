package com.dito.app.core.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.dito.app.core.repository.HealthRepository
import com.dito.app.core.repository.HealthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthModule {

    @Provides
    @Singleton
    fun provideHealthConnectClient(
        @ApplicationContext context: Context
    ): HealthConnectClient? {
        return if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHealthRepository(
        impl: HealthRepositoryImpl
    ): HealthRepository
}
