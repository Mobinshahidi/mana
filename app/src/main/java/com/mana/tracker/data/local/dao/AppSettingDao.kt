package com.mana.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mana.tracker.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: AppSettingEntity)

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun get(key: String): AppSettingEntity?

    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun observe(key: String): Flow<AppSettingEntity?>
}
