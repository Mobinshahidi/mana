package com.mana.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.ui.components.PennyWiseScaffold
import com.mana.ui.theme.Spacing
import com.mana.ui.viewmodel.PermissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
onPermissionGranted: () -> Unit,
viewModel: PermissionViewModel = hiltViewModel(),
modifier: Modifier = Modifier
) {
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val context = LocalContext.current

// Track which permissions have been granted
var readSmsGranted by remember { mutableStateOf(false) }
var receiveSmsGranted by remember { mutableStateOf(false) }

// Permission launcher for multiple permissions
val multiplePermissionLauncher = rememberLauncherForActivityResult(
contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
readSmsGranted = permissions[Manifest.permission.READ_SMS] == true
receiveSmsGranted = permissions[Manifest.permission.RECEIVE_SMS] == true

// Both SMS permissions granted (or at least READ_SMS for basic functionality)
if (readSmsGranted) {
viewModel.onPermissionResult(true)
onPermissionGranted()
} else {
viewModel.onPermissionDenied()
}
}

PennyWiseScaffold(
modifier = modifier,
transparentTopBar = true
) { innerPadding ->
Column(
modifier = Modifier
.fillMaxSize()
.verticalScroll(rememberScrollState())
.padding(innerPadding)
.padding(Spacing.lg),
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.Center
) {
Icon(
imageVector = Icons.Filled.MailOutline,
contentDescription = null,
modifier = Modifier.size(120.dp),
tint = MaterialTheme.colorScheme.primary
)

Spacer(modifier = Modifier.height(Spacing.xl))

Text(
text = "فعال‌سازی تشخیص خودکار تراکنش‌ها",
style = MaterialTheme.typography.headlineMedium,
textAlign = TextAlign.Center
)

Spacer(modifier = Modifier.height(Spacing.md))

Text(
text = "مانا می‌تواند تراکنش‌های بانکی را از پیامک‌ها به‌صورت خودکار شناسایی و دسته‌بندی کند و در زمان شما صرفه‌جویی کند.",
style = MaterialTheme.typography.bodyLarge,
textAlign = TextAlign.Center,
color = MaterialTheme.colorScheme.onSurfaceVariant
)

Spacer(modifier = Modifier.height(Spacing.lg))

// Privacy card
Card(
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.primaryContainer
),
modifier = Modifier.fillMaxWidth()
) {
Column(
modifier = Modifier.padding(Spacing.md)
) {
Text(
text = "حریم خصوصی شما مهم است",
style = MaterialTheme.typography.titleSmall,
color = MaterialTheme.colorScheme.onPrimaryContainer
)
Spacer(modifier = Modifier.height(Spacing.sm))
Text(
text = "• فقط پیامک‌های تراکنش بررسی می‌شوند\n" +
"• همه داده‌ها روی دستگاه شما می‌مانند\n" +
"• پیامک‌های شخصی خوانده نمی‌شوند\n" +
"• هر زمان از تنظیمات می‌توانید دسترسی را قطع کنید",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onPrimaryContainer
)
}
}

Spacer(modifier = Modifier.height(Spacing.xl))

// Show rationale if permission was denied
if (uiState.showRationale) {
Card(
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.errorContainer
),
modifier = Modifier.fillMaxWidth()
) {
Text(
text = "بدون دسترسی به پیامک، باید همه تراکنش‌ها را دستی ثبت کنید. " +
"ما فقط پیامک‌های بانکی را می‌خوانیم، نه گفتگوهای شخصی.",
style = MaterialTheme.typography.bodyMedium,
color = MaterialTheme.colorScheme.onErrorContainer,
modifier = Modifier.padding(Spacing.md)
)
}
Spacer(modifier = Modifier.height(Spacing.md))
}

// Primary action button
Button(
onClick = {
// Request both READ_SMS and RECEIVE_SMS permissions
val permissions = mutableListOf(
Manifest.permission.READ_SMS,
Manifest.permission.RECEIVE_SMS
)

// Add POST_NOTIFICATIONS for Android 13+
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
permissions.add(Manifest.permission.POST_NOTIFICATIONS)
}

multiplePermissionLauncher.launch(permissions.toTypedArray())
},
modifier = Modifier.fillMaxWidth()
) {
Text("فعال‌سازی تشخیص خودکار")
}
}
}
}
