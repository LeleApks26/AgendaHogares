package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.models.Actividad
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador
import com.example.data.repository.AgendaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AgendaTab(val label: String, val iconName: String) {
    PLANILLA("Planilla", "table_chart"),
    CRONOGRAMA("Cronograma", "schedule"),
    CHICOS("Chicos", "face"),
    ACTIVIDADES("Actividades", "local_activity"),
    LUGARES("Lugares", "place"),
    OPERADORES("Operadores", "badge")
}

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgendaRepository
    private val calendar = Calendar.getInstance()
    private val spanishDateFormat = SimpleDateFormat("EEEE d, MMMM", Locale("es", "ES"))

    val isDarkMode = MutableStateFlow(false)
    val currentTab = MutableStateFlow(AgendaTab.PLANILLA)
    val currentDateKey = MutableStateFlow("JUEVES 10, SEPTIEMBRE")

    // Dialog flags
    val isEditEntryDialogOpen = MutableStateFlow(false)
    val editingEntry = MutableStateFlow<AgendaEntry?>(null)

    val isWhatsAppDialogOpen = MutableStateFlow(false)
    val isExportJpgDialogOpen = MutableStateFlow(false)
    val exportedJpgFile = MutableStateFlow<java.io.File?>(null)
    val isSavedAgendasDialogOpen = MutableStateFlow(false)
    val weekdayTemplates = MutableStateFlow<List<WeekdayTemplateInfo>>(emptyList())
    val isClearDayDialogOpen = MutableStateFlow(false)
    val isDatePickerDialogOpen = MutableStateFlow(false)

    // Catalog edit dialog
    val isCatalogDialogOpen = MutableStateFlow(false)
    val catalogEditType = MutableStateFlow<String>("") // "chico", "actividad", "lugar", "operador"
    val editingChico = MutableStateFlow<Chico?>(null)
    val editingActividad = MutableStateFlow<Actividad?>(null)
    val editingLugar = MutableStateFlow<Lugar?>(null)
    val editingOperador = MutableStateFlow<Operador?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AgendaRepository(db.agendaDao(), db.catalogDao())
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<AgendaEntry>> = currentDateKey
        .flatMapLatest { key -> repository.getEntriesForDate(key) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedDates: StateFlow<List<String>> = repository.getAllDistinctDates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("JUEVES 10, SEPTIEMBRE")
        )

    val chicos: StateFlow<List<Chico>> = repository.getAllChicos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val actividades: StateFlow<List<Actividad>> = repository.getAllActividades()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lugares: StateFlow<List<Lugar>> = repository.getAllLugares()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val operadores: StateFlow<List<Operador>> = repository.getAllOperadores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun setTab(tab: AgendaTab) {
        currentTab.value = tab
    }

    fun setDateKey(newKey: String) {
        currentDateKey.value = newKey
    }

    fun nextDay() {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formatted = spanishDateFormat.format(calendar.time).uppercase()
        currentDateKey.value = formatted
    }

    fun prevDay() {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val formatted = spanishDateFormat.format(calendar.time).uppercase()
        currentDateKey.value = formatted
    }

    fun setDateFromCalendar(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        val formatted = spanishDateFormat.format(calendar.time).uppercase()
        currentDateKey.value = formatted
    }

    // Entries Actions
    fun openAddEntryDialog() {
        editingEntry.value = null
        isEditEntryDialogOpen.value = true
    }

    fun openEditEntryDialog(entry: AgendaEntry) {
        editingEntry.value = entry
        isEditEntryDialogOpen.value = true
    }

    fun closeEditEntryDialog() {
        isEditEntryDialogOpen.value = false
        editingEntry.value = null
    }

    fun saveEntry(
        id: Long,
        time: String,
        chicos: String,
        actividad: String,
        lugar: String,
        responsables: String,
        observaciones: String
    ) {
        viewModelScope.launch {
            val dateKey = currentDateKey.value
            val currentList = entries.value
            val orderIndex = if (id == 0L) (currentList.size + 1) else (editingEntry.value?.orderIndex ?: (currentList.size + 1))
            val entry = AgendaEntry(
                id = id,
                dateKey = dateKey,
                time = time.ifBlank { "08:00" },
                chicos = chicos.ifBlank { "TODOS" },
                actividad = actividad.ifBlank { "ACTIVIDAD" },
                lugar = lugar.ifBlank { "HOGAR" },
                responsables = responsables.ifBlank { "OPERADOR" },
                observaciones = observaciones,
                orderIndex = orderIndex
            )
            if (id == 0L) {
                repository.insertEntry(entry)
            } else {
                repository.updateEntry(entry)
            }
            closeEditEntryDialog()
        }
    }

    fun duplicateEntry(entry: AgendaEntry) {
        viewModelScope.launch {
            val copy = entry.copy(
                id = 0,
                orderIndex = entries.value.size + 1
            )
            repository.insertEntry(copy)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteEntryById(id)
        }
    }

    fun clearCurrentDay() {
        viewModelScope.launch {
            repository.clearEntriesForDate(currentDateKey.value)
            isClearDayDialogOpen.value = false
        }
    }

    // Catalogs
    fun openAddChicoDialog() {
        editingChico.value = null
        catalogEditType.value = "chico"
        isCatalogDialogOpen.value = true
    }

    fun openEditChicoDialog(chico: Chico) {
        editingChico.value = chico
        catalogEditType.value = "chico"
        isCatalogDialogOpen.value = true
    }

    fun saveChico(name: String, colorHex: String, textColorHex: String) {
        viewModelScope.launch {
            val id = editingChico.value?.id ?: 0L
            repository.insertChico(Chico(id = id, name = name.uppercase().trim(), colorHex = colorHex, textColorHex = textColorHex))
            isCatalogDialogOpen.value = false
        }
    }

    fun deleteChico(id: Long) {
        viewModelScope.launch { repository.deleteChico(id) }
    }

    fun openAddActividadDialog() {
        editingActividad.value = null
        catalogEditType.value = "actividad"
        isCatalogDialogOpen.value = true
    }

    fun saveActividad(name: String, colorHex: String) {
        viewModelScope.launch {
            val id = editingActividad.value?.id ?: 0L
            repository.insertActividad(Actividad(id = id, name = name.uppercase().trim(), colorHex = colorHex))
            isCatalogDialogOpen.value = false
        }
    }

    fun deleteActividad(id: Long) {
        viewModelScope.launch { repository.deleteActividad(id) }
    }

    fun openAddLugarDialog() {
        editingLugar.value = null
        catalogEditType.value = "lugar"
        isCatalogDialogOpen.value = true
    }

    fun saveLugar(name: String) {
        viewModelScope.launch {
            val id = editingLugar.value?.id ?: 0L
            repository.insertLugar(Lugar(id = id, name = name.uppercase().trim()))
            isCatalogDialogOpen.value = false
        }
    }

    fun deleteLugar(id: Long) {
        viewModelScope.launch { repository.deleteLugar(id) }
    }

    fun openAddOperadorDialog() {
        editingOperador.value = null
        catalogEditType.value = "operador"
        isCatalogDialogOpen.value = true
    }

    fun openEditOperadorDialog(operador: Operador) {
        editingOperador.value = operador
        catalogEditType.value = "operador"
        isCatalogDialogOpen.value = true
    }

    fun saveOperador(name: String, colorHex: String) {
        viewModelScope.launch {
            val id = editingOperador.value?.id ?: 0L
            repository.insertOperador(Operador(id = id, name = name.uppercase().trim(), colorHex = colorHex))
            isCatalogDialogOpen.value = false
        }
    }

    fun deleteOperador(id: Long) {
        viewModelScope.launch { repository.deleteOperador(id) }
    }

    fun quickAddChico(name: String) {
        viewModelScope.launch {
            repository.insertChico(Chico(id = 0L, name = name.uppercase().trim(), colorHex = "#3B82F6", textColorHex = "#FFFFFF"))
        }
    }

    fun quickAddActividad(name: String) {
        viewModelScope.launch {
            repository.insertActividad(Actividad(id = 0L, name = name.uppercase().trim(), colorHex = "#2E7D32"))
        }
    }

    fun quickAddLugar(name: String) {
        viewModelScope.launch {
            repository.insertLugar(Lugar(id = 0L, name = name.uppercase().trim()))
        }
    }

    fun quickAddOperador(name: String) {
        viewModelScope.launch {
            repository.insertOperador(Operador(id = 0L, name = name.uppercase().trim(), colorHex = "#2563EB"))
        }
    }

    // WhatsApp Formatted Text
    fun formatWhatsAppMessage(): String {
        val currentEntries = entries.value
        val date = currentDateKey.value
        val sb = StringBuilder()
        sb.append("📋 *AGENDA HOGARES*\n")
        sb.append("📅 *$date*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        if (currentEntries.isEmpty()) {
            sb.append("No hay registros programados para este día.")
            return sb.toString()
        }

        currentEntries.forEachIndexed { index, item ->
            sb.append("${index + 1}️⃣ ⏰ *${item.time} hs* - *${item.actividad}*\n")
            sb.append("🧒 *Niña/o:* ${item.chicos}\n")
            sb.append("🏢 *Lugar:* ${item.lugar}\n")
            sb.append("👥 *Coordi:* ${item.responsables}\n")
            if (item.observaciones.isNotBlank()) {
                sb.append("📝 *Obs:* ${item.observaciones}\n")
            }
            sb.append("────────────────────\n")
        }

        sb.append("\n_Generado automáticamente desde Agenda Hogares_ ✨")
        return sb.toString()
    }

    fun exportPlanillaToJpg() {
        viewModelScope.launch {
            val file = com.example.util.PlanillaImageGenerator.generatePlanillaJpg(
                context = getApplication(),
                dateKey = currentDateKey.value,
                entries = entries.value,
                chicosCatalog = chicos.value,
                operadoresCatalog = operadores.value
            )
            exportedJpgFile.value = file
            isExportJpgDialogOpen.value = true
        }
    }

    fun openSavedAgendasDialog() {
        refreshWeekdayTemplates()
        isSavedAgendasDialogOpen.value = true
    }

    fun refreshWeekdayTemplates() {
        viewModelScope.launch {
            val days = listOf(
                "PLANTILLA_LUNES" to "Lunes",
                "PLANTILLA_MARTES" to "Martes",
                "PLANTILLA_MIERCOLES" to "Miércoles",
                "PLANTILLA_JUEVES" to "Jueves",
                "PLANTILLA_VIERNES" to "Viernes",
                "PLANTILLA_SABADO" to "Sábado",
                "PLANTILLA_DOMINGO" to "Domingo"
            )
            val list = days.map { (key, name) ->
                val count = repository.countEntriesForDate(key)
                WeekdayTemplateInfo(dayKey = key, displayName = name, count = count)
            }
            weekdayTemplates.value = list
        }
    }

    fun saveCurrentDayAsWeekdayTemplate(templateKey: String) {
        viewModelScope.launch {
            repository.copyEntriesFromDateToDate(currentDateKey.value, templateKey)
            refreshWeekdayTemplates()
        }
    }

    fun loadWeekdayTemplateIntoCurrentDay(templateKey: String) {
        viewModelScope.launch {
            repository.copyEntriesFromDateToDate(templateKey, currentDateKey.value)
            refreshWeekdayTemplates()
        }
    }
}

data class WeekdayTemplateInfo(
    val dayKey: String,
    val displayName: String,
    val count: Int
)
