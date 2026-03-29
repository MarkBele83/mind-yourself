package de.stroebele.mindyourself.domain.model

data class AppSettings(
    val stepDailyGoal: Int = 10_000,
    val hydrationDailyGoalMl: Int = 0,
)
