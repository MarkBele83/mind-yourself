package de.stroebele.mindyourself.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.stroebele.mindyourself.data.db.AppDatabase
import de.stroebele.mindyourself.data.db.dao.HealthCacheDao
import de.stroebele.mindyourself.data.db.dao.HydrationDao
import de.stroebele.mindyourself.data.db.dao.NamedLocationDao
import de.stroebele.mindyourself.data.db.dao.ReminderConfigDao
import de.stroebele.mindyourself.data.db.dao.ReminderStateDao
import de.stroebele.mindyourself.data.db.dao.SupplementDao
import de.stroebele.mindyourself.data.repository.HealthCacheRepositoryImpl
import de.stroebele.mindyourself.data.repository.HydrationRepositoryImpl
import de.stroebele.mindyourself.data.repository.NamedLocationRepositoryImpl
import de.stroebele.mindyourself.data.repository.ReminderConfigRepositoryImpl
import de.stroebele.mindyourself.data.repository.ReminderStateRepositoryImpl
import de.stroebele.mindyourself.data.repository.SupplementRepositoryImpl
import de.stroebele.mindyourself.data.repository.VacationSettingsRepositoryImpl
import de.stroebele.mindyourself.domain.repository.HealthCacheRepository
import de.stroebele.mindyourself.domain.repository.HydrationRepository
import de.stroebele.mindyourself.domain.repository.NamedLocationRepository
import de.stroebele.mindyourself.domain.repository.ReminderConfigRepository
import de.stroebele.mindyourself.domain.repository.ReminderStateRepository
import de.stroebele.mindyourself.domain.repository.SupplementRepository
import de.stroebele.mindyourself.domain.repository.VacationSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mindyourself.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides fun provideReminderConfigDao(db: AppDatabase): ReminderConfigDao = db.reminderConfigDao()
    @Provides fun provideReminderStateDao(db: AppDatabase): ReminderStateDao = db.reminderStateDao()
    @Provides fun provideHydrationDao(db: AppDatabase): HydrationDao = db.hydrationDao()
    @Provides fun provideSupplementDao(db: AppDatabase): SupplementDao = db.supplementDao()
    @Provides fun provideHealthCacheDao(db: AppDatabase): HealthCacheDao = db.healthCacheDao()
    @Provides fun provideNamedLocationDao(db: AppDatabase): NamedLocationDao = db.namedLocationDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindReminderConfigRepo(impl: ReminderConfigRepositoryImpl): ReminderConfigRepository
    @Binds abstract fun bindReminderStateRepo(impl: ReminderStateRepositoryImpl): ReminderStateRepository
    @Binds abstract fun bindHydrationRepo(impl: HydrationRepositoryImpl): HydrationRepository
    @Binds abstract fun bindSupplementRepo(impl: SupplementRepositoryImpl): SupplementRepository
    @Binds abstract fun bindHealthCacheRepo(impl: HealthCacheRepositoryImpl): HealthCacheRepository
    @Binds abstract fun bindNamedLocationRepo(impl: NamedLocationRepositoryImpl): NamedLocationRepository
    @Binds abstract fun bindVacationSettingsRepo(impl: VacationSettingsRepositoryImpl): VacationSettingsRepository
}
