package com.mana.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mana.data.database.entity.UserBankEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBankDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bank: UserBankEntity): Long

    @Update
    suspend fun update(bank: UserBankEntity)

    @Delete
    suspend fun delete(bank: UserBankEntity)

    @Query("SELECT * FROM user_banks ORDER BY name ASC")
    fun getAllBanks(): Flow<List<UserBankEntity>>

    @Query("SELECT * FROM user_banks WHERE id = :id LIMIT 1")
    suspend fun getBankById(id: Long): UserBankEntity?

    @Query("SELECT * FROM user_banks WHERE sender_numbers LIKE '%' || :sender || '%' LIMIT 1")
    suspend fun getBankBySender(sender: String): UserBankEntity?

    @Query("DELETE FROM user_banks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
