package de.stroebele.mindyourself.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.stroebele.mindyourself.domain.model.AppSettings
import de.stroebele.mindyourself.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppSettingsRepository {

    private val stepDailyGoalKey = intPreferencesKey("step_daily_goal")
    private val hydrationDailyGoalMlKey = intPreferencesKey("hydration_daily_goal_ml")

    override fun observe(): Flow<AppSettings> =
        context.appSettingsDataStore.data.map { prefs ->
            AppSettings(
                stepDailyGoal = prefs[stepDailyGoalKey] ?: AppSettings().stepDailyGoal,
                hydrationDailyGoalMl = prefs[hydrationDailyGoalMlKey] ?: 0,
            )
        }

    override suspend fun save(settings: AppSettings) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[stepDailyGoalKey] = settings.stepDailyGoal
            prefs[hydrationDailyGoalMlKey] = settings.hydrationDailyGoalMl
        }
    }
}
