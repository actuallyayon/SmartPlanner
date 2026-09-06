package com.smartplanner.di

import com.smartplanner.model.MockRepository
import com.smartplanner.model.Repository
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
    abstract fun bindRepository(
        mockRepository: MockRepository
    ): Repository
}
