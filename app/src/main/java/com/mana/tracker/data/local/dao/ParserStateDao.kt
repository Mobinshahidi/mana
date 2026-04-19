package com.mana.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mana.tracker.data.local.entity.ParserStateEntity

@Dao
interface ParserStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ParserStateEntity)

    @Query("SELECT * FROM parser_state WHERE id = 1")
    suspend fun get(): ParserStateEntity?
}
