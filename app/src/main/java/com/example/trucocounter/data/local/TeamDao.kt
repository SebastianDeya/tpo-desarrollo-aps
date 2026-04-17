package com.example.trucocounter.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TeamDao {

    @Query("SELECT * FROM equipos ORDER BY nombre ASC")
    fun getAllTeams(): LiveData<List<TeamEntity>>

    @Query("SELECT * FROM equipos ORDER BY nombre ASC")
    suspend fun getAllTeamsOnce(): List<TeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)

    @Query("DELETE FROM equipos")
    suspend fun deleteAll()
}
