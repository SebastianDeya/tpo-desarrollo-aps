package com.example.trucocounter.data.remote

import retrofit2.http.*

interface ApiService {

    @GET("equipos")
    suspend fun getEquipos(): List<TeamDto>

    @POST("equipos")
    suspend fun createEquipo(@Body equipo: TeamDto): TeamDto

    @PUT("equipos/{id}")
    suspend fun updateEquipo(@Path("id") id: String, @Body equipo: TeamDto): TeamDto

    @DELETE("equipos/{id}")
    suspend fun deleteEquipo(@Path("id") id: String): TeamDto
}
