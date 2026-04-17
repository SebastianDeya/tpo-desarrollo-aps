package com.example.trucocounter.data.remote

import retrofit2.http.*

interface ApiService {

    @GET("Equipos")
    suspend fun getEquipos(): List<TeamDto>

    @POST("Equipos")
    suspend fun createEquipo(@Body equipo: CreateTeamRequest): TeamDto

    @PUT("Equipos/{id}")
    suspend fun updateEquipo(@Path("id") id: String, @Body equipo: TeamDto): TeamDto

    @DELETE("Equipos/{id}")
    suspend fun deleteEquipo(@Path("id") id: String): TeamDto
}
