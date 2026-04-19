package com.mana.tracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mana.tracker.core.i18n.LocaleManager
import com.mana.tracker.core.theme.ManaTheme
import com.mana.tracker.ui.components.ManaScaffold
import com.mana.tracker.ui.navigation.ManaDestination
import com.mana.tracker.ui.screens.accounts.AccountsScreen
import com.mana.tracker.ui.screens.analytics.AnalyticsScreen
import com.mana.tracker.ui.screens.budgets.BudgetsScreen
import com.mana.tracker.ui.screens.chat.ChatPlaceholderScreen
import com.mana.tracker.ui.screens.home.HomeScreen
import com.mana.tracker.ui.screens.rules.RulesScreen
import com.mana.tracker.ui.screens.settings.SettingsScreen
import com.mana.tracker.ui.screens.subscriptions.SubscriptionsScreen
import com.mana.tracker.ui.screens.transactions.TransactionsScreen
import com.mana.tracker.ui.viewmodel.SettingsViewModel
import com.mana.tracker.core.security.BiometricLockManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun ManaAppRoot() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val localeTag by settingsViewModel.localeTag.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometricLockManager = BiometricLockManager(context)

    ManaTheme {
        ManaScaffold(navController = navController) { modifier ->
            NavHost(
                navController = navController,
                startDestination = ManaDestination.Home.route,
                modifier = modifier
            ) {
                composable(ManaDestination.Home.route) {
                    HomeScreen(localeTag = localeTag)
                }
                composable(ManaDestination.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(ManaDestination.Chat.route) {
                    ChatPlaceholderScreen()
                }
                composable(ManaDestination.Subscriptions.route) {
                    SubscriptionsScreen()
                }
                composable(ManaDestination.Transactions.route) {
                    TransactionsScreen(localeTag = localeTag)
                }
                composable(ManaDestination.Rules.route) {
                    RulesScreen()
                }
                composable(ManaDestination.Settings.route) {
                    SettingsScreen(
                        currentLocaleTag = localeTag,
                        biometricLockManager = biometricLockManager,
                        onSetLocale = {
                            LocaleManager.apply(it)
                            settingsViewModel.setLocale(it)
                        }
                    )
                }
                composable(ManaDestination.Budgets.route) {
                    BudgetsScreen()
                }
                composable(ManaDestination.Accounts.route) {
                    AccountsScreen()
                }
            }
        }
    }
}
