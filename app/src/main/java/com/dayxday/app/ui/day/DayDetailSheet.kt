package com.dayxday.app.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dayxday.app.TrackerViewModel
import com.dayxday.app.data.DataEntry
import com.dayxday.app.data.DataType
import com.dayxday.app.data.EntryWithType
import com.dayxday.app.data.ValueKind
import com.dayxday.app.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(viewModel: TrackerViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val entries by viewModel.dayEntries.collectAsState()
    val dataTypes by viewModel.dataTypes.collectAsState()

    if (selectedDate == null) return

    val date = selectedDate!!
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember(date) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = viewModel::closeDay,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = date.format(DayFormatter),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (dataTypes.isEmpty()) {
                Text(
                    text = "Create a data type first to start tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (entries.isEmpty()) {
                Text(
                    text = "No entries yet. Tap + to add data for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entries.forEach { entryWithType ->
                        EntryRow(
                            entryWithType = entryWithType,
                            onDelete = { viewModel.deleteEntry(entryWithType.entry) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add entry")
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            date = date,
            dataTypes = dataTypes,
            existingEntries = entries,
            onDismiss = { showAddDialog = false },
            onSave = { dataTypeId, value ->
                viewModel.saveEntry(dataTypeId, date, value)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EntryRow(
    entryWithType: EntryWithType,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(entryWithType.dataType.colorArgb.toInt()))
        )
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = entryWithType.dataType.name,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = formatDisplayValue(entryWithType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete entry")
        }
    }
}

private fun formatDisplayValue(entryWithType: EntryWithType): String {
    return when (entryWithType.dataType.kind) {
        ValueKind.NUMBER -> {
            val unit = entryWithType.dataType.unit
            if (unit.isBlank()) entryWithType.entry.value else "${entryWithType.entry.value} $unit"
        }
        ValueKind.BOOLEAN -> if (entryWithType.entry.value.equals("true", ignoreCase = true)) "Yes" else "No"
        ValueKind.TEXT -> entryWithType.entry.value
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryDialog(
    date: LocalDate,
    dataTypes: List<DataType>,
    existingEntries: List<EntryWithType>,
    onDismiss: () -> Unit,
    onSave: (dataTypeId: Long, value: String) -> Unit
) {
    val availableTypes = dataTypes.filter { type ->
        existingEntries.none { it.dataType.id == type.id }
    }
    var selectedType by remember(availableTypes) { mutableStateOf(availableTypes.firstOrNull()) }
    var textValue by remember { mutableStateOf("") }
    var numberValue by remember { mutableStateOf("") }
    var boolValue by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add entry for ${date.dayOfMonth}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (availableTypes.isEmpty()) {
                    Text("All data types already have entries for this day.")
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedType?.name.orEmpty(),
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
                            availableTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    when (selectedType?.kind) {
                        ValueKind.NUMBER -> {
                            OutlinedTextField(
                                value = numberValue,
                                onValueChange = { numberValue = it },
                                label = {
                                    Text(
                                        if (selectedType?.unit.isNullOrBlank()) "Value"
                                        else "Value (${selectedType?.unit})"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        ValueKind.TEXT -> {
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { textValue = it },
                                label = { Text("Notes") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ValueKind.BOOLEAN -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = boolValue, onCheckedChange = { boolValue = it })
                                Text("Yes")
                            }
                        }
                        null -> Unit
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val type = selectedType ?: return@Button
                    val value = when (type.kind) {
                        ValueKind.NUMBER -> numberValue.trim()
                        ValueKind.TEXT -> textValue.trim()
                        ValueKind.BOOLEAN -> boolValue.toString()
                    }
                    if (value.isNotBlank() || type.kind == ValueKind.BOOLEAN) {
                        onSave(type.id, value)
                    }
                },
                enabled = availableTypes.isNotEmpty() && selectedType != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
