package com.example.aurafarm2.features.expenses

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.KeyboardBackspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.aurafarm2.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SecurityLockScreen(
    settings: AppSettings,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Vibration/shake translation effect for wrong PIN
    val shakeOffset = remember { Animatable(0f) }

    // Instant launch biometric prompt on mount if enabled
    LaunchedEffect(Unit) {
        if (settings.biometricEnabled) {
            triggerBiometricPrompt(
                context = context,
                onSuccess = onAuthenticated,
                onFailure = { /* Silent fallback to PIN pad */ }
            )
        }
    }

    // Check PIN when length reaches 6
    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 6) {
            val isCorrect = verifyPassword(context, enteredPin)
            if (isCorrect) {
                onAuthenticated()
            } else {
                isError = true
                // Shake animation feedback
                coroutineScope.launch {
                    val spec = repeatable<Float>(
                        iterations = 4,
                        animation = tween(durationMillis = 50, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                    shakeOffset.animateTo(12f, animationSpec = spec)
                    shakeOffset.animateTo(0f, animationSpec = spec)
                }
                delay(400)
                enteredPin = ""
                isError = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            // ── Top Header ───────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "App Locked",
                        tint = OnPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "AURA FARM",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Primary
                )

                Text(
                    text = "Enter Security PIN to unlock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // ── PIN Dot Indicators ──────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .offset(x = shakeOffset.value.dp)
                    .padding(vertical = 32.dp)
            ) {
                repeat(6) { index ->
                    val isOccupied = index < enteredPin.length
                    val dotColor = when {
                        isError -> Error
                        isOccupied -> Primary
                        else -> OnSurfaceVariant.copy(alpha = 0.2f)
                    }
                    val scale = if (isOccupied) 1.2f else 1.0f
                    val animatedScale by animateFloatAsState(
                        targetValue = scale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dot_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = 1.dp,
                                color = if (isOccupied) Color.Transparent else OnSurfaceVariant.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .aspectRatio(1f)
                    )
                }
            }

            // ── Keypad Grid ─────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Biometric", "0", "Delete")
                )

                keypadRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                key = key,
                                biometricEnabled = settings.biometricEnabled,
                                onClick = {
                                    when (key) {
                                        "Delete" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        }
                                        "Biometric" -> {
                                            if (settings.biometricEnabled) {
                                                triggerBiometricPrompt(
                                                    context = context,
                                                    onSuccess = onAuthenticated,
                                                    onFailure = {}
                                                )
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 6) {
                                                enteredPin += key
                                            }
                                        }
                                    }
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
private fun KeypadButton(
    key: String,
    biometricEnabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "key_scale"
    )

    val isSpecial = key == "Biometric" || key == "Delete"
    val showButton = key != "Biometric" || biometricEnabled

    Box(
        modifier = Modifier
            .size(76.dp)
            .padding(2.dp)
    ) {
        if (showButton) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isPressed) {
                            if (isSpecial) SurfaceContainerHighest else Primary.copy(alpha = 0.15f)
                        } else {
                            if (isSpecial) Color.Transparent else SurfaceContainerLow
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSpecial) Color.Transparent else Divider.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            ) {
                when (key) {
                    "Biometric" -> {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Use Biometrics",
                            tint = Primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    "Delete" -> {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardBackspace,
                            contentDescription = "Backspace",
                            tint = OnSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp
                            ),
                            color = OnSurface
                        )
                    }
                }
            }
        }
    }
}

private fun triggerBiometricPrompt(
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val activity = context.findFragmentActivity() ?: return
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val manager = BiometricManager.from(context)

    if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
        onFailure("Biometrics unavailable")
        return
    }

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFailure("Authentication failed")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Aura Farm")
        .setSubtitle("Authenticate using your credentials")
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
