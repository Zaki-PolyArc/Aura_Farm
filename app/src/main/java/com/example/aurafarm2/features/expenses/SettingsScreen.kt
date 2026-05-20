package com.example.aurafarm2.features.expenses

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurafarm2.core.theme.*

// ── Settings screen ────────────────────────────────────────────

@Composable
fun SettingsScreen() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        AnimatedSettingsSection(visible, 0) { SettingsTopBar() }

        Spacer(Modifier.height(32.dp))

        // Profile
        AnimatedSettingsSection(visible, 80) { ProfileSection() }

        Spacer(Modifier.height(40.dp))

        // Account group
        AnimatedSettingsSection(visible, 160) {
            SettingsGroup(
                label = "ACCOUNT",
                items = listOf(
                    SettingsItem(Icons.Outlined.Person,        "Personal Profile",      null,                      true),
                    SettingsItem(Icons.Outlined.Mail,          "Email Address",         "j.thorne@elevated.design", true),
                    SettingsItem(Icons.Outlined.Notifications, "Notification Settings", null,                      true),
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        // Preferences group
        AnimatedSettingsSection(visible, 220) {
            SettingsGroup(
                label = "PREFERENCES",
                items = listOf(
                    SettingsItem(Icons.Outlined.DarkMode,  "Appearance", "Dark",   true),
                    SettingsItem(Icons.Outlined.Payments,  "Currency",   "USD ($)", true),
                    SettingsItem(Icons.Outlined.Language,  "Language",   null,      true),
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        // Security group
        AnimatedSettingsSection(visible, 280) {
            SettingsGroup(
                label = "SECURITY",
                items = listOf(
                    SettingsItem(Icons.Outlined.Lock,        "Change Password",  null, true),
                    SettingsItem(Icons.Outlined.Fingerprint, "Biometric Login",  null, false),
                    SettingsItem(Icons.Outlined.Devices,     "Active Devices",   null, true),
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        // Sign out
        AnimatedSettingsSection(visible, 340) { SignOutButton() }

        Spacer(Modifier.height(16.dp))

        // Version
        AnimatedSettingsSection(visible, 380) {
            Text(
                text     = "Version 2.4.0 (Build 1204)",
                style    = MaterialTheme.typography.labelSmall,
                color    = OnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Animated wrapper ───────────────────────────────────────────

@Composable
private fun AnimatedSettingsSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 30f,
        animationSpec = tween(450, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "settings_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "settings_alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

// ── Top bar ────────────────────────────────────────────────────

@Composable
private fun SettingsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Person,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )
        }

        Text(
            text  = "Focus",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector        = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint               = Primary,   // active on this screen
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

// ── Profile section ────────────────────────────────────────────

@Composable
private fun ProfileSection() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar — large rounded square
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Person,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text  = "Julian Thorne",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text  = "j.thorne@elevated.design",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// ── Settings group ─────────────────────────────────────────────

data class SettingsItem(
    val icon: ImageVector,
    val label: String,
    val value: String?,
    val hasChevron: Boolean
)

@Composable
private fun SettingsGroup(
    label: String,
    items: List<SettingsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Section label — uppercase spaced
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color    = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Items card — no border, dark surface
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
        ) {
            items.forEachIndexed { index, item ->
                SettingsRow(item = item)

                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(1.dp)
                            .background(Divider)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text  = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (item.value != null) {
                Text(
                    text  = item.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            if (item.hasChevron) {
                Icon(
                    imageVector        = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint               = OnSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Sign out ───────────────────────────────────────────────────

@Composable
private fun SignOutButton() {
    TextButton(
        onClick  = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Sign Out",
            style = MaterialTheme.typography.labelLarge,
            color = Error
        )
    }
}
