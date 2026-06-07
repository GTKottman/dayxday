package com.dayxday.app.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayxday.app.TrackerViewModel
import com.dayxday.app.data.MonthlyStat
import com.dayxday.app.data.TrendPoint
import com.dayxday.app.data.ValueKind
import com.dayxday.app.ui.theme.toComposeColor
import java.time.format.DateTimeFormatter
import java.time.YearMonth
private val MonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun StatsScreen(viewModel: TrackerViewModel) {
    val statsMonth by viewModel.statsMonth.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val dataTypes by viewModel.dataTypes.collectAsState()
    val trendPoints by viewModel.trendPoints.collectAsState()
    val selectedTrendTypeId by viewModel.selectedTrendTypeId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        MonthHeader(
            month = statsMonth,
            onPrevious = viewModel::previousStatsMonth,
            onNext = viewModel::nextStatsMonth
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Monthly summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (monthlyStats.isEmpty() || monthlyStats.all { it.entryCount == 0 }) {
            Text(
                text = "No data recorded this month. Add entries from the calendar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            monthlyStats.filter { it.entryCount > 0 }.forEach { stat ->
                StatCard(stat)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Trend over time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (dataTypes.isEmpty()) {
            Text(
                text = "Create data types to see trends.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TrendTypeSelector(
                dataTypes = dataTypes,
                selectedId = selectedTrendTypeId,
                onSelect = viewModel::selectTrendType
            )
            Spacer(modifier = Modifier.height(12.dp))

            val selectedType = dataTypes.find { it.id == selectedTrendTypeId }
            if (selectedType == null) {
                Text(
                    text = "Select a data type to view its trend.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (trendPoints.isEmpty()) {
                Text(
                    text = "No entries yet for ${selectedType.name}.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TrendChart(
                    points = trendPoints,
                    kind = selectedType.kind,
                    color = selectedType.colorArgb.toComposeColor(),
                    unit = selectedType.unit
                )
                Spacer(modifier = Modifier.height(8.dp))
                trendPoints.takeLast(10).reversed().forEach { point ->
                    Text(
                        text = "${point.date}: ${formatTrendValue(point, selectedType.kind, selectedType.unit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(MonthFormatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun StatCard(stat: MonthlyStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = stat.dataType.colorArgb.toComposeColor().copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stat.dataType.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = stat.dataType.colorArgb.toComposeColor()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${stat.entryCount} entries this month")
            when (stat.dataType.kind) {
                ValueKind.NUMBER -> {
                    stat.numericAverage?.let {
                        Text("Average: ${formatNumber(it)} ${stat.dataType.unit}".trim())
                    }
                    if (stat.numericMin != null && stat.numericMax != null) {
                        Text("Range: ${formatNumber(stat.numericMin)} - ${formatNumber(stat.numericMax)} ${stat.dataType.unit}".trim())
                    }
                }
                ValueKind.BOOLEAN -> {
                    Text("Yes: ${stat.booleanTrueCount}, No: ${stat.booleanFalseCount}")
                }
                ValueKind.TEXT -> {
                    Text("Text entries logged")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendTypeSelector(
    dataTypes: List<com.dayxday.app.data.DataType>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = dataTypes.find { it.id == selectedId }?.name.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Data type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dataTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelect(type.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TrendChart(
    points: List<TrendPoint>,
    kind: ValueKind,
    color: Color,
    unit: String
) {
    if (kind == ValueKind.TEXT) {
        Text(
            text = "Text data is listed below instead of charted.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val numericPoints = points.map { it.numericValue }
    val minValue = numericPoints.minOrNull() ?: 0.0
    val maxValue = numericPoints.maxOrNull() ?: 1.0
    val valueRange = kotlin.math.max(maxValue - minValue, 0.001)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = if (points.size <= 1) 0f else width / (points.size - 1)

                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = if (points.size <= 1) width / 2f else index * stepX
                    val normalized = ((point.numericValue - minValue) / valueRange).toFloat()
                    val y = height - (normalized * (height * 0.8f)) - height * 0.1f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4f)
                )

                points.forEachIndexed { index, point ->
                    val x = if (points.size <= 1) width / 2f else index * stepX
                    val normalized = ((point.numericValue - minValue) / valueRange).toFloat()
                    val y = height - (normalized * (height * 0.8f)) - height * 0.1f
                    drawCircle(color = color, radius = 6f, center = Offset(x, y))
                }
            }
            Text(
                text = "Min: ${formatNumber(minValue)}  Max: ${formatNumber(maxValue)} ${unit}".trim(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.1f".format(value)
    }
}

private fun formatTrendValue(point: TrendPoint, kind: ValueKind, unit: String): String {
    return when (kind) {
        ValueKind.NUMBER -> "${formatNumber(point.numericValue)} $unit".trim()
        ValueKind.BOOLEAN -> if (point.numericValue >= 0.5) "Yes" else "No"
        ValueKind.TEXT -> point.displayValue
    }
}
