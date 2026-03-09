package de.stroebele.mindyourself.domain.repository

import de.stroebele.mindyourself.domain.model.VacationSettings
import kotlinx.coroutines.flow.Flow

interface VacationSettingsRepository {
    fun observe(): Flow<VacationSettings>
    suspend fun save(settings: VacationSettings)
}
