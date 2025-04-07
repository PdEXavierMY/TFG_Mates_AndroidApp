package com.example.tfgmates.bbdd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tfgmates.bbdd.entities.WiFiRecord

@Dao
interface WiFiDao {
    @Insert
    suspend fun insert(wifiRecord: WiFiRecord): Long

    @Query("SELECT * FROM wifi_records WHERE wifi = :wifi LIMIT 1")
    suspend fun getWiFiByName(wifi: String): WiFiRecord?

    @Query("SELECT * FROM wifi_records")
    suspend fun getAllWiFis(): List<WiFiRecord>

    // Obtiene un registro WiFi por su ID
    @Query("SELECT * FROM wifi_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WiFiRecord?

    // Actualiza un registro WiFi
    @Update
    suspend fun update(wifiRecord: WiFiRecord)
}



