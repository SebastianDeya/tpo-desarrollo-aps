package com.example.trucocounter

import androidx.lifecycle.MutableLiveData
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.local.TeamDao
import com.example.trucocounter.data.local.TeamEntity
import com.example.trucocounter.data.remote.ApiService
import com.example.trucocounter.data.remote.TeamDto
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class TeamRepositoryTest {

    private lateinit var teamDao: TeamDao
    private lateinit var apiService: ApiService
    private lateinit var repository: TeamRepository

    @Before
    fun setUp() {
        teamDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        every { teamDao.getAllTeams() } returns MutableLiveData(emptyList())
        repository = TeamRepository(teamDao, apiService)
    }

    // ─── syncFromApi ──────────────────────────────────────────────

    @Test
    fun `syncFromApi inserta equipos en la BD local`() = runTest {
        val dtos = listOf(
            TeamDto(id = "1", nombre = "Nosotros", puntos = 10),
            TeamDto(id = "2", nombre = "Ellos",    puntos = 5)
        )
        coEvery { apiService.getEquipos() } returns dtos

        val result = repository.syncFromApi()

        assertTrue(result.isSuccess)
        coVerify { teamDao.insertTeam(TeamEntity("1", "Nosotros", 10)) }
        coVerify { teamDao.insertTeam(TeamEntity("2", "Ellos",    5)) }
    }

    @Test
    fun `syncFromApi devuelve failure cuando la API lanza excepcion`() = runTest {
        coEvery { apiService.getEquipos() } throws Exception("Timeout")

        val result = repository.syncFromApi()

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }

    // ─── createTeam ───────────────────────────────────────────────

    @Test
    fun `createTeam guarda el equipo retornado por la API`() = runTest {
        val dto = TeamDto(id = "1", nombre = "Nosotros", puntos = 0)
        coEvery { apiService.createEquipo(any()) } returns dto

        val result = repository.createTeam("Nosotros", 0)

        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrNull()?.id)
        coVerify { teamDao.insertTeam(TeamEntity("1", "Nosotros", 0)) }
    }

    @Test
    fun `createTeam devuelve failure si la API falla`() = runTest {
        coEvery { apiService.createEquipo(any()) } throws Exception("Sin conexión")

        val result = repository.createTeam("Nosotros", 0)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { teamDao.insertTeam(any()) }
    }

    // ─── updateTeam ───────────────────────────────────────────────

    @Test
    fun `updateTeam actualiza la BD local despues de la API`() = runTest {
        val entity = TeamEntity(id = "1", nombre = "Nosotros", puntos = 20)
        coEvery { apiService.updateEquipo("1", any()) } returns TeamDto("1", "Nosotros", 20)

        val result = repository.updateTeam(entity)

        assertTrue(result.isSuccess)
        coVerify { teamDao.updateTeam(entity) }
    }

    @Test
    fun `updateTeam no actualiza BD si la API falla`() = runTest {
        val entity = TeamEntity(id = "1", nombre = "Nosotros", puntos = 20)
        coEvery { apiService.updateEquipo(any(), any()) } throws Exception("Error")

        val result = repository.updateTeam(entity)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { teamDao.updateTeam(any()) }
    }

    // ─── deleteTeam ───────────────────────────────────────────────

    @Test
    fun `deleteTeam elimina de BD local despues de la API`() = runTest {
        val entity = TeamEntity(id = "2", nombre = "Ellos", puntos = 5)
        coEvery { apiService.deleteEquipo("2") } returns TeamDto("2", "Ellos", 5)

        val result = repository.deleteTeam(entity)

        assertTrue(result.isSuccess)
        coVerify { teamDao.deleteTeam(entity) }
    }

    @Test
    fun `deleteTeam no elimina de BD si la API falla`() = runTest {
        val entity = TeamEntity(id = "2", nombre = "Ellos", puntos = 5)
        coEvery { apiService.deleteEquipo(any()) } throws Exception("Error")

        val result = repository.deleteTeam(entity)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { teamDao.deleteTeam(any()) }
    }
}
