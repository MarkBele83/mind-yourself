package de.stroebele.mindyourself.domain.model

data class LocationImportResult(
    val addedCount: Int,
    val updatedCount: Int,
    val errorCount: Int,
)
