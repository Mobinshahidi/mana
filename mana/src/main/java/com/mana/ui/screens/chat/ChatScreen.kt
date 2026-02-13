package com.mana.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.data.repository.ModelState
import com.mana.ui.theme.Dimensions
import com.mana.ui.theme.Spacing
import com.mana.utils.TokenUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
modifier: Modifier = Modifier,
viewModel: ChatViewModel = hiltViewModel(),
onNavigateToSettings: () -> Unit
) {
val messages by viewModel.messages.collectAsStateWithLifecycle()
val modelState by viewModel.modelState.collectAsStateWithLifecycle()
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val currentResponse by viewModel.currentResponse.collectAsStateWithLifecycle()
val isDeveloperMode by viewModel.isDeveloperModeEnabled.collectAsStateWithLifecycle()
val chatStats by viewModel.chatStats.collectAsStateWithLifecycle()

var inputText by remember { mutableStateOf("") }
val listState = rememberLazyListState()
val scope = rememberCoroutineScope()
val focusRequester = remember { FocusRequester() }

// Auto-scroll to bottom when new messages arrive
LaunchedEffect(messages.size, currentResponse) {
if (messages.isNotEmpty() || currentResponse.isNotEmpty()) {
scope.launch {
listState.animateScrollToItem(
index = if (currentResponse.isNotEmpty()) messages.size else messages.size - 1
)
}
}
}

Box(
modifier = modifier.fillMaxSize()
) {
when (modelState) {
ModelState.NOT_DOWNLOADED, ModelState.ERROR -> {
// Show existing messages if any, but disable input
Column(
modifier = Modifier.fillMaxSize()
) {
// If no messages, show the download prompt centered
if (messages.isEmpty()) {
Box(
modifier = Modifier
.weight(1f)
.fillMaxWidth(),
contentAlignment = Alignment.Center
) {
Column(
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
Icon(
Icons.Default.CloudDownload,
contentDescription = null,
modifier = Modifier.size(64.dp),
tint = MaterialTheme.colorScheme.primary
)
Text(
text = "مدل Qwen لازم است",
style = MaterialTheme.typography.headlineSmall
)
Text(
text = "برای شروع گفت‌وگو، مدل هوش مصنوعی را از تنظیمات دانلود کنید",
style = MaterialTheme.typography.bodyMedium,
textAlign = TextAlign.Center,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
Button(onClick = onNavigateToSettings) {
Text("رفتن به تنظیمات")
}
}
}
} else {
// Show existing messages (read-only)
LazyColumn(
state = listState,
modifier = Modifier
.weight(1f)
.fillMaxWidth(),
contentPadding = PaddingValues(
start = Dimensions.Padding.content,
end = Dimensions.Padding.content,
top = Dimensions.Padding.content,
bottom = Spacing.lg
),
verticalArrangement = Arrangement.spacedBy(Spacing.sm)
) {
items(messages) { message ->
ChatMessageItem(message = message)
}
}
}

// Show model required banner at bottom
Surface(
modifier = Modifier.fillMaxWidth(),
color = MaterialTheme.colorScheme.tertiaryContainer,
tonalElevation = 3.dp
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
verticalAlignment = Alignment.CenterVertically,
horizontalArrangement = Arrangement.SpaceBetween
) {
Column(modifier = Modifier.weight(1f)) {
Text(
text = "مدل لازم است",
style = MaterialTheme.typography.titleSmall,
fontWeight = FontWeight.Bold
)
Text(
text = "برای ادامه گفت‌وگو دانلود کنید",
style = MaterialTheme.typography.bodySmall
)
}
Button(
onClick = onNavigateToSettings,
modifier = Modifier.padding(start = Spacing.sm)
) {
Text("دانلود")
}
}
}
}
}

ModelState.DOWNLOADING -> {
Box(
modifier = Modifier
.fillMaxSize(),
contentAlignment = Alignment.Center
) {
Column(
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
CircularProgressIndicator()
Text(
text = "در حال دانلود مدل...",
style = MaterialTheme.typography.bodyLarge
)
Text(
text = "برای دیدن پیشرفت، تنظیمات را بررسی کنید",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
}
}
}

ModelState.READY, ModelState.LOADING -> {
// Show loading overlay when model is loading
if (modelState == ModelState.LOADING) {
Box(
modifier = Modifier.fillMaxSize(),
contentAlignment = Alignment.Center
) {
Column(
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
CircularProgressIndicator()
Text(
text = "در حال آماده‌سازی مدل هوش مصنوعی...",
style = MaterialTheme.typography.bodyLarge
)
Text(
text = "چند ثانیه زمان می‌برد",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
}
}
} else {
Column(
modifier = Modifier.fillMaxSize()
) {
// Developer info card
AnimatedVisibility(
visible = isDeveloperMode && messages.isNotEmpty(),
enter = expandVertically() + fadeIn(),
exit = shrinkVertically() + fadeOut()
) {
DeveloperInfoCard(chatStats = chatStats)
}

// Token limit warning
AnimatedVisibility(
visible = chatStats.contextUsagePercent >= 80,
enter = expandVertically() + fadeIn(),
exit = shrinkVertically() + fadeOut()
) {
TokenLimitWarning(
usagePercent = chatStats.contextUsagePercent,
onClearChat = { viewModel.clearChat() }
)
}

// Clear chat button when there are messages
AnimatedVisibility(
visible = messages.isNotEmpty(),
enter = expandVertically() + fadeIn(),
exit = shrinkVertically() + fadeOut()
) {
Surface(
modifier = Modifier.fillMaxWidth(),
color = MaterialTheme.colorScheme.surface,
tonalElevation = 1.dp
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(
horizontal = Dimensions.Padding.content,
vertical = Spacing.sm
),
horizontalArrangement = Arrangement.End
) {
TextButton(
onClick = { viewModel.clearChat() },
colors = ButtonDefaults.textButtonColors(
contentColor = MaterialTheme.colorScheme.error
)
) {
Icon(
Icons.Outlined.Delete,
contentDescription = null,
modifier = Modifier.size(18.dp)
)
Spacer(modifier = Modifier.width(Spacing.xs))
Text("پاک کردن گفتگو")
}
}
}
}

// Messages list
LazyColumn(
state = listState,
modifier = Modifier
.weight(1f)
.fillMaxWidth(),
contentPadding = PaddingValues(
start = Dimensions.Padding.content,
end = Dimensions.Padding.content,
top = Dimensions.Padding.content,
bottom = Spacing.lg
),
verticalArrangement = Arrangement.spacedBy(Spacing.sm),
reverseLayout = false
) {
// Example prompts when no messages
if (messages.isEmpty() && currentResponse.isEmpty() && !uiState.isLoading) {
item {
ChatEmptyState(
onPromptClick = { prompt ->
viewModel.sendMessage(prompt)
}
)
}
}

items(messages) { message ->
ChatMessageItem(message = message)
}

// Show streaming response if available
if (currentResponse.isNotEmpty()) {
item {
ChatMessageItem(
message = com.mana.data.database.entity.ChatMessage(
message = currentResponse,
isUser = false,
timestamp = System.currentTimeMillis()
),
isStreaming = true
)
}
} else if (uiState.isLoading) {
// Show typing indicator while waiting for response
item {
TypingIndicator()
}
}
}

// Error message
AnimatedVisibility(
visible = uiState.error != null,
enter = expandVertically() + fadeIn(),
exit = shrinkVertically() + fadeOut()
) {
Card(
modifier = Modifier
.fillMaxWidth()
.padding(horizontal = Dimensions.Padding.content),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.errorContainer
)
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = uiState.error ?: "",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onErrorContainer,
modifier = Modifier.weight(1f)
)
IconButton(onClick = { viewModel.clearError() }) {
Icon(
Icons.Default.Close,
contentDescription = "بستن",
tint = MaterialTheme.colorScheme.onErrorContainer
)
}
}
}
}

// Input field
Surface(
modifier = Modifier.fillMaxWidth(),
color = MaterialTheme.colorScheme.surface,
tonalElevation = 3.dp
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
verticalAlignment = Alignment.Bottom
) {
OutlinedTextField(
value = inputText,
onValueChange = { inputText = it },
modifier = Modifier
.weight(1f)
.focusRequester(focusRequester),
placeholder = { Text("درباره هزینه‌هایتان بپرسید...") },
enabled = !uiState.isLoading,
maxLines = 3,
shape = RoundedCornerShape(24.dp)
)

Spacer(modifier = Modifier.width(Spacing.sm))

FilledIconButton(
onClick = {
viewModel.sendMessage(inputText)
inputText = ""
// Keep keyboard open by requesting focus
focusRequester.requestFocus()
},
enabled = inputText.isNotBlank() && !uiState.isLoading,
modifier = Modifier.size(48.dp)
) {
if (uiState.isLoading) {
CircularProgressIndicator(
modifier = Modifier.size(24.dp),
strokeWidth = 2.dp
)
} else {
Icon(
Icons.AutoMirrored.Filled.Send,
contentDescription = "ارسال"
)
}
}
}
}
}
}
}
}
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenLimitWarning(
usagePercent: Int,
onClearChat: () -> Unit,
modifier: Modifier = Modifier
) {
val backgroundColor = when {
usagePercent >= 95 -> MaterialTheme.colorScheme.errorContainer
usagePercent >= 90 -> Color(0xFFFFF3E0) // Orange container
else -> MaterialTheme.colorScheme.secondaryContainer
}

val contentColor = when {
usagePercent >= 95 -> MaterialTheme.colorScheme.onErrorContainer
usagePercent >= 90 -> Color(0xFF5D4037) // Dark orange
else -> MaterialTheme.colorScheme.onSecondaryContainer
}

val icon = when {
usagePercent >= 95 -> Icons.Default.Error
else -> Icons.Default.Warning
}

val message = when {
usagePercent >= 95 -> "حافظه گفتگو تقریبا پر است! برای ادامه، گفتگو را پاک کنید."
usagePercent >= 90 -> "حافظه گفتگو ${usagePercent}% پر است. بهتر است به زودی پاک کنید."
else -> "حافظه گفتگو ${usagePercent}% پر است."
}

Surface(
modifier = modifier.fillMaxWidth(),
color = backgroundColor,
tonalElevation = 2.dp
) {
Row(
modifier = Modifier
.fillMaxWidth()
.padding(Dimensions.Padding.content),
verticalAlignment = Alignment.CenterVertically,
horizontalArrangement = Arrangement.SpaceBetween
) {
Row(
modifier = Modifier.weight(1f),
verticalAlignment = Alignment.CenterVertically
) {
Icon(
icon,
contentDescription = null,
tint = contentColor,
modifier = Modifier.size(20.dp)
)
Spacer(modifier = Modifier.width(Spacing.sm))
Text(
text = message,
style = MaterialTheme.typography.bodyMedium,
color = contentColor
)
}
if (usagePercent >= 90) {
TextButton(
onClick = onClearChat,
colors = ButtonDefaults.textButtonColors(
contentColor = contentColor
)
) {
Text("پاک کردن", fontWeight = FontWeight.Bold)
}
}
}
}
}

@Composable
fun DeveloperInfoCard(
chatStats: ChatStats,
modifier: Modifier = Modifier
) {
var isExpanded by remember { mutableStateOf(false) }
val usageHint = remember(chatStats.contextUsagePercent) {
TokenUtils.getUsageColorHint(chatStats.contextUsagePercent)
}
val usageColor = when (usageHint) {
"critical" -> MaterialTheme.colorScheme.error
"warning" -> MaterialTheme.colorScheme.tertiary
else -> MaterialTheme.colorScheme.primary
}

Card(
modifier = modifier
.fillMaxWidth()
.padding(horizontal = Dimensions.Padding.content)
.padding(bottom = Spacing.sm),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
),
onClick = { isExpanded = !isExpanded }
) {
Column(
modifier = Modifier.padding(Dimensions.Padding.content)
) {
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically
) {
Row(
horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
verticalAlignment = Alignment.CenterVertically
) {
Icon(
Icons.Default.Code,
contentDescription = null,
modifier = Modifier.size(16.dp),
tint = MaterialTheme.colorScheme.onSurfaceVariant
)
Text(
text = "Qwen 2.5 • ${chatStats.messageCount} پیام",
style = MaterialTheme.typography.labelSmall,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
}

Row(
horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
verticalAlignment = Alignment.CenterVertically
) {
Text(
text = "${TokenUtils.formatNumber(chatStats.estimatedTokens)} توکن",
style = MaterialTheme.typography.labelSmall,
fontWeight = FontWeight.Medium,
color = usageColor
)
Icon(
if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
contentDescription = if (isExpanded) "جمع کردن" else "باز کردن",
modifier = Modifier.size(16.dp),
tint = MaterialTheme.colorScheme.onSurfaceVariant
)
}
}

AnimatedVisibility(
visible = isExpanded,
enter = expandVertically() + fadeIn(),
exit = shrinkVertically() + fadeOut()
) {
Column(
modifier = Modifier
.fillMaxWidth()
.padding(top = Spacing.sm),
verticalArrangement = Arrangement.spacedBy(Spacing.xs)
) {
HorizontalDivider(
color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
)

Spacer(modifier = Modifier.height(Spacing.xs))

// Context usage
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween
) {
Text(
text = "مصرف زمینه",
style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSurfaceVariant
)
Text(
text = "${chatStats.contextUsagePercent}%",
style = MaterialTheme.typography.bodySmall,
fontWeight = FontWeight.Medium,
color = usageColor
)
}

LinearProgressIndicator(
progress = { chatStats.contextUsagePercent / 100f },
modifier = Modifier
.fillMaxWidth()
.height(4.dp)
.clip(RoundedCornerShape(2.dp)),
color = usageColor,
trackColor = MaterialTheme.colorScheme.surfaceVariant,
drawStopIndicator = {}
)

Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.SpaceBetween
) {
Text(
text = "${TokenUtils.formatNumber(chatStats.estimatedTokens)} / ${TokenUtils.formatNumber(chatStats.maxTokens)} توکن",
style = MaterialTheme.typography.labelSmall,
color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
)
if (chatStats.systemPromptTokens > 0) {
Text(
text = "سیستم: ${TokenUtils.formatNumber(chatStats.systemPromptTokens)}",
style = MaterialTheme.typography.labelSmall,
color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
)
}
}
}
}
}
}
}

@Composable
fun TypingIndicator(
modifier: Modifier = Modifier
) {
Row(
modifier = modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.Start
) {
Card(
modifier = Modifier.widthIn(max = 280.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.secondaryContainer
)
) {
Row(
modifier = Modifier.padding(
horizontal = Dimensions.Padding.content,
vertical = Spacing.md
),
horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
verticalAlignment = Alignment.CenterVertically
) {
// Three animated dots
val infiniteTransition = rememberInfiniteTransition(label = "typing")

for (i in 0..2) {
val alpha by infiniteTransition.animateFloat(
initialValue = 0.3f,
targetValue = 1f,
animationSpec = infiniteRepeatable(
animation = keyframes {
durationMillis = 1200
0.3f at 0
1f at 400
0.3f at 800
},
repeatMode = RepeatMode.Restart,
initialStartOffset = StartOffset(i * 200)
),
label = "dot_alpha_$i"
)

Box(
modifier = Modifier
.size(8.dp)
.background(
color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha),
shape = RoundedCornerShape(50)
)
)
}
}
}
}
}

@Composable
fun ChatMessageItem(
message: com.mana.data.database.entity.ChatMessage,
isStreaming: Boolean = false
) {
val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
) {
Card(
modifier = Modifier
.widthIn(max = 280.dp)
.animateContentSize(),
colors = CardDefaults.cardColors(
containerColor = if (message.isUser)
MaterialTheme.colorScheme.primaryContainer
else
MaterialTheme.colorScheme.secondaryContainer
)
) {
Column(
modifier = Modifier.padding(Dimensions.Padding.content)
) {
Text(
text = message.message,
style = MaterialTheme.typography.bodyMedium,
color = if (message.isUser)
MaterialTheme.colorScheme.onPrimaryContainer
else
MaterialTheme.colorScheme.onSecondaryContainer
)

Spacer(modifier = Modifier.height(Spacing.xs))

Row(
verticalAlignment = Alignment.CenterVertically,
horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
) {
if (isStreaming) {
CircularProgressIndicator(
modifier = Modifier.size(12.dp),
strokeWidth = 1.dp
)
}
Text(
text = timeFormat.format(Date(message.timestamp)),
style = MaterialTheme.typography.labelSmall,
color = if (message.isUser)
MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
else
MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
)
}
}
}
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatEmptyState(
onPromptClick: (String) -> Unit
) {
val examplePrompts = listOf(
"این ماه برای غذا چقدر خرج کرده‌ام؟",
"بیشترین هزینه من چه بوده؟",
"از بودجه‌ام عبور کرده‌ام؟",
"این ماه را با ماه قبل مقایسه کن"
)

Column(
modifier = Modifier
.fillMaxWidth()
.padding(vertical = Spacing.lg),
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
Icon(
Icons.Default.AutoAwesome,
contentDescription = null,
modifier = Modifier.size(48.dp),
tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
)

Text(
text = "درباره خرج‌هایتان بپرسید",
style = MaterialTheme.typography.titleMedium,
color = MaterialTheme.colorScheme.onSurface
)

Text(
text = "یکی از این پیشنهادها را امتحان کنید",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onSurfaceVariant
)

Spacer(modifier = Modifier.height(Spacing.sm))

FlowRow(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
verticalArrangement = Arrangement.spacedBy(Spacing.sm)
) {
examplePrompts.forEach { prompt ->
SuggestionChip(
onClick = { onPromptClick(prompt) },
label = { Text(prompt) },
icon = {
Icon(
Icons.Default.AutoAwesome,
contentDescription = null,
modifier = Modifier.size(16.dp)
)
}
)
}
}
}
}
