package com.dayxday.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DataTypeDao {
    @Query("SELECT * FROM data_types ORDER BY name ASC")
    fun observeAll(): Flow<List<DataType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dataType: DataType): Long

    @Update
    suspend fun update(dataType: DataType)

    @Delete
    suspend fun delete(dataType: DataType)
}

@Dao
interface DataEntryDao {
    @Query(
        """
        SELECT e.id AS entry_id, e.dataTypeId AS entry_dataTypeId,
               e.epochDay AS entry_epochDay, e.value AS entry_value,
               t.id AS type_id, t.name AS type_name, t.kind AS type_kind,
               t.unit AS type_unit, t.colorArgb AS type_colorArgb
        FROM data_entries e
        INNER JOIN data_types t ON t.id = e.dataTypeId
        WHERE e.epochDay = :epochDay
        ORDER BY t.name ASC
        """
    )
    fun observeForDay(epochDay: Long): Flow<List<EntryWithTypeRow>>

    @Query(
        """
        SELECT e.epochDay AS epochDay, COUNT(*) AS entryCount
        FROM data_entries e
        WHERE e.epochDay BETWEEN :startDay AND :endDay
        GROUP BY e.epochDay
        """
    )
    fun observeDaySummaries(startDay: Long, endDay: Long): Flow<List<DaySummary>>

    @Query(
        """
        SELECT e.id AS entry_id, e.dataTypeId AS entry_dataTypeId,
               e.epochDay AS entry_epochDay, e.value AS entry_value,
               t.id AS type_id, t.name AS type_name, t.kind AS type_kind,
               t.unit AS type_unit, t.colorArgb AS type_colorArgb
        FROM data_entries e
        INNER JOIN data_types t ON t.id = e.dataTypeId
        WHERE e.epochDay BETWEEN :startDay AND :endDay
        ORDER BY e.epochDay ASC, t.name ASC
        """
    )
    fun observeInRange(startDay: Long, endDay: Long): Flow<List<EntryWithTypeRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DataEntry): Long

    @Update
    suspend fun update(entry: DataEntry)

    @Delete
    suspend fun delete(entry: DataEntry)

    @Query("DELETE FROM data_entries WHERE dataTypeId = :dataTypeId")
    suspend fun deleteByDataType(dataTypeId: Long)
}

data class EntryWithTypeRow(
    val entry_id: Long,
    val entry_dataTypeId: Long,
    val entry_epochDay: Long,
    val entry_value: String,
    val type_id: Long,
    val type_name: String,
    val type_kind: ValueKind,
    val type_unit: String,
    val type_colorArgb: Long
) {
    fun toEntryWithType(): EntryWithType = EntryWithType(
        entry = DataEntry(
            id = entry_id,
            dataTypeId = entry_dataTypeId,
            epochDay = entry_epochDay,
            value = entry_value
        ),
        dataType = DataType(
            id = type_id,
            name = type_name,
            kind = type_kind,
            unit = type_unit,
            colorArgb = type_colorArgb
        )
    )
}
