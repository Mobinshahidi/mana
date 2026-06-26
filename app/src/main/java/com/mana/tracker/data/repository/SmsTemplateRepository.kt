package com.mana.tracker.data.repository

import com.mana.tracker.data.database.dao.SmsTemplateDao
import com.mana.tracker.data.database.entity.SmsTemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsTemplateRepository @Inject constructor(
    private val smsTemplateDao: SmsTemplateDao
) {
    fun getTemplatesByBankId(bankId: Long): Flow<List<SmsTemplateEntity>> =
        smsTemplateDao.getTemplatesByBankId(bankId)

    suspend fun getTemplateById(id: Long): SmsTemplateEntity? =
        smsTemplateDao.getTemplateById(id)

    suspend fun getActiveTemplatesForSender(sender: String): List<SmsTemplateEntity> =
        smsTemplateDao.getActiveTemplatesForSender(sender)

    suspend fun insert(template: SmsTemplateEntity): Long =
        smsTemplateDao.insert(template)

    suspend fun update(template: SmsTemplateEntity) =
        smsTemplateDao.update(template)

    suspend fun delete(template: SmsTemplateEntity) =
        smsTemplateDao.delete(template)

    suspend fun deleteById(id: Long) =
        smsTemplateDao.deleteById(id)
}
