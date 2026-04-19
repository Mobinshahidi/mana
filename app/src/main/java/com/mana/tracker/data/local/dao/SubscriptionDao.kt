package com.mana.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mana.tracker.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: SubscriptionEntity): Long

    @Query("SELECT * FROM subscriptions WHERE active = 1 ORDER BY nextDueAt ASC")
    fun observeActive(): Flow<List<SubscriptionEntity>>
}
