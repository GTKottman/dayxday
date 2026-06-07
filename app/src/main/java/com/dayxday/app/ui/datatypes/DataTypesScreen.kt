package com.dayxday.app.ui.datatypes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayxday.app.TrackerViewModel
import com.dayxday.app.data.DataType
import com.dayxday.app.data.ValueKind
import com.dayxday.app.ui.theme.PresetColors
import com.dayxday.app.ui.theme.toArgbLong
import com.dayxday.app.ui.theme.toComposeColor

@Composable
fun DataTypesScreen(viewModel: TrackerViewModel) {
    val dataTypes by viewModel.dataTypes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<DataType?>(null) }
    var deletingType by remember { mutableStateOf<DataType?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (dataTypes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No data types yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create types like Weight, Mood, or Sleep to track on your calendar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dataTypes, key = { it.id }) { dataType ->
                    DataTypeCard(
                        dataType = dataType,
                        onEdit = {
                            editingType = dataType
                            showDialog = true
                        },
                        onDelete = { deletingType = dataType }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingType = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add data type")
        }
    }

    if (showDialog) {
        DataTypeDialog(
            existing = editingType,
            onDismiss = {
                showDialog = false
                editingType = null
            },
            onSave = { dataType ->
                viewModel.saveDataType(dataType)
                showDialog = false
                editingType = null
            }
        )
    }

    deletingType?.let { type ->
        AlertDialog(
            onDismissRequest = { deletingType = null },
            title = { Text("Delete ${type.name}?") },
            text = { Text("This will also delete all entries for this data type.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDataType(type)
                        deletingType = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingType = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DataTypeCard(
    dataType: DataType,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dataType.colorArgb.toComposeColor())
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = dataType.name, fontWeight = FontWeight.Bold)
                Text(
                    text = kindLabel(dataType.kind) +
                        if (dataType.unit.isNotBlank()) " (${dataType.unit})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataTypeDialog(
    existing: DataType?,
    onDismiss: () -> Unit,
    onSave: (DataType) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var kind by remember(existing) { mutableStateOf(existing?.kind ?: ValueKind.NUMBER) }
    var unit by remember(existing) { mutableStateOf(existing?.unit.orEmpty()) }
    var selectedColor by remember(existing) {
        mutableStateOf(existing?.colorArgb?.toComposeColor() ?: PresetColors.first())
    }
    var kindExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New data type" else "Edit data type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = kindExpanded,
                    onExpandedChange = { kindExpanded = it }
                ) {
                    OutlinedTextField(
                        value = kindLabel(kind),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(kindExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = kindExpanded,
                        onDismissRequest = { kindExpanded = false }
                    ) {
                        ValueKind.entries.forEach { valueKind ->
                            DropdownMenuItem(
                                text = { Text(kindLabel(valueKind)) },
                                onClick = {
                                    kind = valueKind
                                    kindExpanded = false
                                }
                            )
                        }
                    }
                }

                if (kind == ValueKind.NUMBER) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("lbs, hours, etc.") }
                    )
                }

                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (color == selectedColor) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            DataType(
                                id = existing?.id ?: 0,
                                name = name.trim(),
                                kind = kind,
                                unit = if (kind == ValueKind.NUMBER) unit.trim() else "",
                                colorArgb = selectedColor.toArgbLong()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
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

private fun kindLabel(kind: ValueKind): String = when (kind) {
    ValueKind.NUMBER -> "Number"
    ValueKind.TEXT -> "Text"
    ValueKind.BOOLEAN -> "Yes / No"
}
