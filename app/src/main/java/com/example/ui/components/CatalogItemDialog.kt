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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador

private val availableColors = listOf(
    "#2563EB", // royal blue (Aaron)
    "#0D9488", // teal (Dami)
    "#1E40AF", // deep indigo (Marcos)
    "#7C3AED", // violet (Lele)
    "#E11D48", // rose (Gaby)
    "#059669", // emerald (Denis)
    "#C2410C", // dark orange (Luis H)
    "#B45309", // amber (Luis G)
    "#0F766E", // pine (Diego)
    "#DB2777", // deep pink (Barbie)
    "#9333EA", // purple (Meli)
    "#4F46E5", // indigo (Ari)
    "#DC2626", // red (Rox)
    "#475569", // slate (Elizabeth)
    "#0891B2", // cyan (Eliana)
    "#854D0E", // bronze (Dolores)
    "#6B21A8", // dark violet (Bettina)
    "#D97706", // golden (Mica)
    "#BE185D", // ruby (Lara)
    "#16A34A", // green (Ale)
    "#0284C7", // sky blue (Cande)
    "#9D174D", // wine (Karen)
    "#65A30D", // lime (Laura R)
    "#0369A1", // cerulean (Laura C)
    "#A21CAF", // magenta (Natalia)
    "#374151"  // dark slate (Silvia B)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogItemDialog(
    type: String, // "chico", "actividad", "lugar", "operador"
    editingChico: Chico?,
    editingActividad: Actividad?,
    editingLugar: Lugar?,
    editingOperador: Operador?,
    onDismiss: () -> Unit,
    onSaveChico: (name: String, colorHex: String, textColorHex: String) -> Unit,
    onSaveActividad: (name: String, colorHex: String) -> Unit,
    onSaveLugar: (name: String) -> Unit,
    onSaveOperador: (name: String, colorHex: String) -> Unit
) {
    var name by remember {
        mutableStateOf(
            when (type) {
                "chico" -> editingChico?.name ?: ""
                "actividad" -> editingActividad?.name ?: ""
                "lugar" -> editingLugar?.name ?: ""
                "operador" -> editingOperador?.name ?: ""
                else -> ""
            }
        )
    }

    var selectedColor by remember {
        mutableStateOf(
            when (type) {
                "chico" -> editingChico?.colorHex ?: availableColors[0]
                "actividad" -> editingActividad?.colorHex ?: availableColors[3]
                "operador" -> editingOperador?.colorHex ?: availableColors[11]
                else -> availableColors[0]
            }
        )
    }

    val title = when (type) {
        "chico" -> if (editingChico != null) "Editar Niño/a" else "Nuevo Niño/a"
        "actividad" -> "Nueva Actividad"
        "lugar" -> "Nuevo Lugar"
        "operador" -> if (editingOperador != null) "Editar Operador" else "Nuevo Operador"
        else -> "Nuevo Elemento"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Escribe aquí...") },
                    modifier = Modifier.fillMaxWidth().testTag("catalog_name_input"),
                    singleLine = true
                )

                if (type == "chico" || type == "operador" || type == "actividad") {
                    Column {
                        Text(
                            text = "Color de etiqueta:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text("Vista previa: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            BadgeChip(
                                text = name.ifBlank { "NOMBRE" },
                                backgroundColorHex = selectedColor,
                                textColorHex = if (selectedColor == "#EAB308" || selectedColor == "#84CC16" || selectedColor == "#22C55E" || selectedColor == "#06B6D4") "#000000" else "#FFFFFF"
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableColors.forEach { colorHex ->
                                val isSelected = selectedColor == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(colorHex, Color.Gray))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = colorHex }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val textColorHex = if (type == "chico") {
                            "#FFFFFF"
                        } else if (selectedColor == "#EAB308" || selectedColor == "#84CC16" || selectedColor == "#22C55E" || selectedColor == "#06B6D4") {
                            "#000000"
                        } else {
                            "#FFFFFF"
                        }
                        when (type) {
                            "chico" -> onSaveChico(name, selectedColor, textColorHex)
                            "actividad" -> onSaveActividad(name, selectedColor)
                            "lugar" -> onSaveLugar(name)
                            "operador" -> onSaveOperador(name, selectedColor)
                        }
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                modifier = Modifier.testTag("save_catalog_item_button")
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
