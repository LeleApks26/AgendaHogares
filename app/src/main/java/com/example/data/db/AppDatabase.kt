package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AgendaDao
import com.example.data.dao.CatalogDao
import com.example.data.models.Actividad
import com.example.data.models.AgendaEntry
import com.example.data.models.Chico
import com.example.data.models.Lugar
import com.example.data.models.Operador

@Database(
    entities = [
        AgendaEntry::class,
        Chico::class,
        Actividad::class,
        Lugar::class,
        Operador::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agendaDao(): AgendaDao
    abstract fun catalogDao(): CatalogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agenda_hogares_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
