package com.mana.tracker.data.repository

import com.mana.tracker.data.local.dao.SubscriptionDao
import com.mana.tracker.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    fun observeActive(): Flow<List<SubscriptionEntity>> = subscriptionDao.observeActive()
}
