package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Actividad
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditEntryDialog(
    entry: AgendaEntry?,
    chicosCatalog: List<Chico>,
    actividadesCatalog: List<Actividad>,
    lugaresCatalog: List<Lugar>,
    operadoresCatalog: List<Operador>,
    onAddChico: (String) -> Unit = {},
    onAddActividad: (String) -> Unit = {},
    onAddLugar: (String) -> Unit = {},
    onAddOperador: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        time: String,
        chicos: String,
        actividad: String,
        lugar: String,
        responsables: String,
        observaciones: String
    ) -> Unit
) {
    val isNew = entry == null

    // Time parsing and dropdown state (Hours 00-23, Minutes 00, 05, ..., 55)
    val initialParts = (entry?.time ?: "08:00").split(":")
    val parsedInitialHour = initialParts.getOrNull(0)?.trim()?.padStart(2, '0') ?: "08"
    val parsedInitialMinRaw = initialParts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    val parsedInitialMinute = ((parsedInitialMinRaw / 5) * 5).coerceIn(0, 55).toString().padStart(2, '0')

    var selectedHour by remember(entry) { mutableStateOf(if (parsedInitialHour in (0..23).map { it.toString().padStart(2, '0') }) parsedInitialHour else "08") }
    var selectedMinute by remember(entry) { mutableStateOf(parsedInitialMinute) }
    var time by remember(entry) { mutableStateOf("$selectedHour:$selectedMinute") }

    var hourDropdownExpanded by remember { mutableStateOf(false) }
    var minuteDropdownExpanded by remember { mutableStateOf(false) }
    val hoursList = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutesList = remember { (0..55 step 5).map { it.toString().padStart(2, '0') } }

    var selectedChicos by remember {
        mutableStateOf(
            entry?.chicos?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        )
    }
    var selectedActividad by remember { mutableStateOf(entry?.actividad ?: (actividadesCatalog.firstOrNull()?.name ?: "DESAYUNO")) }
    var customActividad by remember { mutableStateOf("") }
    var isCustomActividad by remember { mutableStateOf(false) }

    var selectedLugar by remember { mutableStateOf(entry?.lugar ?: (lugaresCatalog.firstOrNull()?.name ?: "HOGAR")) }
    var customLugar by remember { mutableStateOf("") }
    var isCustomLugar by remember { mutableStateOf(false) }

    var selectedResponsables by remember {
        mutableStateOf(
            entry?.responsables?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        )
    }
    var observaciones by remember { mutableStateOf(entry?.observaciones ?: "") }

    // Quick Add dialog state for items in catalogs
    var quickAddCatalogType by remember { mutableStateOf<String?>(null) }
    var quickAddName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "➕ Nueva Fila en Planilla" else "✏️ Editar Fila (${entry?.time})",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HORA: Two dropdown menus for hour (00-23) and minutes (intervals of 5 min from 00)
                Column {
                    Text("Configurar Hora:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown 1: HORAS (00 a 23)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { hourDropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("dropdown_hour_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$selectedHour hs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar horas"
                                )
                            }

                            DropdownMenu(
                                expanded = hourDropdownExpanded,
                                onDismissRequest = { hourDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                hoursList.forEach { h ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$h hs",
                                                fontWeight = if (h == selectedHour) FontWeight.Black else FontWeight.Normal,
                                                color = if (h == selectedHour) MaterialTheme.colorScheme.primary else Color.Unspecified
                                            )
                                        },
                                        onClick = {
                                            selectedHour = h
                                            time = "$selectedHour:$selectedMinute"
                                            hourDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown 2: MINUTOS (intervalos de a 5 min: 00, 05, 10, ..., 55)
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { minuteDropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("dropdown_minute_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$selectedMinute min",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar minutos"
                                )
                            }

                            DropdownMenu(
                                expanded = minuteDropdownExpanded,
                                onDismissRequest = { minuteDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                minutesList.forEach { m ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$m min",
                                                fontWeight = if (m == selectedMinute) FontWeight.Black else FontWeight.Normal,
                                                color = if (m == selectedMinute) MaterialTheme.colorScheme.primary else Color.Unspecified
                                            )
                                        },
                                        onClick = {
                                            selectedMinute = m
                                            time = "$selectedHour:$selectedMinute"
                                            minuteDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Hora fijada: ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$time hs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // NIÑAS / NIÑOS (Multi-select) con botón "+" para agregar nuevo item base
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Niña / Niño (${selectedChicos.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    quickAddCatalogType = "chico"
                                    quickAddName = ""
                                },
                                modifier = Modifier.size(26.dp).testTag("add_chico_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Agregar niño/a a la lista base",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Text(
                            text = if (selectedChicos.contains("TODOS")) "Desmarcar Todos" else "Marcar TODOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                selectedChicos = if (selectedChicos.contains("TODOS")) {
                                    emptyList()
                                } else {
                                    listOf("TODOS")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chicosCatalog.forEach { chico ->
                            val isSelected = selectedChicos.contains(chico.name.uppercase())
                            val chipBg = parseHexColor(chico.colorHex, Color(0xFF3B82F6))
                            val chipText = Color.White

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(chipBg)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.5.dp,
                                        color = if (isSelected) Color.Black else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        val nameUpper = chico.name.uppercase()
                                        selectedChicos = if (isSelected) {
                                            selectedChicos - nameUpper
                                        } else {
                                            (selectedChicos - "TODOS") + nameUpper
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = chipText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = chico.name,
                                        color = chipText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ACTIVIDAD con botón "+" para agregar nuevo item base
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Actividad:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                quickAddCatalogType = "actividad"
                                quickAddName = ""
                            },
                            modifier = Modifier.size(26.dp).testTag("add_actividad_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Agregar actividad a la lista base",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        actividadesCatalog.forEach { act ->
                            val isSelected = !isCustomActividad && selectedActividad == act.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedActividad = act.name
                                        isCustomActividad = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = act.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // LUGAR con botón "+" para agregar nuevo item base
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lugar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                quickAddCatalogType = "lugar"
                                quickAddName = ""
                            },
                            modifier = Modifier.size(26.dp).testTag("add_lugar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Agregar lugar a la lista base",
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        lugaresCatalog.forEach { lug ->
                            val isSelected = !isCustomLugar && selectedLugar == lug.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedLugar = lug.name
                                        isCustomLugar = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lug.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // RESPONSABLES (COORDI / Operadores multi-select) con botón "+" para agregar nuevo item base
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Coordi / Operadores Responsables:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                quickAddCatalogType = "coordi"
                                quickAddName = ""
                            },
                            modifier = Modifier.size(26.dp).testTag("add_operador_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Agregar coordi a la lista base",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        operadoresCatalog.forEach { op ->
                            val isSelected = selectedResponsables.contains(op.name.uppercase())
                            val chipBg = parseHexColor(op.colorHex, Color(0xFF2563EB))
                            val chipText = parseHexColor(op.textColorHex, Color.White)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(chipBg)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.Black else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        val nameUpper = op.name.uppercase()
                                        selectedResponsables = if (isSelected) {
                                            selectedResponsables - nameUpper
                                        } else {
                                            selectedResponsables + nameUpper
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = chipText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = op.name,
                                        color = chipText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // OBSERVACIONES
                Column {
                    Text("Observaciones / Notas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        modifier = Modifier.fillMaxWidth().testTag("entry_obs_input"),
                        placeholder = { Text("Ej: Medicación, llevar vianda, sesión...") },
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalActividad = if (isCustomActividad && customActividad.isNotBlank()) customActividad else selectedActividad
                    val finalLugar = if (isCustomLugar && customLugar.isNotBlank()) customLugar else selectedLugar
                    val finalChicos = if (selectedChicos.isEmpty()) "TODOS" else selectedChicos.joinToString(", ")
                    val finalResponsables = if (selectedResponsables.isEmpty()) "A DEFINIR" else selectedResponsables.joinToString(", ")

                    onSave(
                        entry?.id ?: 0L,
                        time,
                        finalChicos,
                        finalActividad,
                        finalLugar,
                        finalResponsables,
                        observaciones
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier.testTag("save_entry_button")
            ) {
                Text("Guardar Fila", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Quick Add dialog for base lists (+ button)
    if (quickAddCatalogType != null) {
        val type = quickAddCatalogType ?: ""
        val dialogTitle = when (type) {
            "chico" -> "➕ Agregar Niño / Niña a la base"
            "actividad" -> "➕ Agregar Actividad a la base"
            "lugar" -> "➕ Agregar Lugar a la base"
            else -> "➕ Agregar Coordi / Operador a la base"
        }
        val placeholder = when (type) {
            "chico" -> "Nombre del niño/a"
            "actividad" -> "Nombre de la actividad"
            "lugar" -> "Nombre del lugar"
            else -> "Nombre del coordi / operador"
        }

        AlertDialog(
            onDismissRequest = { quickAddCatalogType = null },
            title = {
                Text(text = dialogTitle, fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Se agregará al listado base para usarlo siempre en las planillas:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quickAddName,
                        onValueChange = { quickAddName = it },
                        label = { Text("Nombre") },
                        placeholder = { Text(placeholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = quickAddName.trim().uppercase()
                        if (cleanName.isNotBlank()) {
                            when (type) {
                                "chico" -> {
                                    onAddChico(cleanName)
                                    selectedChicos = (selectedChicos - "TODOS") + cleanName
                                }
                                "actividad" -> {
                                    onAddActividad(cleanName)
                                    selectedActividad = cleanName
                                    isCustomActividad = false
                                }
                                "lugar" -> {
                                    onAddLugar(cleanName)
                                    selectedLugar = cleanName
                                    isCustomLugar = false
                                }
                                "coordi" -> {
                                    onAddOperador(cleanName)
                                    selectedResponsables = selectedResponsables + cleanName
                                }
                            }
                            quickAddCatalogType = null
                            quickAddName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Agregar a la Base", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { quickAddCatalogType = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
