package com.mana.presentation.home

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import android.view.HapticFeedbackConstants
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.work.WorkInfo
import com.mana.ui.components.SmsParsingProgressDialog
import kotlinx.coroutines.launch
import com.mana.data.database.entity.SubscriptionEntity
import com.mana.data.database.entity.TransactionEntity
import com.mana.data.database.entity.TransactionType
import com.mana.ui.components.BrandIcon
import com.mana.core.Constants
import com.mana.ui.theme.*
import com.mana.ui.components.SummaryCard
import com.mana.ui.components.ListItemCard
import com.mana.ui.components.SectionHeader
import com.mana.ui.components.ManaCard
import com.mana.ui.components.AccountBalancesCard
import com.mana.data.repository.MonthlyBudgetSpending
import com.mana.ui.theme.budget_safe_light
import com.mana.ui.theme.budget_safe_dark
import com.mana.ui.theme.budget_warning_light
import com.mana.ui.theme.budget_warning_dark
import com.mana.ui.theme.budget_danger_light
import com.mana.ui.theme.budget_danger_dark
import com.mana.ui.components.CreditCardsCard
import com.mana.ui.components.UnifiedAccountsCard
import com.mana.ui.components.spotlightTarget
import com.mana.utils.CurrencyFormatter
import com.mana.utils.formatAmount
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
viewModel: HomeViewModel = hiltViewModel(),
navController: NavController,
onNavigateToSettings: () -> Unit = {},
onNavigateToTransactions: () -> Unit = {},
onNavigateToTransactionsWithSearch: () -> Unit = {},
onNavigateToSubscriptions: () -> Unit = {},
onNavigateToBudgets: () -> Unit = {},
onNavigateToAddScreen: () -> Unit = {},
onTransactionClick: (Long) -> Unit = {},
onTransactionTypeClick: (String?) -> Unit = {},
onFabPositioned: (Rect) -> Unit = {}
) {
val uiState by viewModel.uiState.collectAsState()
val deletedTransaction by viewModel.deletedTransaction.collectAsState()
val smsScanWorkInfo by viewModel.smsScanWorkInfo.collectAsState()
val netDisplayType by viewModel.netDisplayType.collectAsState()
val activity = LocalActivity.current

val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// State for full resync confirmation dialog
var showFullResyncDialog by remember { mutableStateOf(false) }

// Haptic feedback
val view = LocalView.current

// Currency dropdown state

// Check for app updates and reviews when the screen is first displayed
LaunchedEffect(Unit) {
// Refresh account balances to ensure proper currency conversion
viewModel.refreshAccountBalances()

activity?.let {
val componentActivity = it as ComponentActivity

// Check for app updates
viewModel.checkForAppUpdate(
activity = componentActivity,
snackbarHostState = snackbarHostState,
scope = scope
)

// Check for in-app review eligibility
viewModel.checkForInAppReview(componentActivity)
}
}

// Refresh hidden accounts whenever this screen becomes visible
// This ensures changes from ManageAccountsScreen are reflected immediately
DisposableEffect(Unit) {
viewModel.refreshHiddenAccounts()
onDispose { }
}

// Handle delete undo snackbar
LaunchedEffect(deletedTransaction) {
deletedTransaction?.let { transaction ->
// Clear the state immediately to prevent re-triggering
viewModel.clearDeletedTransaction()

scope.launch {
val result = snackbarHostState.showSnackbar(
message = "تراکنش حذف شد",
actionLabel = "بازگردانی",
duration = SnackbarDuration.Short
)
if (result == SnackbarResult.ActionPerformed) {
// Pass the transaction directly since state is already cleared
viewModel.undoDeleteTransaction(transaction)
}
}
}
}

// Clear snackbar when navigating away
DisposableEffect(Unit) {
onDispose {
snackbarHostState.currentSnackbarData?.dismiss()
}
}

Scaffold(
snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
) { paddingValues ->
Box(modifier = Modifier
.fillMaxSize()
.padding(paddingValues)) {
LazyColumn(
modifier = Modifier.fillMaxSize(),
contentPadding = PaddingValues(
start = Dimensions.Padding.content,
end = Dimensions.Padding.content,
top = Dimensions.Padding.content,
bottom = Dimensions.Padding.content + 120.dp // Space for dual FABs (Add + Sync)
),
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
// Transaction Summary Cards with HorizontalPager
item {
TransactionSummaryCards(
uiState = uiState,
netDisplayType = netDisplayType,
onCurrencySelected = { viewModel.selectCurrency(it) },
onTypeClick = onTransactionTypeClick
)
}

			item {
				QuickActionsSection(
					onNavigateToTransactions = onNavigateToTransactions,
					onNavigateToBudgets = onNavigateToBudgets,
					onNavigateToSubscriptions = onNavigateToSubscriptions,
					onNavigateToSettings = onNavigateToSettings
				)
			}

// Unified Accounts Section (Credit Cards + Bank Accounts)
if (uiState.creditCards.isNotEmpty() || uiState.accountBalances.isNotEmpty()) {
item {
UnifiedAccountsCard(
creditCards = uiState.creditCards,
bankAccounts = uiState.accountBalances,
totalBalance = uiState.totalBalance,
totalAvailableCredit = uiState.totalAvailableCredit,
selectedCurrency = uiState.selectedCurrency,
onAccountClick = { bankName, accountLast4 ->
navController.navigate(
com.mana.navigation.AccountDetail(
bankName = bankName,
accountLast4 = accountLast4
)
)
}
)
}
}

// Monthly Budget Card
uiState.monthlyBudgetSpending?.let { spending ->
item {
MonthlyBudgetHomeCard(
spending = spending,
currency = uiState.selectedCurrency,
onClick = onNavigateToBudgets
)
}
}

// Upcoming Subscriptions Alert
if (uiState.upcomingSubscriptions.isNotEmpty()) {
item {
UpcomingSubscriptionsCard(
subscriptions = uiState.upcomingSubscriptions,
totalAmount = uiState.upcomingSubscriptionsTotal,
onClick = onNavigateToSubscriptions
)
}
}

// Recent Transactions Section
item {
Spacer(modifier = Modifier.height(Spacing.xs))
HorizontalDivider(
thickness = 0.5.dp,
color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
)
Spacer(modifier = Modifier.height(Spacing.sm))
SectionHeader(
title = "تراکنش‌های اخیر",
action = {
Row(
horizontalArrangement = Arrangement.spacedBy(4.dp),
verticalAlignment = Alignment.CenterVertically
) {
// Search button
IconButton(
onClick = onNavigateToTransactionsWithSearch,
modifier = Modifier.size(36.dp)
) {
Icon(
imageVector = Icons.Default.Search,
contentDescription = "جستجوی تراکنش‌ها",
tint = MaterialTheme.colorScheme.primary
)
}

// View All button
TextButton(onClick = onNavigateToTransactions) {
Text("نمایش همه")
}
}
}
)
}

if (uiState.isLoading) {
item {
Box(
modifier = Modifier
.fillMaxWidth()
.height(Dimensions.Component.minTouchTarget * 2),
contentAlignment = Alignment.Center
) {
CircularProgressIndicator()
}
}
} else if (uiState.recentTransactions.isEmpty()) {
item {
ManaCard(
modifier = Modifier.fillMaxWidth()
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.card),
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
Icon(
imageVector = Icons.Default.Sync,
contentDescription = null,
modifier = Modifier.size(48.dp),
tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
)
Text(
text = "هنوز تراکنشی ثبت نشده",
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.Medium,
color = MaterialTheme.colorScheme.onSurface
)
Text(
text = "برای اسکن پیامک‌ها و شناسایی خودکار تراکنش‌ها، دکمه همگام‌سازی را بزنید",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant,
textAlign = TextAlign.Center
)
Button(
onClick = { viewModel.scanSmsMessages() },
modifier = Modifier.padding(top = Spacing.xs)
) {
Icon(
imageVector = Icons.Default.Sync,
contentDescription = null,
modifier = Modifier.size(18.dp)
)
Spacer(modifier = Modifier.width(Spacing.sm))
Text("Scan SMS")
}
}
}
}
} else {
items(
items = uiState.recentTransactions,
key = { it.id }
) { transaction ->
SimpleTransactionItem(
transaction = transaction,
onClick = { onTransactionClick(transaction.id) }
)
}
}
}

// FABs - Direct access (no speed dial)
Column(
modifier = Modifier
.align(Alignment.BottomEnd)
.padding(Dimensions.Padding.content),
verticalArrangement = Arrangement.spacedBy(12.dp),
horizontalAlignment = Alignment.End
) {
// Add FAB (top, small)
SmallFloatingActionButton(
onClick = onNavigateToAddScreen,
containerColor = MaterialTheme.colorScheme.secondaryContainer,
contentColor = MaterialTheme.colorScheme.onSecondaryContainer
) {
Icon(
imageVector = Icons.Default.Add,
contentDescription = "افزودن تراکنش یا اشتراک"
)
}

// Sync FAB (bottom, primary)
// Single tap: incremental scan, Long press: full resync
Column(
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(4.dp)
) {
Surface(
modifier = Modifier
.spotlightTarget(onFabPositioned)
.size(56.dp)
.pointerInput(Unit) {
detectTapGestures(
onTap = { viewModel.scanSmsMessages() },
onLongPress = {
// Haptic feedback for long press
view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
showFullResyncDialog = true
}
)
},
shape = FloatingActionButtonDefaults.shape,
color = MaterialTheme.colorScheme.primaryContainer,
contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
shadowElevation = 6.dp
) {
Box(
modifier = Modifier.fillMaxSize(),
contentAlignment = Alignment.Center
) {
Icon(
imageVector = Icons.Default.Sync,
contentDescription = "همگام‌سازی پیامک‌ها (نگه‌داشتن برای بازاسکن کامل)"
)
}
}
// Hint for long-press functionality - only show for new users (no transactions yet)
if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
Text(
text = "برای بازاسکن کامل نگه دارید",
style = MaterialTheme.typography.labelSmall,
color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
)
}
}
}

// Full Resync Confirmation Dialog
if (showFullResyncDialog) {
AlertDialog(
onDismissRequest = { showFullResyncDialog = false },
icon = {
Icon(
imageVector = Icons.Default.Sync,
contentDescription = null,
tint = MaterialTheme.colorScheme.primary
)
},
title = {
Text("بازاسکن کامل")
},
text = {
Text(
"This will reprocess all SMS messages from scratch. " +
"Use this to fix issues caused by updated bank parsers.\n\n" +
"This may take a few seconds depending on your message history."
)
},
confirmButton = {
Button(
onClick = {
showFullResyncDialog = false
viewModel.scanSmsMessages(forceResync = true)
}
) {
Text("بازاسکن همه")
}
},
dismissButton = {
TextButton(
onClick = { showFullResyncDialog = false }
) {
Text("Cancel")
}
}
)
}

// SMS Parsing Progress Dialog
SmsParsingProgressDialog(
isVisible = uiState.isScanning,
workInfo = smsScanWorkInfo,
onDismiss = { viewModel.cancelSmsScan() },
onCancel = { viewModel.cancelSmsScan() }
)

// Breakdown Dialog
if (uiState.showBreakdownDialog) {
BreakdownDialog(
currentMonthIncome = uiState.currentMonthIncome,
currentMonthExpenses = uiState.currentMonthExpenses,
currentMonthTotal = uiState.currentMonthTotal,
lastMonthIncome = uiState.lastMonthIncome,
lastMonthExpenses = uiState.lastMonthExpenses,
lastMonthTotal = uiState.lastMonthTotal,
onDismiss = { viewModel.hideBreakdownDialog() }
)
}
}
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTransactionItem(
transaction: TransactionEntity,
onClick: () -> Unit = {}
) {
val amountColor = when (transaction.transactionType) {
TransactionType.INCOME -> if (!isSystemInDarkTheme()) income_light else income_dark
TransactionType.EXPENSE -> if (!isSystemInDarkTheme()) expense_light else expense_dark
TransactionType.CREDIT -> if (!isSystemInDarkTheme()) credit_light else credit_dark
TransactionType.TRANSFER -> if (!isSystemInDarkTheme()) transfer_light else transfer_dark
TransactionType.INVESTMENT -> if (!isSystemInDarkTheme()) investment_light else investment_dark
}

val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
val dateTimeText = transaction.dateTime.format(dateTimeFormatter)

ListItemCard(
title = transaction.merchantName,
subtitle = dateTimeText,
amount = transaction.formatAmount(),
amountColor = amountColor,
onClick = onClick,
leadingContent = {
BrandIcon(
merchantName = transaction.merchantName,
size = 40.dp,
showBackground = true
)
}
)
}

@Composable
private fun MonthSummaryCard(
monthTotal: BigDecimal,
monthlyChange: BigDecimal,
monthlyChangePercent: Int,
currency: String,
currentExpenses: BigDecimal = BigDecimal.ZERO,
lastExpenses: BigDecimal = BigDecimal.ZERO,
isNetDisplayTypeManeh: Boolean = false,
onShowBreakdown: () -> Unit = {}
) {
val isPositive = monthTotal >= BigDecimal.ZERO
val displayAmount = if (isPositive) {
"+${CurrencyFormatter.formatCurrency(monthTotal, currency)}"
} else {
CurrencyFormatter.formatCurrency(monthTotal, currency)
}
val amountColor = if (isPositive) {
if (!isSystemInDarkTheme()) income_light else income_dark
} else {
if (!isSystemInDarkTheme()) expense_light else expense_dark
}

val expenseChange = currentExpenses - lastExpenses
val now = LocalDate.now()
val lastMonth = now.minusMonths(1)
val periodLabel = "vs ${lastMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} 1-${now.dayOfMonth}"

val subtitle = when {
// No transactions yet
currentExpenses == BigDecimal.ZERO && lastExpenses == BigDecimal.ZERO -> {
"No transactions yet"
}
// Spent more than last period
expenseChange > BigDecimal.ZERO -> {
"😟 Spent ${CurrencyFormatter.formatCurrency(expenseChange.abs(), currency)} more $periodLabel"
}
// Spent less than last period
expenseChange < BigDecimal.ZERO -> {
"😊 Spent ${CurrencyFormatter.formatCurrency(expenseChange.abs(), currency)} less $periodLabel"
}
// Saved more (higher positive balance)
monthlyChange > BigDecimal.ZERO && monthTotal > BigDecimal.ZERO -> {
"🎉 Saved ${CurrencyFormatter.formatCurrency(monthlyChange.abs(), currency)} more $periodLabel"
}
// No change
else -> {
"Same as last period"
}
}

val currentMonth = now.month.name.lowercase().replaceFirstChar { it.uppercase() }

// Currency symbol mapping for display
val currencySymbols = mapOf(
"INR" to "₹",
"USD" to "$",
"AED" to "AED",
"NPR" to "₨",
"ETB" to "ብর"
)
val currencySymbol = currencySymbols[currency] ?: currency

// Determine the title based on net display type
val titleText = if (isNetDisplayTypeManeh) {
"Net Worth ($currencySymbol) • $currentMonth 1-${now.dayOfMonth}"
} else {
"Cash Flow ($currencySymbol) • $currentMonth 1-${now.dayOfMonth}"
}

SummaryCard(
title = titleText,
amount = displayAmount,
subtitle = subtitle,
amountColor = amountColor,
onClick = onShowBreakdown
)
}

@Composable
private fun TransactionItem(
transaction: TransactionEntity,
onClick: () -> Unit = {}
) {
val amountColor = when (transaction.transactionType) {
TransactionType.INCOME -> if (!isSystemInDarkTheme()) income_light else income_dark
TransactionType.EXPENSE -> if (!isSystemInDarkTheme()) expense_light else expense_dark
TransactionType.CREDIT -> if (!isSystemInDarkTheme()) credit_light else credit_dark
TransactionType.TRANSFER -> if (!isSystemInDarkTheme()) transfer_light else transfer_dark
TransactionType.INVESTMENT -> if (!isSystemInDarkTheme()) investment_light else investment_dark
}

// Get subtle background color based on transaction type
val cardBackgroundColor = when (transaction.transactionType) {
TransactionType.CREDIT -> (if (!isSystemInDarkTheme()) credit_light else credit_dark).copy(alpha = 0.05f)
TransactionType.TRANSFER -> (if (!isSystemInDarkTheme()) transfer_light else transfer_dark).copy(alpha = 0.05f)
TransactionType.INVESTMENT -> (if (!isSystemInDarkTheme()) investment_light else investment_dark).copy(alpha = 0.05f)
TransactionType.INCOME -> (if (!isSystemInDarkTheme()) income_light else income_dark).copy(alpha = 0.03f)
else -> Color.Transparent // Default for regular expenses
}

ListItemCard(
title = transaction.merchantName,
subtitle = transaction.dateTime.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
amount = transaction.formatAmount(),
amountColor = amountColor,
onClick = onClick,
leadingContent = {
BrandIcon(
merchantName = transaction.merchantName,
size = 40.dp,
showBackground = true
)
},
trailingContent = {
Row(
verticalAlignment = Alignment.CenterVertically,
horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
) {
// Show icon for transaction types
when (transaction.transactionType) {
TransactionType.CREDIT -> Icon(
Icons.Default.CreditCard,
contentDescription = "Credit Card",
modifier = Modifier.size(Dimensions.Icon.small),
tint = if (!isSystemInDarkTheme()) credit_light else credit_dark
)
TransactionType.TRANSFER -> Icon(
Icons.Default.SwapHoriz,
contentDescription = "Transfer",
modifier = Modifier.size(Dimensions.Icon.small),
tint = if (!isSystemInDarkTheme()) transfer_light else transfer_dark
)
TransactionType.INVESTMENT -> Icon(
Icons.AutoMirrored.Filled.ShowChart,
contentDescription = "Investment",
modifier = Modifier.size(Dimensions.Icon.small),
tint = if (!isSystemInDarkTheme()) investment_light else investment_dark
)
TransactionType.INCOME -> Icon(
Icons.AutoMirrored.Filled.TrendingUp,
contentDescription = "Income",
modifier = Modifier.size(Dimensions.Icon.small),
tint = if (!isSystemInDarkTheme()) income_light else income_dark
)
TransactionType.EXPENSE -> Icon(
Icons.AutoMirrored.Filled.TrendingDown,
contentDescription = "Expense",
modifier = Modifier.size(Dimensions.Icon.small),
tint = if (!isSystemInDarkTheme()) expense_light else expense_dark
)
}

// Always show amount
Text(
text = transaction.formatAmount(),
style = MaterialTheme.typography.bodyLarge,
fontWeight = FontWeight.SemiBold,
color = amountColor
)
}
}
)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreakdownDialog(
currentMonthIncome: BigDecimal,
currentMonthExpenses: BigDecimal,
currentMonthTotal: BigDecimal,
lastMonthIncome: BigDecimal,
lastMonthExpenses: BigDecimal,
lastMonthTotal: BigDecimal,
onDismiss: () -> Unit
) {
val now = LocalDate.now()
val currentPeriod = "${now.month.name.lowercase().replaceFirstChar { it.uppercase() }} 1-${now.dayOfMonth}"
val lastMonth = now.minusMonths(1)
val lastPeriod = "${lastMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} 1-${now.dayOfMonth}"

Dialog(onDismissRequest = onDismiss) {
Card(
modifier = Modifier
.fillMaxWidth()
.padding(horizontal = Spacing.md), // Reduced horizontal padding for wider modal
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surface
)
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.card),
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
// Title
Text(
text = "Calculation Breakdown",
style = MaterialTheme.typography.headlineSmall,
fontWeight = FontWeight.Bold
)

// Current Period Section
Text(
text = currentPeriod,
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary
)

BreakdownRow(
label = "Income",
amount = currentMonthIncome,
isIncome = true
)

BreakdownRow(
label = "Expenses",
amount = currentMonthExpenses,
isIncome = false
)

HorizontalDivider()

BreakdownRow(
label = "Cash Flow",
amount = currentMonthTotal,
isIncome = currentMonthTotal >= BigDecimal.ZERO,
isBold = true
)

Spacer(modifier = Modifier.height(Spacing.sm))

// Last Period Section
Text(
text = lastPeriod,
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.SemiBold,
color = MaterialTheme.colorScheme.primary
)

BreakdownRow(
label = "Income",
amount = lastMonthIncome,
isIncome = true
)

BreakdownRow(
label = "Expenses",
amount = lastMonthExpenses,
isIncome = false
)

HorizontalDivider()

BreakdownRow(
label = "Cash Flow",
amount = lastMonthTotal,
isIncome = lastMonthTotal >= BigDecimal.ZERO,
isBold = true
)

// Formula explanation
Spacer(modifier = Modifier.height(Spacing.sm))
Card(
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.secondaryContainer
),
modifier = Modifier.fillMaxWidth()
) {
Text(
text = "Formula: Income - Expenses = Cash Flow\n" +
"Green (+) = Savings | Red (-) = Overspending",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSecondaryContainer,
modifier = Modifier.padding(Spacing.sm),
textAlign = TextAlign.Center
)
}

// Close button
TextButton(
onClick = onDismiss,
modifier = Modifier.align(Alignment.End)
) {
Text("Close")
}
}
}
}
}

@Composable
private fun BreakdownRow(
label: String,
amount: BigDecimal,
isIncome: Boolean,
isBold: Boolean = false
) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween
) {
Text(
text = label,
style = MaterialTheme.typography.bodyLarge,
fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
)
Text(
text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.formatCurrency(amount.abs())}",
style = MaterialTheme.typography.bodyLarge,
fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
color = if (isIncome) {
if (!isSystemInDarkTheme()) income_light else income_dark
} else {
if (!isSystemInDarkTheme()) expense_light else expense_dark
}
)
}
}

@Composable
private fun UpcomingSubscriptionsCard(
subscriptions: List<SubscriptionEntity>,
totalAmount: BigDecimal,
onClick: () -> Unit = {}
) {
Card(
modifier = Modifier.fillMaxWidth(),
onClick = onClick,
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.secondaryContainer
)
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Row(
horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
verticalAlignment = Alignment.CenterVertically,
modifier = Modifier.weight(1f)
) {
Icon(
imageVector = Icons.Default.CalendarToday,
contentDescription = null,
tint = MaterialTheme.colorScheme.onSecondaryContainer,
modifier = Modifier.size(Dimensions.Icon.medium)
)
Column {
Text(
text = "اشتراک فعال: ${subscriptions.size}",
style = MaterialTheme.typography.bodyLarge,
fontWeight = FontWeight.Medium,
color = MaterialTheme.colorScheme.onSecondaryContainer
)
Text(
text = "جمع ماهانه: ${CurrencyFormatter.formatCurrency(totalAmount)}",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = Dimensions.Alpha.subtitle)
)
}
}
Text(
text = "مشاهده",
style = MaterialTheme.typography.labelLarge,
color = MaterialTheme.colorScheme.primary,
fontWeight = FontWeight.Medium
)
}
}
}

@Composable
private fun TransactionSummaryCards(
uiState: HomeUiState,
netDisplayType: String,
onCurrencySelected: (String) -> Unit = {},
onTypeClick: (String?) -> Unit = {}
) {
val netWorth = remember(uiState.accountBalances, uiState.creditCards, uiState.selectedCurrency) {
val bankTotal = uiState.accountBalances
.filter { it.currency == uiState.selectedCurrency }
.sumOf { it.balance }
val creditTotal = uiState.creditCards
.filter { it.currency == uiState.selectedCurrency }
.sumOf { it.balance }
bankTotal - creditTotal
}

val cashFlow = uiState.currentMonthIncome - uiState.currentMonthExpenses

Column(
verticalArrangement = Arrangement.spacedBy(Spacing.sm)
) {
if (uiState.availableCurrencies.size > 1) {
EnhancedCurrencySelector(
selectedCurrency = uiState.selectedCurrency,
availableCurrencies = uiState.availableCurrencies,
onCurrencySelected = onCurrencySelected,
modifier = Modifier.fillMaxWidth()
)
}

Text(
text = "خلاصه این ماه",
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.SemiBold
)

Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
SummaryMetricCard(
modifier = Modifier.weight(1f),
label = "خالص دارایی",
value = CurrencyFormatter.formatCurrency(netWorth, uiState.selectedCurrency),
tooltip = "خالص دارایی یعنی جمع موجودی حساب‌ها منهای بدهی کارت‌های اعتباری.",
onClick = { onTypeClick(null) }
)
SummaryMetricCard(
modifier = Modifier.weight(1f),
label = "درآمد",
value = CurrencyFormatter.formatCurrency(uiState.currentMonthIncome, uiState.selectedCurrency),
tooltip = "درآمد یعنی پولی که وارد حساب شما می‌شود؛ مثل حقوق، واریزی یا بازپرداخت.",
onClick = { onTypeClick("INCOME") }
)
}

Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
SummaryMetricCard(
modifier = Modifier.weight(1f),
label = "هزینه‌ها",
value = CurrencyFormatter.formatCurrency(uiState.currentMonthExpenses, uiState.selectedCurrency),
tooltip = "هزینه‌ها یعنی پولی که خرج می‌کنید؛ مثل خرید، قبض یا خدمات.",
onClick = { onTypeClick("EXPENSE") }
)
SummaryMetricCard(
modifier = Modifier.weight(1f),
label = "جریان نقدی",
value = CurrencyFormatter.formatCurrency(cashFlow, uiState.selectedCurrency),
tooltip = "جریان نقدی برابر است با درآمد منهای هزینه‌ها در همین بازه.",
onClick = { onTypeClick(null) }
)
}
}
}

@Composable
private fun SummaryMetricCard(
modifier: Modifier = Modifier,
label: String,
value: String,
tooltip: String,
onClick: () -> Unit = {}
) {
var showInfo by remember { mutableStateOf(false) }

ManaCard(
modifier = modifier
.fillMaxWidth()
.combinedClickable(
onClick = onClick,
onLongClick = { showInfo = true }
)
) {
Column(
verticalArrangement = Arrangement.spacedBy(Spacing.xs)
) {
Row(
verticalAlignment = Alignment.CenterVertically,
horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
) {
Text(
text = label,
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
IconButton(
onClick = { showInfo = true },
modifier = Modifier.size(24.dp)
) {
Icon(
imageVector = Icons.Outlined.Info,
contentDescription = "راهنما",
tint = MaterialTheme.colorScheme.onSurfaceVariant
)
}
}

@Composable
private fun QuickActionsSection(
	onNavigateToTransactions: () -> Unit,
	onNavigateToBudgets: () -> Unit,
	onNavigateToSubscriptions: () -> Unit,
	onNavigateToSettings: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
		Text(
			text = "دسترسی سریع",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.SemiBold
		)

		Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
			QuickActionButton(
				modifier = Modifier.weight(1f),
				label = "تراکنش‌ها",
				icon = Icons.Default.ReceiptLong,
				onClick = onNavigateToTransactions
			)
			QuickActionButton(
				modifier = Modifier.weight(1f),
				label = "بودجه ماهانه",
				icon = Icons.Default.AccountBalanceWallet,
				onClick = onNavigateToBudgets
			)
		}

		Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
			QuickActionButton(
				modifier = Modifier.weight(1f),
				label = "اشتراک‌ها",
				icon = Icons.Default.CalendarToday,
				onClick = onNavigateToSubscriptions
			)
			QuickActionButton(
				modifier = Modifier.weight(1f),
				label = "تنظیمات",
				icon = Icons.Default.Settings,
				onClick = onNavigateToSettings
			)
		}
	}
}

@Composable
private fun QuickActionButton(
	modifier: Modifier = Modifier,
	label: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	onClick: () -> Unit
) {
	FilledTonalButton(
		onClick = onClick,
		modifier = modifier.height(52.dp),
		contentPadding = PaddingValues(horizontal = Spacing.sm)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.width(Spacing.xs))
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Medium
		)
	}
}
Text(
text = value,
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.SemiBold
)
}
}

if (showInfo) {
AlertDialog(
onDismissRequest = { showInfo = false },
confirmButton = {
TextButton(onClick = { showInfo = false }) {
Text("باشه")
}
},
title = { Text(label) },
text = { Text(tooltip) }
)
}
}

@Composable
private fun TransactionTypeCard(
title: String,
icon: androidx.compose.ui.graphics.vector.ImageVector,
amount: BigDecimal,
color: Color,
emoji: String,
currency: String,
onClick: () -> Unit = {}
) {
val currentMonth = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }
val now = LocalDate.now()

val subtitle = when {
amount > BigDecimal.ZERO -> {
when (title) {
"Credit Card" -> "Spent on credit this month"
"Transfers" -> "Moved between accounts"
"Investments" -> "Invested this month"
else -> "Total this month"
}
}
else -> {
when (title) {
"Credit Card" -> "No credit card spending"
"Transfers" -> "No transfers this month"
"Investments" -> "No investments this month"
else -> "بدون تراکنش"
}
}
}

SummaryCard(
title = "$emoji $title • $currentMonth",
subtitle = subtitle,
amount = CurrencyFormatter.formatCurrency(amount, currency),
amountColor = color,
onClick = onClick
)
}

@Composable
private fun EnhancedCurrencySelector(
selectedCurrency: String,
availableCurrencies: List<String>,
onCurrencySelected: (String) -> Unit,
modifier: Modifier = Modifier
) {
// Currency symbol mapping
val currencySymbols = mapOf(
"INR" to "₹",
"USD" to "$",
"AED" to "AED",
"NPR" to "₨",
"ETB" to "ብር"
)

// Compact segmented button style
Surface(
modifier = modifier.fillMaxWidth(),
shape = RoundedCornerShape(12.dp),
color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
border = BorderStroke(
width = 0.5.dp,
color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
)
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(horizontal = Spacing.xs, vertical = Spacing.xs),
horizontalArrangement = Arrangement.SpaceEvenly,
verticalAlignment = Alignment.CenterVertically
) {
availableCurrencies.forEach { currency ->
val isSelected = selectedCurrency == currency
val symbol = currencySymbols[currency] ?: currency

Surface(
onClick = { onCurrencySelected(currency) },
modifier = Modifier
.weight(1f)
.animateContentSize(),
shape = RoundedCornerShape(8.dp),
color = if (isSelected) {
MaterialTheme.colorScheme.primary
} else {
Color.Transparent
}
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = Spacing.sm, horizontal = Spacing.xs),
horizontalArrangement = Arrangement.Center,
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = symbol,
style = MaterialTheme.typography.labelSmall,
fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
color = if (isSelected) {
MaterialTheme.colorScheme.onPrimary
} else {
MaterialTheme.colorScheme.onSurfaceVariant
}
)
if (symbol != currency) {
Spacer(modifier = Modifier.width(2.dp))
Text(
text = currency,
style = MaterialTheme.typography.labelSmall,
color = if (isSelected) {
MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
} else {
MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
}
)
}
}
}
}
}
}
}

@Composable
private fun MonthlyBudgetHomeCard(
spending: MonthlyBudgetSpending,
currency: String,
onClick: () -> Unit
) {
val isDark = isSystemInDarkTheme()
val progressColor = when {
spending.percentageUsed < 50f -> if (isDark) budget_safe_dark else budget_safe_light
spending.percentageUsed < 80f -> if (isDark) budget_warning_dark else budget_warning_light
else -> if (isDark) budget_danger_dark else budget_danger_light
}

Card(
onClick = onClick,
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.secondaryContainer
)
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
verticalArrangement = Arrangement.spacedBy(Spacing.sm)
) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = "Monthly Budget",
style = MaterialTheme.typography.titleSmall,
fontWeight = FontWeight.SemiBold
)
if (spending.daysRemaining > 0 && spending.remaining > java.math.BigDecimal.ZERO) {
Text(
text = "${CurrencyFormatter.formatCurrency(spending.dailyAllowance, currency)}/day",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSecondaryContainer
)
}
}

Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.Bottom
) {
Text(
text = CurrencyFormatter.formatCurrency(spending.totalSpent, currency),
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.Bold,
color = progressColor
)
Text(
text = "/ ${CurrencyFormatter.formatCurrency(spending.totalLimit, currency)}",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSecondaryContainer
)
}

LinearProgressIndicator(
progress = { (spending.percentageUsed / 100f).coerceIn(0f, 1f) },
modifier = Modifier
.fillMaxWidth()
.height(6.dp),
color = progressColor,
trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
)

Text(
text = if (spending.remaining >= java.math.BigDecimal.ZERO) {
"${CurrencyFormatter.formatCurrency(spending.remaining, currency)} remaining"
} else {
"${CurrencyFormatter.formatCurrency(spending.remaining.abs(), currency)} over budget"
},
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSecondaryContainer
)

if (spending.totalIncome > java.math.BigDecimal.ZERO) {
val savingsColor = if (spending.netSavings >= java.math.BigDecimal.ZERO) {
if (isDark) budget_safe_dark else budget_safe_light
} else {
if (isDark) budget_danger_dark else budget_danger_light
}
val deltaText = spending.savingsDelta?.let { delta ->
if (delta.compareTo(java.math.BigDecimal.ZERO) != 0) {
" ${if (delta >= java.math.BigDecimal.ZERO) "↑" else "↓"}${CurrencyFormatter.formatCurrency(delta.abs(), currency)}"
} else null
} ?: ""

Text(
text = "${if (spending.netSavings >= java.math.BigDecimal.ZERO) "Saved" else "Overspent"} ${CurrencyFormatter.formatCurrency(spending.netSavings.abs(), currency)} (${String.format("%.0f", kotlin.math.abs(spending.savingsRate))}%)$deltaText",
style = MaterialTheme.typography.bodySmall,
fontWeight = FontWeight.Medium,
color = savingsColor
)
}
}
}
}
