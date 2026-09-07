package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Operador
import com.example.ui.components.BadgeChip

private val HeaderGreen = Color(0xFF2E7D32)
private val GridBorderColor = Color(0xFFCBD5E1)
private val DarkGridBorderColor = Color(0xFF334155)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanillaScreen(
    entries: List<AgendaEntry>,
    chicosCatalog: List<Chico>,
    operadoresCatalog: List<Operador>,
    onEditEntry: (AgendaEntry) -> Unit,
    onDuplicateEntry: (AgendaEntry) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onAddRowClick: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isDarkMode) DarkGridBorderColor else GridBorderColor
    val chicosColorMap = chicosCatalog.associate { it.name.uppercase() to Pair(it.colorHex, it.textColorHex) }
    val operadoresColorMap = operadoresCatalog.associate { it.name.uppercase() to Pair(it.colorHex, it.textColorHex) }

    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No hay actividades registradas para este día",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toca el botón para agregar una nueva fila a la planilla.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddRowClick,
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Agregar Fila", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        val horizontalScrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            // Excel-style sub-header (Columns labels #, A, B, C, D, E, F)
            Row(
                modifier = Modifier
                    .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    .border(0.5.dp, borderColor)
                    .height(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubHeaderCell("#", 44.dp, borderColor)
                SubHeaderCell("A", 84.dp, borderColor)
                SubHeaderCell("B", 140.dp, borderColor)
                SubHeaderCell("C", 115.dp, borderColor)
                SubHeaderCell("D", 120.dp, borderColor)
                SubHeaderCell("E", 120.dp, borderColor)
                SubHeaderCell("F", 190.dp, borderColor)
                SubHeaderCell("", 76.dp, borderColor)
            }

            // Green Header Row exactly as in the screenshot
            Row(
                modifier = Modifier
                    .background(HeaderGreen)
                    .border(0.5.dp, borderColor)
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell("#", 44.dp)
                HeaderCellWithSort("HORA", 84.dp)
                HeaderCell("NIÑA/O", 140.dp)
                HeaderCell("ACTIVIDAD", 125.dp)
                HeaderCell("LUGAR", 120.dp)
                HeaderCell("COORDI", 120.dp)
                HeaderCell("OBSERVACIONES", 190.dp)
                HeaderCell("ACCIONES", 76.dp)
            }

            // Table Data Rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, fill = false)
            ) {
                itemsIndexed(entries, key = { _, item -> item.id }) { index, item ->
                    val rowBg = if (index % 2 == 0) {
                        if (isDarkMode) Color(0xFF0F172A) else Color.White
                    } else {
                        if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    }

                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .background(rowBg)
                            .border(0.5.dp, borderColor)
                            .testTag("row_${item.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // # Index
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .fillMaxHeight()
                                .padding(vertical = 12.dp)
                                .border(0.5.dp, borderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // HORA (Column A) with dropdown pill look
                        Box(
                            modifier = Modifier
                                .width(84.dp)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .border(0.5.dp, borderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable { onEditEntry(item) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.time,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // NIÑA/NIÑO (Column B) colored badges - strictly 2 per line
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                .border(0.5.dp, borderColor)
                                .clickable { onEditEntry(item) },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val names = item.chicos.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                names.chunked(2).forEach { pair ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        pair.forEach { name ->
                                            val colors = chicosColorMap[name.uppercase()]
                                            val bg = colors?.first ?: "#3B82F6"
                                            BadgeChip(
                                                text = name,
                                                backgroundColorHex = bg,
                                                textColorHex = "#FFFFFF"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ACTIVIDAD (Column C) - Multi-line without truncation, always shows full text
                        Box(
                            modifier = Modifier
                                .width(125.dp)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .border(0.5.dp, borderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable { onEditEntry(item) }
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.actividad,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 15.sp,
                                    softWrap = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // LUGAR (Column D)
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .border(0.5.dp, borderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                    .clickable { onEditEntry(item) }
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.lugar,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 15.sp,
                                    softWrap = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // RESPONSABLES (Column E) - Always stacked one below the other
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                                .border(0.5.dp, borderColor)
                                .clickable { onEditEntry(item) },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val ops = item.responsables.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                ops.forEach { op ->
                                    val colors = operadoresColorMap[op.uppercase()]
                                    val bg = colors?.first ?: "#2563EB"
                                    val txt = colors?.second ?: "#FFFFFF"
                                    BadgeChip(
                                        text = op,
                                        backgroundColorHex = bg,
                                        textColorHex = txt
                                    )
                                }
                            }
                        }

                        // OBSERVACIONES (Column F)
                        Box(
                            modifier = Modifier
                                .width(190.dp)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .border(0.5.dp, borderColor)
                                .clickable { onEditEntry(item) },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = item.observaciones.ifBlank { "-" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }

                        // Actions (Duplicate, Delete)
                        Row(
                            modifier = Modifier
                                .width(76.dp)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .border(0.5.dp, borderColor),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onDuplicateEntry(item) },
                                modifier = Modifier.size(32.dp).testTag("duplicate_row_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Duplicar fila",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteEntry(item.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_row_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Eliminar fila",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(title: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeaderCellWithSort(title: String, width: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
        Icon(
            imageVector = Icons.Default.ArrowDropUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SubHeaderCell(label: String, width: androidx.compose.ui.unit.Dp, borderColor: Color) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .border(0.5.dp, borderColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
