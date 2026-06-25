package com.mana.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
val route: String,
val title: String,
val icon: ImageVector
) {
data object Home : BottomNavItem(
route = "home",
title = "خانه",
icon = Icons.Default.Home
)

data object Transactions : BottomNavItem(
route = "transactions",
title = "تراکنش‌ها",
icon = Icons.Default.ReceiptLong
)

data object Analytics : BottomNavItem(
route = "analytics",
title = "نمودار",
icon = Icons.Default.Analytics
)

data object Chat : BottomNavItem(
route = "chat",
title = "گفت‌وگو",
icon = Icons.AutoMirrored.Filled.Chat
)
}
