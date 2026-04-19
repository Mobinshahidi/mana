package com.mana.tracker.data.repository

import com.mana.tracker.data.local.dao.BudgetDao
import com.mana.tracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun observeMonth(monthKey: String): Flow<List<BudgetEntity>> = budgetDao.observeByMonth(monthKey)
}
