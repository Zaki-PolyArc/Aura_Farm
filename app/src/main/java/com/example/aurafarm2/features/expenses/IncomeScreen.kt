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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurafarm2.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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

data class RecentIncomeEntry(
    val id: String,
    val source: String,
    val tag: String,
    val amount: Double,
    val date: String
)

// ── Root screen ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val incomes by remember { incomeEntriesFlow(context) }.collectAsState(initial = emptyList())
    val settings by remember { appSettingsFlow(context) }.collectAsState(initial = AppSettings())
    val symbol = currencySymbol(settings.currency)
    var showAddSheet by remember { mutableStateOf(false) }

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

    val recentIncomeEntries = incomes.sortedByDescending { it.dateEpochDay }.take(10).map { entry ->
        RecentIncomeEntry(
            id = entry.id,
            source = entry.source,
            tag = entry.tag,
            amount = entry.amount,
            date = LocalDate.ofEpochDay(entry.dateEpochDay)
                .format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))
        )
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

            AnimatedIncomeSection(visible, 80)  { IncomeHeroSection(visible, totalIncome, symbol) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 160) { IncomeStreamsSection(visible, incomeSources, symbol) }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 240) {
                RecentIncomeSection(
                    visible = visible,
                    entries = recentIncomeEntries,
                    currencySymbol = symbol,
                    onDelete = { entryId ->
                        coroutineScope.launch {
                            deleteIncomeEntry(context, entryId)
                        }
                    }
                )
            }

            Spacer(Modifier.height(48.dp))

            AnimatedIncomeSection(visible, 320) { BreakdownSection(visible, breakdownItems, symbol) }

            Spacer(Modifier.height(100.dp))
        }

        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            label         = "income_fab_scale"
        )
        IncomeFab(
            onClick  = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }

    if (showAddSheet) {
        AddIncomeBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { source, tag, amount, epochDay ->
                coroutineScope.launch {
                    saveIncomeEntry(
                        context,
                        IncomeEntry(
                            id = UUID.randomUUID().toString(),
                            source = source,
                            tag = tag,
                            amount = amount,
                            dateEpochDay = epochDay
                        )
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Long) -> Unit
) {
    var source by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    val tags = listOf("Salary", "Freelance", "Investments", "Gifts", "Other")
    var expanded by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf(tags[0]) }

    val initialMillis = remember { Instant.now().toEpochMilli() }
    var selectedDateMillis by remember { mutableLongStateOf(initialMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Validation
    val isValid = source.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Add Income", style = MaterialTheme.typography.headlineMedium, color = OnSurface)

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source Name (e.g. Acme Corp)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                )
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTag,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tags.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag) },
                            onClick = {
                                selectedTag = tag
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                )
            )

            // Date Field (Clickable)
            val formattedDate = remember(selectedDateMillis) {
                Instant.ofEpochMilli(selectedDateMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))
            }
            OutlinedTextField(
                value = formattedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.DateRange, contentDescription = "Select Date", tint = OnSurfaceVariant)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val epochDay = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.of("UTC")).toLocalDate().toEpochDay()
                    onSave(source, selectedTag, amt, epochDay)
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    disabledContainerColor = SurfaceVariant,
                    disabledContentColor = OnSurfaceVariant
                )
            ) {
                Text("Save Income", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK", color = Primary)
                }
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
        Spacer(Modifier.size(36.dp))

        Text(
            text  = "Focus",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )

        Spacer(Modifier.size(36.dp))
    }
}

// ── Hero ───────────────────────────────────────────────────────

@Composable
private fun IncomeHeroSection(visible: Boolean, totalIncome: Double, currencySymbol: String) {
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
            text      = "$currencySymbol${"%.2f".format(animVal)}",
            style     = MaterialTheme.typography.displayLarge,
            color     = Primary,
            textAlign = TextAlign.Center
        )
    }
}

// ── Income Streams section ─────────────────────────────────────

@Composable
private fun IncomeStreamsSection(
    visible: Boolean,
    sources: List<IncomeSource>,
    currencySymbol: String
) {
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
                    IncomeStreamCard(source = source, currencySymbol = currencySymbol)
                }

                if (index < sources.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun IncomeStreamCard(source: IncomeSource, currencySymbol: String) {
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
            text  = "$currencySymbol${"%.2f".format(source.amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Breakdown section ──────────────────────────────────────────

@Composable
private fun RecentIncomeSection(
    visible: Boolean,
    entries: List<RecentIncomeEntry>,
    currencySymbol: String,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Recent Income",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(20.dp))

        if (entries.isEmpty()) {
            Text(
                text = "No income entries yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            entries.forEachIndexed { index, entry ->
                val rowAlpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(300, delayMillis = 300 + index * 70),
                    label = "income_entry_alpha_$index"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = rowAlpha }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = entry.source,
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${entry.tag} • ${entry.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "+$currencySymbol${"%.2f".format(entry.amount)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSurface
                        )
                        IconButton(onClick = { onDelete(entry.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete income",
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (index < entries.lastIndex) {
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

@Composable
private fun BreakdownSection(
    visible: Boolean,
    items: List<IncomeBreakdownItem>,
    currencySymbol: String
) {
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
                            text  = "$currencySymbol${"%.2f".format(item.amount)} (${item.percent}%)",
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
