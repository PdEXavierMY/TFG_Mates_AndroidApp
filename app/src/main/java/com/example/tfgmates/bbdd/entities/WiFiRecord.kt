package com.example.tfgmates.bbdd.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_records")
data class WiFiRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "nombre")
    var nombre: String,

    @ColumnInfo(name = "wifi")
    val wifi: String,

    @ColumnInfo(name = "ip")
    var ip: String
)