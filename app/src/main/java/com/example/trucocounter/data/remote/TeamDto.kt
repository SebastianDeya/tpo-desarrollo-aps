package com.example.trucocounter.data.remote

import com.google.gson.annotations.SerializedName

data class TeamDto(
    @SerializedName("id")     val id: String = "",
    @SerializedName("nombre") val nombre: String,
    @SerializedName("puntos") val puntos: Int
)
