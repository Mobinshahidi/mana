package com.mana.tracker.data.repository

import com.mana.tracker.data.local.dao.RuleDao
import com.mana.tracker.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao
) {
    fun observeAll(): Flow<List<RuleEntity>> = ruleDao.observeAll()
}
