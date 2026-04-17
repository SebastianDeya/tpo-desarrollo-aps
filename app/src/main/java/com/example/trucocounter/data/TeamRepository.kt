package com.example.trucocounter.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.trucocounter.data.remote.ApiService
import com.example.trucocounter.data.remote.CreateTeamRequest
import com.example.trucocounter.data.remote.TeamDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamRepository(
    private val apiService: ApiService
) {
    private val _teams = MutableLiveData<List<TeamDto>>(emptyList())
    val localTeams: LiveData<List<TeamDto>> = _teams

    // GET - sincroniza la API remota con la BD local
    suspend fun syncFromApi(): Result<Unit> = runCatching {
        val remoteTeams = withContext(Dispatchers.IO) {
            apiService.getEquipos()
        }.filter { it.id.isNotBlank() && it.nombre.isNotBlank() }

        _teams.postValue(remoteTeams)
    }

    // POST - crea un equipo en la API y lo persiste localmente
    suspend fun createTeam(nombre: String, puntos: Int): Result<TeamDto> = runCatching {
        val created = withContext(Dispatchers.IO) {
            apiService.createEquipo(CreateTeamRequest(nombre = nombre, puntos = puntos))
        }

        val updated = (_teams.value.orEmpty() + created)
            .filter { it.id.isNotBlank() && it.nombre.isNotBlank() }
        _teams.postValue(updated)
        created
    }

    // PUT - actualiza puntos en la API y en la BD local
    suspend fun updateTeam(team: TeamDto): Result<Unit> = runCatching {
        val updatedTeam = withContext(Dispatchers.IO) {
            apiService.updateEquipo(team.id, team)
        }

        val updated = _teams.value.orEmpty()
            .map { if (it.id == updatedTeam.id) updatedTeam else it }
        _teams.postValue(updated)
    }

    // DELETE - elimina el equipo de la API y de la BD local
    suspend fun deleteTeam(team: TeamDto): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.deleteEquipo(team.id)
        }

        val updated = _teams.value.orEmpty().filterNot { it.id == team.id }
        _teams.postValue(updated)
    }

    fun clearCache() {
        _teams.postValue(emptyList())
    }
}
