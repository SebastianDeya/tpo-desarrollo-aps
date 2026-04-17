package com.example.trucocounter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.local.TeamEntity
import com.example.trucocounter.ui.truco.TrucoViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class TrucoViewModelTest {

    // Permite que LiveData se ejecute sincrónicamente en tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: TeamRepository
    private lateinit var viewModel: TrucoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.localTeams } returns MutableLiveData(emptyList())
        viewModel = TrucoViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── CREATE ───────────────────────────────────────────────────

    @Test
    fun `createTeam llama al repositorio con nombre y puntos correctos`() = runTest {
        val entity = TeamEntity(id = "1", nombre = "Nosotros", puntos = 0)
        coEvery { repository.createTeam("Nosotros", 0) } returns Result.success(entity)

        viewModel.createTeam("Nosotros", 0)

        coVerify(exactly = 1) { repository.createTeam("Nosotros", 0) }
    }

    @Test
    fun `createTeam termina con isLoading en false`() = runTest {
        coEvery { repository.createTeam(any(), any()) } returns Result.success(
            TeamEntity("1", "Nosotros", 0)
        )

        viewModel.createTeam("Nosotros", 0)

        assertFalse(viewModel.isLoading.value ?: true)
    }

    // ─── READ / SYNC ──────────────────────────────────────────────

    @Test
    fun `syncTeams llama a syncFromApi del repositorio`() = runTest {
        coEvery { repository.syncFromApi() } returns Result.success(Unit)

        viewModel.syncTeams()

        coVerify(exactly = 1) { repository.syncFromApi() }
    }

    @Test
    fun `syncTeams muestra error cuando la API falla`() = runTest {
        coEvery { repository.syncFromApi() } returns Result.failure(Exception("Sin conexión"))

        viewModel.syncTeams()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("sincronizar"))
    }

    // ─── UPDATE ───────────────────────────────────────────────────

    @Test
    fun `updateTeam llama al repositorio con el equipo correcto`() = runTest {
        val team = TeamEntity(id = "1", nombre = "Nosotros", puntos = 15)
        coEvery { repository.updateTeam(team) } returns Result.success(Unit)

        viewModel.updateTeam(team)

        coVerify(exactly = 1) { repository.updateTeam(team) }
    }

    @Test
    fun `updateTeam no muestra error cuando tiene exito`() = runTest {
        val team = TeamEntity(id = "1", nombre = "Ellos", puntos = 5)
        coEvery { repository.updateTeam(team) } returns Result.success(Unit)

        viewModel.updateTeam(team)

        assertNull(viewModel.error.value)
    }

    // ─── DELETE ───────────────────────────────────────────────────

    @Test
    fun `deleteTeam llama al repositorio con el equipo correcto`() = runTest {
        val team = TeamEntity(id = "2", nombre = "Ellos", puntos = 0)
        coEvery { repository.deleteTeam(team) } returns Result.success(Unit)

        viewModel.deleteTeam(team)

        coVerify(exactly = 1) { repository.deleteTeam(team) }
    }

    @Test
    fun `deleteTeam muestra error cuando falla`() = runTest {
        val team = TeamEntity(id = "2", nombre = "Ellos", puntos = 0)
        coEvery { repository.deleteTeam(team) } returns Result.failure(Exception("Error de red"))

        viewModel.deleteTeam(team)

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("eliminar"))
    }

    // ─── ERROR HANDLING ───────────────────────────────────────────

    @Test
    fun `clearError pone el error en null`() = runTest {
        coEvery { repository.syncFromApi() } returns Result.failure(Exception("error"))
        viewModel.syncTeams()
        assertNotNull(viewModel.error.value)

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }
}
