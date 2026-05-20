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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by remember { appSettingsFlow(context) }.collectAsState(initial = AppSettings())

    var visible by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var biometricMessage by remember { mutableStateOf<String?>(null) }

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
                    value = "Later",
                    onClick = {}
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

        AnimatedSettingsSection(visible, 280) {
            SettingsGroup(label = "SECURITY") {
                SettingsRow(
                    label = if (settings.hasPassword) "Change Password" else "Set Password",
                    value = if (settings.hasPassword) "Set" else "Not set",
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

        AnimatedSettingsSection(visible, 340) { SignOutButton() }

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
private fun PasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isValid = password.length >= 6 && password == confirmPassword

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
        title = { Text("Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors()
                )
                Text(
                    text = "Use at least 6 characters.",
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
private fun SignOutButton() {
    TextButton(
        onClick = {},
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
