package com.example.wheresxyz.di

import com.example.wheresxyz.repository.AuthRepository
import com.example.wheresxyz.repository.FirebaseAuthRepository
import com.example.wheresxyz.repository.FirebaseGroupRepository
import com.example.wheresxyz.repository.GroupRepository
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
}
