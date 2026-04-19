package com.mana.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mana.tracker.data.local.dao.AccountDao
import com.mana.tracker.data.local.dao.AppSettingDao
import com.mana.tracker.data.local.dao.BudgetDao
import com.mana.tracker.data.local.dao.CategoryDao
import com.mana.tracker.data.local.dao.ParserStateDao
import com.mana.tracker.data.local.dao.RuleDao
import com.mana.tracker.data.local.dao.SubscriptionDao
import com.mana.tracker.data.local.dao.TransactionDao
import com.mana.tracker.data.local.entity.AccountEntity
import com.mana.tracker.data.local.entity.AppSettingEntity
import com.mana.tracker.data.local.entity.BalanceHistoryEntity
import com.mana.tracker.data.local.entity.BudgetEntity
import com.mana.tracker.data.local.entity.CategoryEntity
import com.mana.tracker.data.local.entity.ParserStateEntity
import com.mana.tracker.data.local.entity.RuleEntity
import com.mana.tracker.data.local.entity.SubscriptionEntity
import com.mana.tracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        RuleEntity::class,
        SubscriptionEntity::class,
        BalanceHistoryEntity::class,
        AppSettingEntity::class,
        ParserStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ManaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun ruleDao(): RuleDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun parserStateDao(): ParserStateDao
    abstract fun appSettingDao(): AppSettingDao
}
