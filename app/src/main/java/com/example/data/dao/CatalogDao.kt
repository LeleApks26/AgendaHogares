package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.Actividad
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    // Chicos
    @Query("SELECT * FROM chicos ORDER BY name ASC")
    fun getAllChicos(): Flow<List<Chico>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChico(chico: Chico): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChicos(chicos: List<Chico>)

    @Update
    suspend fun updateChico(chico: Chico)

    @Query("DELETE FROM chicos WHERE id = :id")
    suspend fun deleteChicoById(id: Long)

    @Query("SELECT COUNT(*) FROM chicos")
    suspend fun countChicos(): Int

    // Actividades
    @Query("SELECT * FROM actividades ORDER BY name ASC")
    fun getAllActividades(): Flow<List<Actividad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividad(actividad: Actividad): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividades(actividades: List<Actividad>)

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun deleteActividadById(id: Long)

    @Query("SELECT COUNT(*) FROM actividades")
    suspend fun countActividades(): Int

    // Lugares
    @Query("SELECT * FROM lugares ORDER BY name ASC")
    fun getAllLugares(): Flow<List<Lugar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLugar(lugar: Lugar): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLugares(lugares: List<Lugar>)

    @Query("DELETE FROM lugares WHERE id = :id")
    suspend fun deleteLugarById(id: Long)

    @Query("SELECT COUNT(*) FROM lugares")
    suspend fun countLugares(): Int

    // Operadores
    @Query("SELECT * FROM operadores ORDER BY name ASC")
    fun getAllOperadores(): Flow<List<Operador>>

    @Query("SELECT * FROM operadores")
    suspend fun getOperadoresList(): List<Operador>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperador(operador: Operador): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperadores(operadores: List<Operador>)

    @Update
    suspend fun updateOperador(operador: Operador)

    @Query("DELETE FROM operadores WHERE id = :id")
    suspend fun deleteOperadorById(id: Long)

    @Query("SELECT COUNT(*) FROM operadores")
    suspend fun countOperadores(): Int
}
