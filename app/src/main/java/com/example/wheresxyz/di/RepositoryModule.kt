package com.example.wheresxyz.di

import com.example.wheresxyz.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepository: FirebaseGroupRepository
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepository: FirebaseUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPingRepository(
        pingRepository: FirebasePingRepository
    ): PingRepository
}
