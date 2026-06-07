package com.dayxday.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dayxday.app.data.AppDatabase
import com.dayxday.app.data.DataEntry
import com.dayxday.app.data.DataType
import com.dayxday.app.data.EntryWithType
import com.dayxday.app.data.MonthlyStat
import com.dayxday.app.data.TrackerRepository
import com.dayxday.app.data.TrendPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TrackerRepository(
        AppDatabase.get(application).dataTypeDao(),
        AppDatabase.get(application).dataEntryDao()
    )

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _statsMonth = MutableStateFlow(YearMonth.now())
    val statsMonth: StateFlow<YearMonth> = _statsMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _monthlyStats = MutableStateFlow<List<MonthlyStat>>(emptyList())
    val monthlyStats: StateFlow<List<MonthlyStat>> = _monthlyStats.asStateFlow()

    private val _trendPoints = MutableStateFlow<List<TrendPoint>>(emptyList())
    val trendPoints: StateFlow<List<TrendPoint>> = _trendPoints.asStateFlow()

    private val _selectedTrendTypeId = MutableStateFlow<Long?>(null)
    val selectedTrendTypeId: StateFlow<Long?> = _selectedTrendTypeId.asStateFlow()

    val dataTypes = repository.observeDataTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarDaySummaries = _selectedMonth
        .flatMapLatest { month -> repository.observeDaySummaries(month) }
        .map { summaries -> summaries.associate { it.epochDay to it.entryCount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dayEntries: StateFlow<List<EntryWithType>> = _selectedDate
        .flatMapLatest { date ->
            if (date == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.observeEntriesForDay(date)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshStats()
    }

    fun setSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun setStatsMonth(month: YearMonth) {
        _statsMonth.value = month
        refreshStats()
    }

    fun previousStatsMonth() {
        _statsMonth.value = _statsMonth.value.minusMonths(1)
        refreshStats()
    }

    fun nextStatsMonth() {
        _statsMonth.value = _statsMonth.value.plusMonths(1)
        refreshStats()
    }

    fun openDay(date: LocalDate) {
        _selectedDate.value = date
    }

    fun closeDay() {
        _selectedDate.value = null
    }

    fun saveDataType(dataType: DataType) {
        viewModelScope.launch {
            repository.upsertDataType(dataType)
            refreshStats()
            refreshTrend()
        }
    }

    fun deleteDataType(dataType: DataType) {
        viewModelScope.launch {
            repository.deleteDataType(dataType)
            if (_selectedTrendTypeId.value == dataType.id) {
                _selectedTrendTypeId.value = null
                _trendPoints.value = emptyList()
            }
            refreshStats()
        }
    }

    fun saveEntry(dataTypeId: Long, date: LocalDate, value: String, existingId: Long = 0) {
        viewModelScope.launch {
            repository.upsertEntry(
                DataEntry(
                    id = existingId,
                    dataTypeId = dataTypeId,
                    epochDay = date.toEpochDay(),
                    value = value
                )
            )
            refreshStats()
            refreshTrend()
        }
    }

    fun deleteEntry(entry: DataEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            refreshStats()
            refreshTrend()
        }
    }

    fun selectTrendType(dataTypeId: Long?) {
        _selectedTrendTypeId.value = dataTypeId
        refreshTrend()
    }

    private fun refreshStats() {
        viewModelScope.launch {
            _monthlyStats.value = repository.getMonthlyStats(_statsMonth.value)
        }
    }

    private fun refreshTrend() {
        viewModelScope.launch {
            val typeId = _selectedTrendTypeId.value
            _trendPoints.value = if (typeId == null) {
                emptyList()
            } else {
                repository.getTrendPoints(typeId)
            }
        }
    }
}
