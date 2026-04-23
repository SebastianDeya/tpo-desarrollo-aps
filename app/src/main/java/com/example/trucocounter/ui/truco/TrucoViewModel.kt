package com.example.trucocounter.ui.truco

import androidx.lifecycle.*
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.remote.TeamDto
import kotlinx.coroutines.launch

class TrucoViewModel(private val repository: TeamRepository) : ViewModel() {

    val teams: LiveData<List<TeamDto>> = repository.localTeams

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // GET - descarga equipos desde MockAPI y los guarda en memoria
    fun syncTeams() {
        viewModelScope.launch {
            repository.syncFromApi()
                .onFailure { _error.value = "Error al sincronizar: ${it.message}" }
        }
    }

    // POST - crea un nuevo equipo
    fun createTeam(nombre: String, puntos: Int) {
        viewModelScope.launch {
            repository.createTeam(nombre, puntos)
                .onFailure { _error.value = "Error al crear equipo: ${it.message}" }
        }
    }

    // PUT - actualiza los puntos de un equipo
    fun updateTeam(team: TeamDto) {
        viewModelScope.launch {
            repository.updateTeam(team)
                .onFailure { _error.value = "Error al actualizar: ${it.message}" }
        }
    }

    // DELETE - elimina un equipo
    fun deleteTeam(team: TeamDto) {
        viewModelScope.launch {
            repository.deleteTeam(team)
                .onFailure { _error.value = "Error al eliminar: ${it.message}" }
        }
    }

    fun resetAllScores() {
        viewModelScope.launch {
            repository.resetAllScores()
                .onFailure { _error.value = "Error al reiniciar puntajes: ${it.message}" }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: TeamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrucoViewModel::class.java)) {
                return TrucoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
