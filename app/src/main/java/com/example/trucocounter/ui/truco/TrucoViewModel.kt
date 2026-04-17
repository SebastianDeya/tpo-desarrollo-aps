package com.example.trucocounter.ui.truco

import androidx.lifecycle.*
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.remote.TeamDto
import kotlinx.coroutines.launch

class TrucoViewModel(private val repository: TeamRepository) : ViewModel() {

    val teams: LiveData<List<TeamDto>> = repository.localTeams

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // GET - descarga equipos desde MockAPI y los guarda en memoria
    fun syncTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncFromApi()
                .onFailure { _error.value = "Error al sincronizar: ${it.message}" }
            _isLoading.value = false
        }
    }

    // POST - crea un nuevo equipo
    fun createTeam(nombre: String, puntos: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createTeam(nombre, puntos)
                .onFailure { _error.value = "Error al crear equipo: ${it.message}" }
            _isLoading.value = false
        }
    }

    // PUT - actualiza los puntos de un equipo
    fun updateTeam(team: TeamDto) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateTeam(team)
                .onFailure { _error.value = "Error al actualizar: ${it.message}" }
            _isLoading.value = false
        }
    }

    // DELETE - elimina un equipo
    fun deleteTeam(team: TeamDto) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTeam(team)
                .onFailure { _error.value = "Error al eliminar: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun resetAllScores() {
        repository.resetAllScores()
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
