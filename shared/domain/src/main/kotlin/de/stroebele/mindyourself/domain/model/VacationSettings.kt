package de.stroebele.mindyourself.domain.model

import java.time.LocalDateTime

data class VacationPeriod(
    val from: LocalDateTime,
    val until: LocalDateTime,
)

data class VacationSettings(
    val periods: List<VacationPeriod> = emptyList(),
)
