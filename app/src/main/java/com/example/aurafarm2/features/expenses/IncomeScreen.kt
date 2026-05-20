package com.example.aurafarm2.features.expenses

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurafarm2.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

// ── Models ─────────────────────────────────────────────────

data class IncomeSource(
    val icon: ImageVector,
    val name: String,
    val subtitle: String,
    val amount: Double,
    val percent: Int,
    val color: Color
)

data class IncomeBreakdownItem(
    val label: String,
    val amount: Double,
    val percent: Int,
    val color: Color,
    val fraction: Float
)

// ── Root screen ────────────────────────────────────────────────

@Composable
fun IncomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val incomes by remember { incomeEntriesFlow(context) }.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val totalIncome = incomes.sumOf { it.amount }
    
    // Group incomes dynamically
    val groupedBySource = incomes.groupBy { it.source }
    val predefinedColors = listOf(SalaryDot, FreelanceDot, InvestmentDot, Primary, Secondary)
    val predefinedIcons = listOf(Icons.Outlined.Work, Icons.Outlined.Draw, Icons.Outlined.AccountBalance, Icons.Outlined.Payments, Icons.Outlined.Savings)
    
    val incomeSources = groupedBySource.entries.mapIndexed { index, (source, entries) ->
        val amount = entries.sumOf { it.amount }
        val percent = if (totalIncome > 0) ((amount / totalIncome) * 100).toInt() else 0
        val color = predefinedColors.getOrElse(index % predefinedColors.size) { Primary }
        val icon = predefinedIcons.getOrElse(index % predefinedIcons.size) { Icons.Outlined.Payments }
        val tag = entries.firstOrNull()?.tag ?: ""
        
        IncomeSource(icon, source, tag, amount, percent, color)
    }.sortedByDescending { it.amount }

    val breakdownItems = incomeSources.map {
        IncomeBreakdownItem(it.name, it.amount, it.percent, it.color, it.percent / 100f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedIncomeSection(visible, 0)   { IncomeTopBar() }

            Spacer(Modifier.height(24.dp))

            AnimatedIncomeSection(visible, 80)  { IncomeHeroSection(visible, totalIncome) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 160) { IncomeStreamsSection(visible, incomeSources) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 240) { BreakdownSection(visible, breakdownItems) }

            Spacer(Modifier.height(100.dp))
        }

        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            label         = "income_fab_scale"
        )
        IncomeFab(
            onClick  = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }

    if (showAddDialog) {
        AddIncomeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { source, tag, amount ->
                coroutineScope.launch {
                    saveIncomeEntry(
                        context,
                        IncomeEntry(
                            id = UUID.randomUUID().toString(),
                            source = source.ifEmpty { "Other" },
                            tag = tag.ifEmpty { "General" },
                            amount = amount,
                            dateEpochDay = LocalDate.now().toEpochDay()
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun AddIncomeDialog(onDismiss: () -> Unit, onSave: (String, String, Double) -> Unit) {
    var source by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income", color = OnSurface) },
        containerColor = SurfaceContainerHigh,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = source, 
                    onValueChange = { source = it }, 
                    label = { Text("Source (e.g. Salary)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                OutlinedTextField(
                    value = tag, 
                    onValueChange = { tag = it }, 
                    label = { Text("Tag (e.g. Work)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                OutlinedTextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    onSave(source, tag, amt)
                    onDismiss()
                }
            }) { Text("Save", color = Primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceVariant) }
        }
    )
}

// ── Animated wrapper ───────────────────────────────────────────

@Composable
private fun AnimatedIncomeSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "income_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "income_alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

// ── Top bar ────────────────────────────────────────────────────

@Composable
private fun IncomeTopBar() {
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
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

// ── Hero ───────────────────────────────────────────────────────

@Composable
private fun IncomeHeroSection(visible: Boolean, totalIncome: Double) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "TOTAL INCOME",
            style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Count-up
        val animVal by animateFloatAsState(
            targetValue   = if (visible) totalIncome.toFloat() else 0f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing),
            label         = "income_hero_count"
        )
        Text(
            text      = "$${"%.2f".format(animVal)}",
            style     = MaterialTheme.typography.displayLarge,
            color     = Primary,
            textAlign = TextAlign.Center
        )
    }
}

// ── Income Streams section ─────────────────────────────────────

@Composable
private fun IncomeStreamsSection(visible: Boolean, sources: List<IncomeSource>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Income Streams",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(20.dp))

        if (sources.isEmpty()) {
            Text(
                text = "No income sources yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            sources.forEachIndexed { index, source ->
                val cardAlpha by animateFloatAsState(
                    targetValue   = if (visible) 1f else 0f,
                    animationSpec = tween(400, delayMillis = 200 + index * 100),
                    label         = "card_alpha_$index"
                )
                val cardOffset by animateFloatAsState(
                    targetValue   = if (visible) 0f else 30f,
                    animationSpec = tween(400, delayMillis = 200 + index * 100, easing = FastOutSlowInEasing),
                    label         = "card_offset_$index"
                )

                Box(
                    Modifier.graphicsLayer {
                        alpha        = cardAlpha
                        translationY = cardOffset.dp.toPx()
                    }
                ) {
                    IncomeStreamCard(source = source)
                }

                if (index < sources.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun IncomeStreamCard(source: IncomeSource) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(20.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Icon(
                imageVector        = source.icon,
                contentDescription = null,
                tint               = OnSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
            Text(
                text  = "${source.percent}%",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text  = source.name,
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )

        Text(
            text  = source.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(source.percent / 100f)
                .height(1.dp)
                .background(source.color)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineVariant)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text  = "$${"%.2f".format(source.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Breakdown section ──────────────────────────────────────────

@Composable
private fun BreakdownSection(visible: Boolean, items: List<IncomeBreakdownItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text  = "Breakdown",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(20.dp)
        ) {
            val barProgress by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(900, delayMillis = 300, easing = FastOutSlowInEasing),
                label         = "breakdown_bar"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
            ) {
                Row(Modifier.fillMaxSize()) {
                    if (items.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize().background(SurfaceContainerHigh)
                        )
                    } else {
                        items.forEach { item ->
                            if (item.fraction > 0f) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(item.fraction * barProgress + 0.001f)
                                        .background(item.color)
                                )
                            }
                        }
                        if (barProgress < 1f) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .weight(1f - barProgress)
                                    .background(SurfaceContainerHigh)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (items.isEmpty()) {
                 Text(
                    text = "No breakdown available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                items.forEachIndexed { index, item ->
                    val itemAlpha by animateFloatAsState(
                        targetValue   = if (visible) 1f else 0f,
                        animationSpec = tween(300, delayMillis = 400 + index * 80),
                        label         = "breakdown_item_$index"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = itemAlpha }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(item.color, CircleShape)
                            )
                            Text(
                                text  = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                        Text(
                            text  = "$${"%.2f".format(item.amount)} (${item.percent}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }

                    if (index < items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Divider)
                        )
                    }
                }
            }
        }
    }
}

// ── FAB ────────────────────────────────────────────────────────

@Composable
private fun IncomeFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "income_fab_press"
    )
    val iconRotation by animateFloatAsState(
        targetValue   = if (pressed) 90f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "income_fab_rotate"
    )

    LaunchedEffect(pressed) {
        if (pressed) { delay(280); pressed = false }
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(pressScale)
            .clip(RoundedCornerShape(14.dp))
            .background(FabBackground),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick  = { pressed = true; onClick() },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector        = Icons.Outlined.Add,
                contentDescription = "Add income",
                tint               = FabIcon,
                modifier           = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}
