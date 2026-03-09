package de.stroebele.mindyourself.sync.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Sync module — WearDataClient is @Singleton and injected directly via @Inject constructor.
 * No explicit @Provides needed; Hilt resolves it automatically.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule
