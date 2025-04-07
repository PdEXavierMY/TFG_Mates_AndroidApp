package com.example.tfgmates.bbdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tfgmates.bbdd.dao.WiFiDao
import com.example.tfgmates.bbdd.entities.WiFiRecord

@Database(entities = [WiFiRecord::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wifiDao(): WiFiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val migration1to2 = object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Realiza las operaciones necesarias para migrar de la versión 1 a la versión 2
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(migration1to2)  // Añadir migraciones
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}