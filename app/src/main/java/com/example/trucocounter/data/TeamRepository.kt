package com.example.trucocounter.data

import androidx.lifecycle.LiveData
import com.example.trucocounter.data.local.TeamDao
import com.example.trucocounter.data.local.TeamEntity
import com.example.trucocounter.data.remote.ApiService
import com.example.trucocounter.data.remote.CreateTeamRequest
import com.example.trucocounter.data.remote.TeamDto

class TeamRepository(
    private val teamDao: TeamDao,
    private val apiService: ApiService
) {
    // LiveData observada por el ViewModel; Room notifica cambios automáticamente
    val localTeams: LiveData<List<TeamEntity>> = teamDao.getAllTeams()

    // GET - sincroniza la API remota con la BD local
    suspend fun syncFromApi(): Result<Unit> = runCatching {
        val remoteTeams = apiService.getEquipos()
        remoteTeams.forEach { dto -> teamDao.insertTeam(dto.toEntity()) }
    }

    // POST - crea un equipo en la API y lo persiste localmente
    suspend fun createTeam(nombre: String, puntos: Int): Result<TeamEntity> = runCatching {
        val request = CreateTeamRequest(nombre = nombre, puntos = puntos)
        val created = apiService.createEquipo(request)
        val entity = created.toEntity()
        teamDao.insertTeam(entity)
        entity
    }

    // PUT - actualiza puntos en la API y en la BD local
    suspend fun updateTeam(team: TeamEntity): Result<Unit> = runCatching {
        apiService.updateEquipo(team.id, team.toDto())
        teamDao.updateTeam(team)
    }

    // DELETE - elimina el equipo de la API y de la BD local
    suspend fun deleteTeam(team: TeamEntity): Result<Unit> = runCatching {
        apiService.deleteEquipo(team.id)
        teamDao.deleteTeam(team)
    }

    private fun TeamDto.toEntity() = TeamEntity(id = id, nombre = nombre, puntos = puntos)
    private fun TeamEntity.toDto() = TeamDto(id = id, nombre = nombre, puntos = puntos)
}
