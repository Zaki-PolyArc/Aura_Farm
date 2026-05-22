package com.example.aurafarm2.features.expenses

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.aurafarm2.core.theme.Background
import com.example.aurafarm2.core.theme.Divider
import com.example.aurafarm2.core.theme.Error
import com.example.aurafarm2.core.theme.OnPrimary
import com.example.aurafarm2.core.theme.OnSurface
import com.example.aurafarm2.core.theme.OnSurfaceVariant
import com.example.aurafarm2.core.theme.Primary
import com.example.aurafarm2.core.theme.SurfaceContainerHigh
import com.example.aurafarm2.core.theme.SurfaceContainerLow
import com.example.aurafarm2.core.theme.SurfaceVariant
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by remember { appSettingsFlow(context) }.collectAsState(initial = AppSettings())
    val reminders by remember { remindersFlow(context) }.collectAsState(initial = emptyList())
    val budgets by remember { budgetsFlow(context) }.collectAsState(initial = emptyList())
    val recurringEntries by remember { recurringEntriesFlow(context) }.collectAsState(initial = emptyList())

    var visible by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var biometricMessage by remember { mutableStateOf<String?>(null) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        AnimatedSettingsSection(visible, 0) { SettingsTopBar() }

        Spacer(Modifier.height(32.dp))

        AnimatedSettingsSection(visible, 80) {
            ProfileSection(
                fullName = settings.fullName,
                email = settings.email,
                onEdit = { showProfileDialog = true }
            )
        }

        Spacer(Modifier.height(40.dp))

        AnimatedSettingsSection(visible, 160) {
            SettingsGroup(label = "ACCOUNT") {
                SettingsRow(
                    label = "Personal Details",
                    value = if (settings.fullName.isBlank() && settings.email.isBlank()) "Add" else "Edit",
                    onClick = { showProfileDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    label = "Notification Settings",
                    value = "${reminders.count { it.enabled }} active",
                    onClick = { showReminderDialog = true }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedSettingsSection(visible, 220) {
            SettingsGroup(label = "PREFERENCES") {
                SettingsRow(
                    label = "Appearance",
                    value = settings.appearance,
                    onClick = { showAppearanceDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    label = "Currency",
                    value = settings.currency,
                    onClick = { showCurrencyDialog = true }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedSettingsSection(visible, 260) {
            SettingsGroup(label = "FINANCE COACH") {
                SettingsRow(
                    label = "Budgets",
                    value = "${budgets.size} set",
                    onClick = { showBudgetDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    label = "Recurring Entries",
                    value = "${recurringEntries.count { it.enabled }} active",
                    onClick = { showRecurringDialog = true }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedSettingsSection(visible, 280) {
            SettingsGroup(label = "SECURITY") {
                SettingsRow(
                    label = if (settings.hasPassword) "Change Security PIN" else "Set Security PIN",
                    value = if (settings.hasPassword) "Active" else "Not set",
                    onClick = { showPasswordDialog = true }
                )
                SettingsDivider()
                BiometricRow(
                    enabled = settings.biometricEnabled,
                    onToggle = { enabled ->
                        handleBiometricToggle(
                            context = context,
                            enable = enabled,
                            onMessage = { biometricMessage = it },
                            onVerified = {
                                coroutineScope.launch {
                                    saveBiometricEnabled(context, enabled)
                                }
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedSettingsSection(visible, 340) { SignOutButton(onClick = { showSignOutConfirm = true }) }

        Spacer(Modifier.height(16.dp))

        AnimatedSettingsSection(visible, 380) {
            Text(
                text = "Version 2.4.0 (Build 1204)",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(80.dp))
    }

    if (showProfileDialog) {
        PersonalDetailsDialog(
            initialName = settings.fullName,
            initialEmail = settings.email,
            onDismiss = { showProfileDialog = false },
            onSave = { name, email ->
                coroutineScope.launch {
                    savePersonalDetails(context, name, email)
                }
                showProfileDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        ChoiceDialog(
            title = "Currency",
            options = listOf("USD ($)", "INR (₹)", "EUR (€)", "GBP (£)", "JPY (¥)"),
            selected = settings.currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                coroutineScope.launch { saveCurrency(context, it) }
                showCurrencyDialog = false
            }
        )
    }

    if (showAppearanceDialog) {
        ChoiceDialog(
            title = "Appearance",
            options = listOf("Dark", "Light", "System"),
            selected = settings.appearance,
            onDismiss = { showAppearanceDialog = false },
            onSelect = {
                coroutineScope.launch { saveAppearance(context, it) }
                showAppearanceDialog = false
            }
        )
    }

    if (showPasswordDialog) {
        PasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSave = {
                coroutineScope.launch { savePassword(context, it) }
                showPasswordDialog = false
            }
        )
    }

    if (showReminderDialog) {
        ReminderManagerDialog(
            reminders = reminders,
            onDismiss = { showReminderDialog = false },
            onSave = { reminder ->
                coroutineScope.launch {
                    saveReminder(context, reminder)
                    scheduleReminder(context, reminder)
                }
            },
            onDelete = { reminder ->
                coroutineScope.launch {
                    deleteReminder(context, reminder.id)
                    cancelReminder(context, reminder.id)
                }
            }
        )
    }

    if (showBudgetDialog) {
        BudgetManagerDialog(
            budgets = budgets,
            onDismiss = { showBudgetDialog = false },
            onSave = { budget ->
                coroutineScope.launch { saveBudget(context, budget) }
            },
            onDelete = { budget ->
                coroutineScope.launch { deleteBudget(context, budget.id) }
            }
        )
    }

    if (showRecurringDialog) {
        RecurringManagerDialog(
            entries = recurringEntries,
            onDismiss = { showRecurringDialog = false },
            onSave = { entry ->
                coroutineScope.launch { saveRecurringEntry(context, entry) }
            },
            onDelete = { entry ->
                coroutineScope.launch { deleteRecurringEntry(context, entry.id) }
            }
        )
    }

    biometricMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { biometricMessage = null },
            confirmButton = {
                TextButton(onClick = { biometricMessage = null }) {
                    Text("OK", color = Primary)
                }
            },
            title = { Text("Biometric Login") },
            text = { Text(message) },
            containerColor = SurfaceContainerHigh,
            titleContentColor = OnSurface,
            textContentColor = OnSurfaceVariant
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            clearPassword(context)
                        }
                        showSignOutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error,
                        contentColor = OnPrimary
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out? This will reset your security passcode and biometric settings.") },
            containerColor = SurfaceContainerHigh,
            titleContentColor = OnSurface,
            textContentColor = OnSurfaceVariant
        )
    }
}

@Composable
private fun AnimatedSettingsSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(450, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "settings_offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(350, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "settings_alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Focus",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )
    }
}

@Composable
private fun ProfileSection(
    fullName: String,
    email: String,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = fullName.ifBlank { "Add your name" },
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = email.ifBlank { "Add your email" },
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onEdit) {
            Text("Edit details", color = Primary)
        }
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow, RoundedCornerShape(12.dp)),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String?,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BiometricRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Biometric Login",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
            Text(
                text = if (enabled) "Enabled" else "Off",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(Divider)
    )
}

@Composable
private fun PersonalDetailsDialog(
    initialName: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    val isValid = name.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(name, email) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    disabledContainerColor = SurfaceVariant,
                    disabledContentColor = OnSurfaceVariant
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        title = { Text("Personal Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true,
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fieldColors()
                )
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) }
                        )
                        TextButton(onClick = { onSelect(option) }) {
                            Text(option, color = OnSurface)
                        }
                    }
                }
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )
}

@Composable
private fun ReminderManagerDialog(
    reminders: List<Reminder>,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Log expense") }
    var repeat by remember { mutableStateOf("Once") }
    var hour by remember { mutableStateOf("9") }
    var minute by remember { mutableStateOf("00") }
    var note by remember { mutableStateOf("") }
    val types = listOf("Log expense", "Log income", "Bill/payment", "Weekly money review")
    val repeats = listOf("Once", "Daily", "Weekly")
    val canSave = title.isNotBlank() && hour.toIntOrNull() in 0..23 && minute.toIntOrNull() in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Done", color = Primary) } },
        title = { Text("Notifications") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (reminders.isEmpty()) {
                    Text("No reminders yet.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                } else {
                    reminders.sortedBy { it.title }.forEach { reminder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(reminder.title, color = OnSurface)
                                Text(
                                    "${reminder.type} • %02d:%02d • ${reminder.repeat}".format(reminder.hour, reminder.minute),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = reminder.enabled,
                                onCheckedChange = { onSave(reminder.copy(enabled = it)) }
                            )
                            TextButton(onClick = { onDelete(reminder) }) {
                                Text("Delete", color = Error)
                            }
                        }
                    }
                }

                SettingsDivider()
                Text("New reminder", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true, colors = fieldColors())
                ChoiceChips(types, type) { type = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(hour, { hour = it }, label = { Text("Hour") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = fieldColors())
                    OutlinedTextField(minute, { minute = it }, label = { Text("Minute") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = fieldColors())
                }
                ChoiceChips(repeats, repeat) { repeat = it }
                OutlinedTextField(note, { note = it }, label = { Text("Note") }, colors = fieldColors())
                Button(
                    onClick = {
                        onSave(
                            Reminder(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                type = type,
                                hour = hour.toInt(),
                                minute = minute.toInt(),
                                repeat = repeat,
                                note = note,
                                enabled = true
                            )
                        )
                        title = ""
                        note = ""
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Text("Add reminder")
                }
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )
}

@Composable
private fun BudgetManagerDialog(
    budgets: List<Budget>,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
    onDelete: (Budget) -> Unit
) {
    var category by remember { mutableStateOf("Food") }
    var limit by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Shopping", "Health", "Other")
    val canSave = limit.toDoubleOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Done", color = Primary) } },
        title = { Text("Budgets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (budgets.isEmpty()) {
                    Text("No budgets yet.", color = OnSurfaceVariant)
                } else {
                    budgets.sortedBy { it.category }.forEach { budget ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${budget.category}: ${"%.0f".format(budget.limit)}", color = OnSurface)
                            TextButton(onClick = { onDelete(budget) }) { Text("Delete", color = Error) }
                        }
                    }
                }
                SettingsDivider()
                Text("Set monthly limit", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                ChoiceChips(categories, category) { category = it }
                OutlinedTextField(limit, { limit = it }, label = { Text("Limit") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = fieldColors())
                Button(
                    onClick = {
                        onSave(Budget(UUID.randomUUID().toString(), category, limit.toDouble()))
                        limit = ""
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Text("Save budget")
                }
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringManagerDialog(
    entries: List<RecurringEntry>,
    onDismiss: () -> Unit,
    onSave: (RecurringEntry) -> Unit,
    onDelete: (RecurringEntry) -> Unit
) {
    var kind by remember { mutableStateOf("Expense") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var amount by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("Monthly") }
    var selectedDateMillis by remember { mutableStateOf(Instant.now().toEpochMilli()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val expenseCategories = listOf("Food", "Transport", "Utilities", "Entertainment", "Shopping", "Health", "Other")
    val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Other")
    val categories = if (kind == "Income") incomeCategories else expenseCategories
    val repeats = listOf("Weekly", "Monthly", "Yearly")
    val canSave = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
    val date = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.of("UTC")).toLocalDate()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Done", color = Primary) } },
        title = { Text("Recurring Entries") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (entries.isEmpty()) {
                    Text("No recurring entries yet.", color = OnSurfaceVariant)
                } else {
                    entries.sortedBy { it.nextDueEpochDay }.forEach { entry ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.name, color = OnSurface)
                                Text(
                                    "${entry.kind} • ${entry.repeat} • ${LocalDate.ofEpochDay(entry.nextDueEpochDay).format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Switch(checked = entry.enabled, onCheckedChange = { onSave(entry.copy(enabled = it)) })
                            TextButton(onClick = { onDelete(entry) }) { Text("Delete", color = Error) }
                        }
                    }
                }
                SettingsDivider()
                Text("New recurring entry", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                ChoiceChips(listOf("Expense", "Income"), kind) {
                    kind = it
                    category = if (it == "Income") "Salary" else "Food"
                }
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, colors = fieldColors())
                ChoiceChips(categories, category) { category = it }
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = fieldColors())
                ChoiceChips(repeats, repeat) { repeat = it }
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Next due: ${date.format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))}", color = Primary)
                }
                Button(
                    onClick = {
                        onSave(
                            RecurringEntry(
                                id = UUID.randomUUID().toString(),
                                kind = kind,
                                name = name,
                                category = category,
                                amount = amount.toDouble(),
                                repeat = repeat,
                                nextDueEpochDay = date.toEpochDay(),
                                enabled = true
                            )
                        )
                        name = ""
                        amount = ""
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Text("Add recurring entry")
                }
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ChoiceChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    TextButton(onClick = { onSelect(option) }) {
                        Text(
                            option,
                            color = if (option == selected) Primary else OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isValid = password.length == 6 && password == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(password) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    disabledContainerColor = SurfaceVariant,
                    disabledContentColor = OnSurfaceVariant
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        title = { Text("Set Security PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            password = input
                        }
                    },
                    label = { Text("New 6-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            confirmPassword = input
                        }
                    },
                    label = { Text("Confirm 6-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = fieldColors()
                )
                Text(
                    text = "Enter exactly 6 numeric digits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        },
        containerColor = SurfaceContainerHigh,
        titleContentColor = OnSurface,
        textContentColor = OnSurfaceVariant
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    focusedBorderColor = Primary,
    unfocusedBorderColor = OnSurfaceVariant,
    focusedLabelColor = Primary,
    unfocusedLabelColor = OnSurfaceVariant
)

private fun handleBiometricToggle(
    context: Context,
    enable: Boolean,
    onMessage: (String) -> Unit,
    onVerified: () -> Unit
) {
    if (!enable) {
        onVerified()
        return
    }

    val activity = context.findFragmentActivity()
    if (activity == null) {
        onMessage("Biometric login is not available from this screen.")
        return
    }

    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val manager = BiometricManager.from(context)
    if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
        onMessage("Set up biometrics or a device screen lock in Android settings first.")
        return
    }

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onVerified()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onMessage(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onMessage("Biometric check failed. Try again.")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Enable Biometric Login")
        .setSubtitle("Confirm it is you")
        .setAllowedAuthenticators(authenticators)
        .build()

    prompt.authenticate(promptInfo)
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.labelLarge,
            color = Error
        )
    }
}
