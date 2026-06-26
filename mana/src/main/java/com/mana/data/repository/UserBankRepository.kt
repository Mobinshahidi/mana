package com.mana.data.repository

import com.mana.data.database.dao.UserBankDao
import com.mana.data.database.entity.UserBankEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBankRepository @Inject constructor(
    private val userBankDao: UserBankDao
) {
    fun getAllBanks(): Flow<List<UserBankEntity>> = userBankDao.getAllBanks()

    suspend fun getBankById(id: Long): UserBankEntity? = userBankDao.getBankById(id)

    suspend fun getBankBySender(sender: String): UserBankEntity? = userBankDao.getBankBySender(sender)

    suspend fun insert(bank: UserBankEntity): Long = userBankDao.insert(bank)

    suspend fun update(bank: UserBankEntity) = userBankDao.update(bank)

    suspend fun delete(bank: UserBankEntity) = userBankDao.delete(bank)

    suspend fun deleteById(id: Long) = userBankDao.deleteById(id)
}
