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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

// ── Models ─────────────────────────────────────────────────

data class Transaction(
    val icon: ImageVector,
    val name: String,
    val category: String,
    val date: String,
    val amount: Double,
    val categoryColor: Color
)

data class AllocationCategory(
    val label: String,
    val percent: Int,
    val color: Color,
    val fraction: Float
)

// ── Root screen ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val expenses by remember { expenseEntriesFlow(context) }.collectAsState(initial = emptyList())
    val incomes by remember { incomeEntriesFlow(context) }.collectAsState(initial = emptyList())
    
    var showAddSheet by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Computations
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    val predefinedColors = listOf(EssentialDot, LuxuryDot, ExtraDot, Primary, Secondary)
    val predefinedIcons = listOf(Icons.Outlined.ShoppingBag, Icons.Outlined.Restaurant, Icons.Outlined.HomeWork, Icons.Outlined.Subscriptions)

    val groupedByCat = expenses.groupBy { it.tag }
    val allocations = groupedByCat.entries.mapIndexed { index, (tag, entries) ->
        val amount = entries.sumOf { it.amount }
        val percent = if (totalExpenses > 0) ((amount / totalExpenses) * 100).toInt() else 0
        val color = predefinedColors.getOrElse(index % predefinedColors.size) { EssentialDot }
        AllocationCategory(tag, percent, color, percent / 100f)
    }.sortedByDescending { it.percent }

    val transactions = expenses.sortedByDescending { it.dateEpochDay }.take(10).mapIndexed { index, entry ->
        val color = allocations.find { it.label == entry.tag }?.color ?: EssentialDot
        val icon = predefinedIcons.getOrElse(index % predefinedIcons.size) { Icons.Outlined.ShoppingBag }
        
        val date = LocalDate.ofEpochDay(entry.dateEpochDay)
        val dateStr = "${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.dayOfMonth}"
        
        Transaction(
            icon = icon,
            name = entry.name,
            category = entry.tag,
            date = dateStr,
            amount = entry.amount,
            categoryColor = color
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
            AnimatedSection(visible, 0) { ExpenseTopBar() }

            Spacer(Modifier.height(24.dp))

            AnimatedSection(visible, 80) { ExpenseHeroSection(visible, netBalance) }

            Spacer(Modifier.height(48.dp))

            AnimatedSection(visible, 180) { AllocationSection(visible, totalExpenses, allocations) }

            Spacer(Modifier.height(48.dp))

            AnimatedSection(visible, 260) { RecentActivitySection(visible, transactions) }

            Spacer(Modifier.height(100.dp))
        }

        val fabScale by animateFloatAsState(
            targetValue   = if (visible) 1f else 0f,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            label         = "fab_scale"
        )
        ExpenseFab(
            onClick  = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }

    if (showAddSheet) {
        AddExpenseBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, tag, amount, epochDay ->
                coroutineScope.launch {
                    saveExpenseEntry(
                        context,
                        ExpenseEntry(
                            id = UUID.randomUUID().toString(),
                            name = name,
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
fun AddExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    val categories = listOf("Essential", "Luxury", "Extra")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val initialMillis = remember { Instant.now().toEpochMilli() }
    var selectedDateMillis by remember { mutableLongStateOf(initialMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isValid = name.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true

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
            Text("Add Expense", style = MaterialTheme.typography.headlineMedium, color = OnSurface)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (e.g. Coffee)") },
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
                    value = selectedCategory,
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
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
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
                Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.of("UTC")).toLocalDate().toString()
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
                    onSave(name, selectedCategory, amt, epochDay)
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
                Text("Save Expense", style = MaterialTheme.typography.labelLarge)
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
private fun AnimatedSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 40f,
        animationSpec = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "alpha"
    )
    Box(Modifier.graphicsLayer { translationY = offsetY.dp.toPx(); this.alpha = alpha }) {
        content()
    }
}

// ── Top bar ────────────────────────────────────────────────────

@Composable
private fun ExpenseTopBar() {
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

// ── Hero section ───────────────────────────────────────────────

@Composable
private fun ExpenseHeroSection(visible: Boolean, netBalance: Double) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "MONTHLY NET BALANCE",
            style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        val animVal by animateFloatAsState(
            targetValue   = if (visible) netBalance.toFloat() else 0f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing),
            label         = "hero_count"
        )
        val sign = if (animVal >= 0) "+" else "-"
        val colorVal = if (animVal >= 0) Primary else Error
        
        Text(
            text      = "$sign$${"%.2f".format(Math.abs(animVal))}",
            style     = MaterialTheme.typography.displayLarge,
            color     = colorVal,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text      = "Income - Expenses",
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Allocation section ─────────────────────────────────────────

@Composable
private fun AllocationSection(visible: Boolean, totalExpenses: Double, allocations: List<AllocationCategory>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Allocation",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text  = "Total Exp: $${"%.0f".format(totalExpenses)}",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        SegmentedAllocationBar(visible = visible, allocations = allocations)

        Spacer(Modifier.height(20.dp))

        if (allocations.isEmpty()) {
            Text(
                text = "No expenses recorded yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                allocations.take(3).forEach { alloc ->
                    AllocationLegendItem(alloc)
                }
            }
        }
    }
}

@Composable
private fun SegmentedAllocationBar(visible: Boolean, allocations: List<AllocationCategory>) {
    val progress by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(900, delayMillis = 200, easing = FastOutSlowInEasing),
        label         = "alloc_bar"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (allocations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(SurfaceContainerHigh)
                )
            } else {
                allocations.forEach { alloc ->
                    if (alloc.fraction > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(alloc.fraction * progress + 0.001f)
                                .background(alloc.color)
                        )
                    }
                }
                if (progress < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - progress)
                            .background(SurfaceContainerHigh)
                    )
                }
            }
        }
    }
}

@Composable
private fun AllocationLegendItem(alloc: AllocationCategory) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(alloc.color, CircleShape)
            )
            Text(
                text  = alloc.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "${alloc.percent}%",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
    }
}

// ── Recent Activity ────────────────────────────────────────────

@Composable
private fun RecentActivitySection(visible: Boolean, transactions: List<Transaction>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Recent Activity",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text  = "FILTER",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                color = Primary
            )
        }

        Spacer(Modifier.height(24.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "No recent transactions.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        } else {
            transactions.forEachIndexed { index, tx ->
                val rowOffset by animateFloatAsState(
                    targetValue   = if (visible) 0f else 60f,
                    animationSpec = tween(400, delayMillis = 300 + index * 70, easing = FastOutSlowInEasing),
                    label         = "row_offset_$index"
                )
                val rowAlpha by animateFloatAsState(
                    targetValue   = if (visible) 1f else 0f,
                    animationSpec = tween(300, delayMillis = 300 + index * 70),
                    label         = "row_alpha_$index"
                )

                Box(
                    Modifier.graphicsLayer {
                        translationX = rowOffset.dp.toPx()
                        alpha        = rowAlpha
                    }
                ) {
                    TransactionRow(tx)
                }

                if (index < transactions.lastIndex) {
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
private fun TransactionRow(tx: Transaction) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "press_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = tx.icon,
                    contentDescription = null,
                    tint               = tx.categoryColor,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text  = tx.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "${tx.category} • ${tx.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }

        Text(
            text  = "-$${"%.2f".format(tx.amount)}",
            style = MaterialTheme.typography.titleSmall,
            color = OnSurface
        )
    }
}

// ── FAB ────────────────────────────────────────────────────────

@Composable
private fun ExpenseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "fab_press"
    )
    val iconRotation by animateFloatAsState(
        targetValue   = if (pressed) 90f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "fab_rotate"
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
            onClick = { pressed = true; onClick() },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector        = Icons.Outlined.Add,
                contentDescription = "Add expense",
                tint               = FabIcon,
                modifier           = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}
