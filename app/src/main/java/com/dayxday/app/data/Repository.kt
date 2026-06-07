package com.dayxday.app.data

import androidx.room.Embedded
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class TrackerRepository(
    private val dataTypeDao: DataTypeDao,
    private val dataEntryDao: DataEntryDao
) {
    fun observeDataTypes(): Flow<List<DataType>> = dataTypeDao.observeAll()

    fun observeEntriesForDay(date: LocalDate): Flow<List<EntryWithType>> {
        return dataEntryDao.observeForDay(date.toEpochDay()).map { rows ->
            rows.map { it.toEntryWithType() }
        }
    }

    fun observeDaySummaries(month: YearMonth): Flow<List<DaySummary>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return dataEntryDao.observeDaySummaries(start, end)
    }

    fun observeEntriesInMonth(month: YearMonth): Flow<List<EntryWithType>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return dataEntryDao.observeInRange(start, end).map { rows ->
            rows.map { it.toEntryWithType() }
        }
    }

    suspend fun upsertDataType(dataType: DataType): Long {
        return if (dataType.id == 0L) {
            dataTypeDao.insert(dataType)
        } else {
            dataTypeDao.update(dataType)
            dataType.id
        }
    }

    suspend fun deleteDataType(dataType: DataType) {
        dataEntryDao.deleteByDataType(dataType.id)
        dataTypeDao.delete(dataType)
    }

    suspend fun upsertEntry(entry: DataEntry): Long {
        return if (entry.id == 0L) {
            dataEntryDao.insert(entry)
        } else {
            dataEntryDao.update(entry)
            entry.id
        }
    }

    suspend fun deleteEntry(entry: DataEntry) {
        dataEntryDao.delete(entry)
    }

    suspend fun getMonthlyStats(month: YearMonth): List<MonthlyStat> {
        val dataTypes = dataTypeDao.observeAll().first()
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        val entries = dataEntryDao.observeInRange(start, end).first().map { it.toEntryWithType() }
        val byType = entries.groupBy { it.dataType.id }

        return dataTypes.map { dataType ->
            val typeEntries = byType[dataType.id].orEmpty()
            when (dataType.kind) {
                ValueKind.NUMBER -> {
                    val numbers = typeEntries.mapNotNull { it.entry.value.toDoubleOrNull() }
                    MonthlyStat(
                        dataType = dataType,
                        entryCount = typeEntries.size,
                        numericAverage = numbers.takeIf { it.isNotEmpty() }?.average(),
                        numericMin = numbers.minOrNull(),
                        numericMax = numbers.maxOrNull(),
                        booleanTrueCount = 0,
                        booleanFalseCount = 0
                    )
                }
                ValueKind.BOOLEAN -> {
                    val trueCount = typeEntries.count { it.entry.value.equals("true", ignoreCase = true) }
                    MonthlyStat(
                        dataType = dataType,
                        entryCount = typeEntries.size,
                        numericAverage = null,
                        numericMin = null,
                        numericMax = null,
                        booleanTrueCount = trueCount,
                        booleanFalseCount = typeEntries.size - trueCount
                    )
                }
                ValueKind.TEXT -> {
                    MonthlyStat(
                        dataType = dataType,
                        entryCount = typeEntries.size,
                        numericAverage = null,
                        numericMin = null,
                        numericMax = null,
                        booleanTrueCount = 0,
                        booleanFalseCount = 0
                    )
                }
            }
        }
    }

    suspend fun getTrendPoints(dataTypeId: Long, monthsBack: Int = 6): List<TrendPoint> {
        val end = YearMonth.now()
        val start = end.minusMonths(monthsBack.toLong() - 1)
        val startDay = start.atDay(1).toEpochDay()
        val endDay = end.atEndOfMonth().toEpochDay()
        val entries = dataEntryDao.observeInRange(startDay, endDay).first()
            .map { it.toEntryWithType() }
            .filter { it.dataType.id == dataTypeId }

        return entries.mapNotNull { entryWithType ->
            val date = LocalDate.ofEpochDay(entryWithType.entry.epochDay)
            val month = YearMonth.from(date)
            when (entryWithType.dataType.kind) {
                ValueKind.NUMBER -> entryWithType.entry.value.toDoubleOrNull()?.let {
                    TrendPoint(month, date, it, entryWithType.entry.value)
                }
                ValueKind.BOOLEAN -> TrendPoint(
                    month,
                    date,
                    if (entryWithType.entry.value.equals("true", ignoreCase = true)) 1.0 else 0.0,
                    entryWithType.entry.value
                )
                ValueKind.TEXT -> TrendPoint(month, date, 0.0, entryWithType.entry.value)
            }
        }.sortedBy { it.date }
    }
}

data class TrendPoint(
    val month: YearMonth,
    val date: LocalDate,
    val numericValue: Double,
    val displayValue: String
)
