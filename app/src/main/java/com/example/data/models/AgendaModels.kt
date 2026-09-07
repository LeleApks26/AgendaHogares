package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_entries")
data class AgendaEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // e.g. "JUEVES 10, SEPTIEMBRE" or "2026-09-10"
    val time: String, // e.g. "07:30"
    val chicos: String, // comma-separated names, e.g. "MOSHI, IGAL, MILO"
    val actividad: String, // e.g. "DESAYUNO"
    val lugar: String, // e.g. "HOGAR"
    val responsables: String, // comma-separated operator names, e.g. "AARON"
    val observaciones: String = "", // e.g. "Medicación de mañana para Moshi"
    val orderIndex: Int = 0
)

@Entity(tableName = "chicos")
data class Chico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val textColorHex: String = "#000000"
)

@Entity(tableName = "actividades")
data class Actividad(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#4CAF50"
)

@Entity(tableName = "lugares")
data class Lugar(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(tableName = "operadores")
data class Operador(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val textColorHex: String = "#FFFFFF"
)
