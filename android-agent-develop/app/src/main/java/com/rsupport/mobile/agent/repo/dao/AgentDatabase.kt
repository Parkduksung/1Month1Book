package com.rsupport.mobile.agent.repo.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AgentEntity::class], version = 2)
abstract class AgentDatabase : RoomDatabase() {

    abstract fun getAgentDao(): AgentDao
}