package com.example.trucocounter.ui.truco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trucocounter.data.remote.TeamDto
import com.example.trucocounter.databinding.ItemTeamBinding

class TeamAdapter(
    private val onEdit: (TeamDto) -> Unit,
    private val onDelete: (TeamDto) -> Unit,
    private val onPointsChange: (TeamDto, Int) -> Unit
) : ListAdapter<TeamDto, TeamAdapter.TeamViewHolder>(DiffCallback) {

    inner class TeamViewHolder(private val binding: ItemTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(team: TeamDto) {
            binding.tvTeamName.text = team.nombre
            binding.tvPoints.text = team.puntos.toString()
            binding.btnAdd.setOnClickListener { onPointsChange(team, team.puntos + 1) }
            binding.btnSubtract.setOnClickListener {
                if (team.puntos > 0) onPointsChange(team, team.puntos - 1)
            }
            binding.btnEdit.setOnClickListener { onEdit(team) }
            binding.btnDelete.setOnClickListener { onDelete(team) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TeamDto>() {
        override fun areItemsTheSame(oldItem: TeamDto, newItem: TeamDto) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TeamDto, newItem: TeamDto) =
            oldItem == newItem
    }
}
