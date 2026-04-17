package com.example.trucocounter.data.remote

import com.google.gson.annotations.SerializedName

data class CreateTeamRequest(
    @SerializedName("nombre")  val nombre: String,
    @SerializedName("puntos")  val puntos: Int
)

