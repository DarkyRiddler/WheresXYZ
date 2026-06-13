package com.example.wheresxyz.di

import com.example.wheresxyz.data.repository.AuthRepository
import com.example.wheresxyz.data.repository.AuthRepositoryImpl
import com.example.wheresxyz.data.repository.FirebaseLocationRepository
import com.example.wheresxyz.data.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        firebaseLocationRepository: FirebaseLocationRepository
    ): LocationRepository
}
