package com.mana.tracker.data.repository

import com.mana.tracker.data.local.dao.AppSettingDao
import com.mana.tracker.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val appSettingDao: AppSettingDao
) {

    fun observeLocale(): Flow<AppSettingEntity?> = appSettingDao.observe(KEY_LOCALE)

    suspend fun setLocale(value: String) {
        appSettingDao.upsert(AppSettingEntity(KEY_LOCALE, value))
    }

    companion object {
        const val KEY_LOCALE = "locale"
    }
}
