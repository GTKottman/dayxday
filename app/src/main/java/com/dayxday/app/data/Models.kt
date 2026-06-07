package com.dayxday.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ValueKind {
    NUMBER,
    TEXT,
    BOOLEAN
}

@Entity(tableName = "data_types")
data class DataType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val kind: ValueKind,
    val unit: String = "",
    val colorArgb: Long = 0xFF2563EB
)

@Entity(tableName = "data_entries")
data class DataEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dataTypeId: Long,
    val epochDay: Long,
    val value: String
)

data class EntryWithType(
    val entry: DataEntry,
    val dataType: DataType
)

data class DaySummary(
    val epochDay: Long,
    val entryCount: Int
)

data class MonthlyStat(
    val dataType: DataType,
    val entryCount: Int,
    val numericAverage: Double?,
    val numericMin: Double?,
    val numericMax: Double?,
    val booleanTrueCount: Int,
    val booleanFalseCount: Int
)
