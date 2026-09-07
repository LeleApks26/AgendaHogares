package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.AgendaEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_entries WHERE dateKey = :dateKey ORDER BY time ASC, orderIndex ASC")
    fun getEntriesForDate(dateKey: String): Flow<List<AgendaEntry>>

    @Query("SELECT * FROM agenda_entries WHERE dateKey = :dateKey ORDER BY time ASC, orderIndex ASC")
    suspend fun getEntriesForDateOnce(dateKey: String): List<AgendaEntry>

    @Query("SELECT DISTINCT dateKey FROM agenda_entries WHERE dateKey NOT LIKE 'PLANTILLA_%' ORDER BY dateKey DESC")
    fun getAllDistinctDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AgendaEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<AgendaEntry>)

    @Update
    suspend fun updateEntry(entry: AgendaEntry)

    @Query("DELETE FROM agenda_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM agenda_entries WHERE dateKey = :dateKey")
    suspend fun clearEntriesForDate(dateKey: String)

    @Query("SELECT COUNT(*) FROM agenda_entries WHERE dateKey = :dateKey")
    suspend fun countEntriesForDate(dateKey: String): Int
}
