package com.rsupport.mobile.agent.repo.dao

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AgentDao {

    @Query("SELECT * FROM agentEntity WHERE guid LIKE :guid LIMIT 1")
    fun loadAgentEntity(guid: String): LiveData<AgentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(agentEntity: AgentEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(agentEntity: AgentEntity)

    @Delete
    fun delete(agentEntity: AgentEntity)

    @Query("DELETE FROM agentEntity")
    fun deleteAll()
}