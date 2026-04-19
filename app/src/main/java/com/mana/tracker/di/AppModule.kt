package com.mana.tracker.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.mana.tracker.data.local.ManaDatabase
import com.mana.tracker.data.local.dao.AccountDao
import com.mana.tracker.data.local.dao.AppSettingDao
import com.mana.tracker.data.local.dao.BudgetDao
import com.mana.tracker.data.local.dao.CategoryDao
import com.mana.tracker.data.local.dao.ParserStateDao
import com.mana.tracker.data.local.dao.RuleDao
import com.mana.tracker.data.local.dao.SubscriptionDao
import com.mana.tracker.data.local.dao.TransactionDao
import com.mana.tracker.data.parser.BankSmsParser
import com.mana.tracker.data.parser.IranianBankParsers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ManaDatabase {
        return Room.databaseBuilder(context, ManaDatabase::class.java, "mana.db").build()
    }

    @Provides
    fun provideTransactionDao(db: ManaDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideAccountDao(db: ManaDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: ManaDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: ManaDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideRuleDao(db: ManaDatabase): RuleDao = db.ruleDao()

    @Provides
    fun provideSubscriptionDao(db: ManaDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun provideParserStateDao(db: ManaDatabase): ParserStateDao = db.parserStateDao()

    @Provides
    fun provideAppSettingDao(db: ManaDatabase): AppSettingDao = db.appSettingDao()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideBankParsers(): List<BankSmsParser> = IranianBankParsers.all
}
