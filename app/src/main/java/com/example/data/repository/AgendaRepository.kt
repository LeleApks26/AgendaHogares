package com.example.data.repository

import com.example.data.dao.AgendaDao
import com.example.data.dao.CatalogDao
import com.example.data.models.Actividad
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AgendaRepository(
    private val agendaDao: AgendaDao,
    private val catalogDao: CatalogDao
) {
    fun getEntriesForDate(dateKey: String): Flow<List<AgendaEntry>> =
        agendaDao.getEntriesForDate(dateKey)

    fun getAllDistinctDates(): Flow<List<String>> =
        agendaDao.getAllDistinctDates()

    suspend fun insertEntry(entry: AgendaEntry): Long =
        agendaDao.insertEntry(entry)

    suspend fun updateEntry(entry: AgendaEntry) =
        agendaDao.updateEntry(entry)

    suspend fun deleteEntryById(id: Long) =
        agendaDao.deleteEntryById(id)

    suspend fun clearEntriesForDate(dateKey: String) =
        agendaDao.clearEntriesForDate(dateKey)

    suspend fun countEntriesForDate(dateKey: String): Int =
        agendaDao.countEntriesForDate(dateKey)

    suspend fun copyEntriesFromDateToDate(sourceDateKey: String, targetDateKey: String): Int = withContext(Dispatchers.IO) {
        val source = agendaDao.getEntriesForDateOnce(sourceDateKey)
        if (source.isNotEmpty()) {
            agendaDao.clearEntriesForDate(targetDateKey)
            val cloned = source.map { it.copy(id = 0, dateKey = targetDateKey) }
            agendaDao.insertEntries(cloned)
        }
        source.size
    }

    // Chicos
    fun getAllChicos(): Flow<List<Chico>> = catalogDao.getAllChicos()
    suspend fun insertChico(chico: Chico) = catalogDao.insertChico(chico)
    suspend fun deleteChico(id: Long) = catalogDao.deleteChicoById(id)

    // Actividades
    fun getAllActividades(): Flow<List<Actividad>> = catalogDao.getAllActividades()
    suspend fun insertActividad(actividad: Actividad) = catalogDao.insertActividad(actividad)
    suspend fun deleteActividad(id: Long) = catalogDao.deleteActividadById(id)

    // Lugares
    fun getAllLugares(): Flow<List<Lugar>> = catalogDao.getAllLugares()
    suspend fun insertLugar(lugar: Lugar) = catalogDao.insertLugar(lugar)
    suspend fun deleteLugar(id: Long) = catalogDao.deleteLugarById(id)

    // Operadores
    fun getAllOperadores(): Flow<List<Operador>> = catalogDao.getAllOperadores()
    suspend fun insertOperador(operador: Operador) = catalogDao.insertOperador(operador)
    suspend fun deleteOperador(id: Long) = catalogDao.deleteOperadorById(id)

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        if (catalogDao.countChicos() == 0) {
            val initialChicos = listOf(
                Chico(name = "MOSHI", colorHex = "#84CC16", textColorHex = "#000000"),
                Chico(name = "IGAL", colorHex = "#06B6D4", textColorHex = "#000000"),
                Chico(name = "MILO", colorHex = "#EF4444", textColorHex = "#FFFFFF"),
                Chico(name = "ARON", colorHex = "#10B981", textColorHex = "#FFFFFF"),
                Chico(name = "ASAF", colorHex = "#E11D48", textColorHex = "#FFFFFF"),
                Chico(name = "MATEO", colorHex = "#EAB308", textColorHex = "#FFFFFF"),
                Chico(name = "MICA", colorHex = "#06B6D4", textColorHex = "#FFFFFF"),
                Chico(name = "TRINI", colorHex = "#DB2777", textColorHex = "#FFFFFF"),
                Chico(name = "MOMI", colorHex = "#22C55E", textColorHex = "#FFFFFF"),
                Chico(name = "TATI", colorHex = "#EA580C", textColorHex = "#FFFFFF"),
                Chico(name = "GALI", colorHex = "#D946EF", textColorHex = "#FFFFFF"),
                Chico(name = "JAIM", colorHex = "#7E22CE", textColorHex = "#FFFFFF"),
                Chico(name = "TODOS", colorHex = "#3B82F6", textColorHex = "#FFFFFF")
            )
            catalogDao.insertChicos(initialChicos)
        }

        if (catalogDao.countActividades() == 0) {
            val initialActividades = listOf(
                Actividad(name = "DESAYUNO", colorHex = "#F59E0B"),
                Actividad(name = "COLEGIO", colorHex = "#3B82F6"),
                Actividad(name = "TERAPIA", colorHex = "#8B5CF6"),
                Actividad(name = "ALMUERZO", colorHex = "#10B981"),
                Actividad(name = "APOYO ESCOLAR", colorHex = "#06B6D4"),
                Actividad(name = "MERIENDA", colorHex = "#F97316"),
                Actividad(name = "TALLER RECREATIVO", colorHex = "#EC4899"),
                Actividad(name = "CENA", colorHex = "#6366F1")
            )
            catalogDao.insertActividades(initialActividades)
        }

        if (catalogDao.countLugares() == 0) {
            val initialLugares = listOf(
                Lugar(name = "HOGAR"),
                Lugar(name = "ESCUELA N° 12"),
                Lugar(name = "CONSULTORIO CENTRAL"),
                Lugar(name = "HOGAR SALÓN"),
                Lugar(name = "PATIO"),
                Lugar(name = "CLUB SOCIAL")
            )
            catalogDao.insertLugares(initialLugares)
        }

        val allOfficialOperadores = listOf(
            Operador(name = "AARON", colorHex = "#2563EB"),
            Operador(name = "DAMI", colorHex = "#0D9488"),
            Operador(name = "MARCOS", colorHex = "#1E40AF"),
            Operador(name = "LELE", colorHex = "#7C3AED"),
            Operador(name = "GABY", colorHex = "#E11D48"),
            Operador(name = "DENIS", colorHex = "#059669"),
            Operador(name = "LUIS H", colorHex = "#C2410C"),
            Operador(name = "LUIS G", colorHex = "#B45309"),
            Operador(name = "DIEGO", colorHex = "#0F766E"),
            Operador(name = "BARBIE", colorHex = "#DB2777"),
            Operador(name = "MELI", colorHex = "#9333EA"),
            Operador(name = "ARI", colorHex = "#4F46E5"),
            Operador(name = "ROX", colorHex = "#DC2626"),
            Operador(name = "ELIZABETH", colorHex = "#475569"),
            Operador(name = "ELIANA", colorHex = "#0891B2"),
            Operador(name = "DOLORES", colorHex = "#854D0E"),
            Operador(name = "BETTINA", colorHex = "#6B21A8"),
            Operador(name = "MICA", colorHex = "#D97706"),
            Operador(name = "LARA", colorHex = "#BE185D"),
            Operador(name = "ALE", colorHex = "#16A34A"),
            Operador(name = "CANDE", colorHex = "#0284C7"),
            Operador(name = "KAREN", colorHex = "#9D174D"),
            Operador(name = "LAURA R", colorHex = "#65A30D"),
            Operador(name = "LAURA C", colorHex = "#0369A1"),
            Operador(name = "NATALIA", colorHex = "#A21CAF"),
            Operador(name = "SILVIA B", colorHex = "#374151")
        )

        val existingOperadores = catalogDao.getOperadoresList()
        val existingMap = existingOperadores.associateBy { it.name.uppercase().trim() }

        allOfficialOperadores.forEach { official ->
            val existing = existingMap[official.name.uppercase().trim()]
            if (existing == null) {
                catalogDao.insertOperador(official)
            } else if (existing.colorHex != official.colorHex && existing.id > 0) {
                catalogDao.updateOperador(existing.copy(colorHex = official.colorHex))
            }
        }

        val defaultDateKey = "JUEVES 10, SEPTIEMBRE"
        if (agendaDao.countEntriesForDate(defaultDateKey) == 0) {
            val initialEntries = listOf(
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "07:30",
                    chicos = "MOSHI, IGAL, MILO",
                    actividad = "DESAYUNO",
                    lugar = "HOGAR",
                    responsables = "AARON",
                    observaciones = "Medicación de mañana para Moshi",
                    orderIndex = 1
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "08:15",
                    chicos = "ARON, ASAF",
                    actividad = "COLEGIO",
                    lugar = "ESCUELA N° 12",
                    responsables = "DAMI, LELE",
                    observaciones = "Llevar vianda y cuaderno de comunicaciones",
                    orderIndex = 2
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "09:30",
                    chicos = "MATEO",
                    actividad = "TERAPIA",
                    lugar = "CONSULTORIO CENTRAL",
                    responsables = "LUIS H",
                    observaciones = "Sesión con Lic. Andrea",
                    orderIndex = 3
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "12:30",
                    chicos = "MICA, TRINI, MOMI, TATI",
                    actividad = "ALMUERZO",
                    lugar = "HOGAR",
                    responsables = "DIEGO, GABY",
                    observaciones = "Menú especial sin sal",
                    orderIndex = 4
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "14:00",
                    chicos = "GALI, JAIM",
                    actividad = "APOYO ESCOLAR",
                    lugar = "HOGAR SALÓN",
                    responsables = "MARCOS",
                    observaciones = "Preparar carpeta de matemáticas",
                    orderIndex = 5
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "16:30",
                    chicos = "MOSHI, ARON, MATEO, GALI",
                    actividad = "MERIENDA",
                    lugar = "HOGAR",
                    responsables = "DAMI, AARON",
                    observaciones = "Merienda comunitaria con frutas frescas",
                    orderIndex = 6
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "18:00",
                    chicos = "TODOS",
                    actividad = "TALLER RECREATIVO",
                    lugar = "PATIO",
                    responsables = "LELE, DIEGO",
                    observaciones = "Juegos de mesa y recreación al aire libre",
                    orderIndex = 7
                ),
                AgendaEntry(
                    dateKey = defaultDateKey,
                    time = "20:30",
                    chicos = "TODOS",
                    actividad = "CENA",
                    lugar = "HOGAR",
                    responsables = "LUIS H, GABY",
                    observaciones = "Revisión de mochilas y descanso",
                    orderIndex = 8
                )
            )
            agendaDao.insertEntries(initialEntries)
            if (agendaDao.countEntriesForDate("PLANTILLA_JUEVES") == 0) {
                val templateEntries = initialEntries.map { it.copy(id = 0, dateKey = "PLANTILLA_JUEVES") }
                agendaDao.insertEntries(templateEntries)
            }
        }
    }
}
