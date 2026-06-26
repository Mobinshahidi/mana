package com.mana.tracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mana.tracker.data.database.entity.SmsTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: SmsTemplateEntity): Long

    @Update
    suspend fun update(template: SmsTemplateEntity)

    @Delete
    suspend fun delete(template: SmsTemplateEntity)

    @Query("SELECT * FROM sms_templates WHERE bank_id = :bankId ORDER BY name ASC")
    fun getTemplatesByBankId(bankId: Long): Flow<List<SmsTemplateEntity>>

    @Query("SELECT * FROM sms_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): SmsTemplateEntity?

    @Query("SELECT st.* FROM sms_templates st INNER JOIN user_banks ub ON ub.id = st.bank_id WHERE ub.sender_numbers LIKE '%' || :sender || '%' AND st.is_active = 1")
    suspend fun getActiveTemplatesForSender(sender: String): List<SmsTemplateEntity>

    @Query("SELECT * FROM sms_templates WHERE is_active = 1")
    fun getAllActiveTemplates(): Flow<List<SmsTemplateEntity>>

    @Query("DELETE FROM sms_templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
