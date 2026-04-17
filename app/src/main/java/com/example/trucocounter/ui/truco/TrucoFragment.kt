package com.example.trucocounter.ui.truco

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trucocounter.R
import com.example.trucocounter.data.TeamRepository
import com.example.trucocounter.data.remote.RetrofitClient
import com.example.trucocounter.data.remote.TeamDto
import com.example.trucocounter.databinding.FragmentTrucoBinding
import com.google.android.material.snackbar.Snackbar

class TrucoFragment : Fragment() {

    private var _binding: FragmentTrucoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrucoViewModel by viewModels {
        TrucoViewModel.Factory(
            TeamRepository(RetrofitClient.apiService)
        )
    }

    private val adapter = TeamAdapter(
        onEdit = { team -> showTeamDialog(team) },
        onDelete = { team -> confirmDelete(team) },
        onPointsChange = { team, newPoints ->
            viewModel.updateTeam(team.copy(puntos = newPoints))
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrucoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showTeamDialog(null) }
        binding.btnSync.setOnClickListener { viewModel.syncTeams() }

        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            adapter.submitList(teams)
            binding.tvEmpty.isVisible = teams.isEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>("pending_action")
            ?.observe(viewLifecycleOwner) { action ->
                findNavController().currentBackStackEntry?.savedStateHandle
                    ?.remove<String>("pending_action")
                when (action) {
                    "create_group" -> showTeamDialog(null)
                    "start_game" -> confirmStartGame()
                    "sync" -> viewModel.syncTeams()
                }
            }
    }

    private fun showTeamDialog(existingTeam: TeamDto?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_team, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etPuntos = dialogView.findViewById<EditText>(R.id.etPuntos)

        if (existingTeam != null) {
            etNombre.setText(existingTeam.nombre)
            etPuntos.setText(existingTeam.puntos.toString())
        }

        val title = if (existingTeam == null) "Nuevo Equipo" else "Editar Equipo"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val puntos = etPuntos.text.toString().toIntOrNull() ?: 0
                if (nombre.isNotEmpty()) {
                    if (existingTeam == null) {
                        viewModel.createTeam(nombre, puntos)
                    } else {
                        viewModel.updateTeam(existingTeam.copy(nombre = nombre, puntos = puntos))
                    }
                } else {
                    Snackbar.make(binding.root, "El nombre no puede estar vacío", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(team: TeamDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que querés eliminar a ${team.nombre}?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.deleteTeam(team) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmStartGame() {
        AlertDialog.Builder(requireContext())
            .setTitle("Empezar Partida")
            .setMessage("¿Reiniciar los puntos de todos los equipos a 0?")
            .setPositiveButton("Empezar") { _, _ -> viewModel.resetAllScores() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
