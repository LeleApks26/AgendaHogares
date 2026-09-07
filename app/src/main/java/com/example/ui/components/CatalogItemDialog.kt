package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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

// Colores Clásicos e Intensos
private val clasicosColors = listOf(
    "#2563EB", // Azul Real
    "#0D9488", // Verde Azulado
    "#1E40AF", // Índigo Profundo
    "#7C3AED", // Violeta
    "#E11D48", // Rosa Rubí
    "#059669", // Esmeralda
    "#C2410C", // Naranja Oscuro
    "#B45309", // Ámbar Tostado
    "#0F766E", // Pino
    "#DB2777", // Fucsia Intenso
    "#9333EA", // Púrpura
    "#4F46E5", // Índigo
    "#DC2626", // Rojo
    "#475569", // Pizarra Azulado
    "#0891B2", // Cian Océano
    "#854D0E", // Bronce
    "#6B21A8", // Violeta Oscuro
    "#D97706", // Dorado Cálido
    "#BE185D", // Rubí
    "#16A34A", // Verde
    "#0284C7", // Azul Cielo
    "#9D174D", // Vino
    "#65A30D", // Lima Intenso
    "#0369A1", // Cerúleo
    "#A21CAF", // Magenta
    "#374151"  // Pizarra
)

// Colores Fluo / Neón (muy llamativos, alto contraste con la planilla)
private val fluoColors = listOf(
    "#FACC15", // Amarillo Neón Eléctrico
    "#EAB308", // Amarillo Fluo Oro
    "#84CC16", // Lima Neón
    "#22C55E", // Verde Fluo
    "#10B981", // Esmeralda Fluo
    "#00F5D4", // Menta Neón
    "#06B6D4", // Cian Fluo
    "#00D2D3", // Turquesa Neón
    "#38BDF8", // Celeste Fluo
    "#60A5FA", // Azul Neón
    "#818CF8", // Índigo Neón
    "#A855F7", // Violeta Neón
    "#D946EF", // Fucsia Neón
    "#FF007F", // Magenta Fluo
    "#EC4899", // Rosa Fluo
    "#F43F5E", // Fresa Neón
    "#FB7185", // Coral Neón
    "#FB923C", // Naranja Neón
    "#F97316", // Naranja Eléctrico
    "#F59E0B"  // Ámbar Fluo
)

// Colores Pastel (tonos suaves pero con suficiente color para contrastar sin ser blanco ni gris)
private val pastelColors = listOf(
    "#FDBA74", // Naranja Pastel / Durazno
    "#FED7AA", // Melocotón Pastel
    "#FCA5A5", // Salmón Pastel
    "#FDA4AF", // Rosa Coral Pastel
    "#F472B6", // Chicle Pastel
    "#F492CE", // Rosa Bebé Pastel
    "#E879F9", // Orquídea Pastel
    "#C084FC", // Lavanda / Lila Pastel
    "#DDD6FE", // Violeta Suave Pastel
    "#A5B4FC", // Índigo Pastel
    "#93C5FD", // Celeste Bebé Pastel
    "#7DD3FC", // Cielo Pastel
    "#BAE6FD", // Hielo Glaciar Pastel
    "#67E8F9", // Aguamarina Pastel
    "#5EEAD4", // Turquesa Pastel
    "#6EE7B7", // Menta Pastel
    "#86EFAC", // Verde Manzana Pastel
    "#BEF264", // Lima Pastel
    "#FDE047"  // Amarillo Pastel
)

private val allAvailableColors = fluoColors + pastelColors + clasicosColors

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
    onSaveOperador: (name: String, colorHex: String, textColorHex: String) -> Unit
) {
    var name by remember(type, editingChico, editingActividad, editingLugar, editingOperador) {
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

    var selectedColor by remember(type, editingChico, editingActividad, editingLugar, editingOperador) {
        mutableStateOf(
            when (type) {
                "chico" -> editingChico?.colorHex ?: allAvailableColors[0]
                "actividad" -> editingActividad?.colorHex ?: "#10B981"
                "operador" -> editingOperador?.colorHex ?: allAvailableColors[1]
                else -> allAvailableColors[0]
            }
        )
    }

    var selectedCategory by remember { mutableStateOf("Todos") }

    val title = when (type) {
        "chico" -> if (editingChico != null) "Editar Niño/a" else "Nuevo Niño/a"
        "actividad" -> if (editingActividad != null) "Editar Actividad" else "Nueva Actividad"
        "lugar" -> if (editingLugar != null) "Editar Lugar" else "Nuevo Lugar"
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                            val previewTextColor = if (type == "chico") "#FFFFFF" else getOptimalTextColorHex(selectedColor)
                            BadgeChip(
                                text = name.ifBlank { "NOMBRE" },
                                backgroundColorHex = selectedColor,
                                textColorHex = previewTextColor,
                                forceWhiteText = (type == "chico")
                            )
                        }

                        // Category filter chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp)
                        ) {
                            listOf("Todos", "⚡ Fluo / Neón", "🎨 Pastel", "🔹 Clásicos").forEach { cat ->
                                val isCatSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }

                        val displayedColors = when (selectedCategory) {
                            "⚡ Fluo / Neón" -> fluoColors
                            "🎨 Pastel" -> pastelColors
                            "🔹 Clásicos" -> clasicosColors
                            else -> allAvailableColors
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            displayedColors.forEach { colorHex ->
                                val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(colorHex, Color.Gray))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = parseHexColor(getOptimalTextColorHex(colorHex), Color.White),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
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
                        val textColorHex = getOptimalTextColorHex(selectedColor)
                        when (type) {
                            "chico" -> onSaveChico(name, selectedColor, "#FFFFFF")
                            "actividad" -> onSaveActividad(name, selectedColor)
                            "lugar" -> onSaveLugar(name)
                            "operador" -> onSaveOperador(name, selectedColor, textColorHex)
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
