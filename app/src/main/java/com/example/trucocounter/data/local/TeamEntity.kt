package com.example.trucocounter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipos")
data class TeamEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val puntos: Int
)
