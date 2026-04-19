package com.mana.tracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mana.tracker.R
import com.mana.tracker.ui.navigation.ManaDestination

@Composable
fun ManaScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    val items = listOf(
        Triple(ManaDestination.Home, Icons.Default.Home, R.string.nav_home),
        Triple(ManaDestination.Analytics, Icons.Default.Analytics, R.string.nav_analytics),
        Triple(ManaDestination.Chat, Icons.Default.Chat, R.string.nav_chat),
        Triple(ManaDestination.Transactions, Icons.Default.ReceiptLong, R.string.nav_transactions),
        Triple(ManaDestination.Rules, Icons.Default.Rule, R.string.nav_rules),
        Triple(ManaDestination.Settings, Icons.Default.Settings, R.string.nav_settings)
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { (dest, icon, labelRes) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = dest.route) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        content(Modifier.padding(padding))
    }
}
