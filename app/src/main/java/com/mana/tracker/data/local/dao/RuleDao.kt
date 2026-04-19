package com.mana.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mana.tracker.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: RuleEntity): Long

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY id DESC")
    suspend fun getEnabled(): List<RuleEntity>

    @Query("SELECT * FROM rules ORDER BY id DESC")
    fun observeAll(): Flow<List<RuleEntity>>
}
