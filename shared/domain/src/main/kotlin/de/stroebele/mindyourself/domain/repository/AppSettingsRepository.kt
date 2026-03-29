package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun save(settings: AppSettings)
}
