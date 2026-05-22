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

data class Transaction(
    val id: String,
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
    val fraction: Float,
    val spent: Double,
    val limit: Double?
)

// ── Root screen ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val expenses by remember { expenseEntriesFlow(context) }.collectAsState(initial = emptyList())
    val incomes by remember { incomeEntriesFlow(context) }.collectAsState(initial = emptyList())
    val budgets by remember { budgetsFlow(context) }.collectAsState(initial = emptyList())
    val recurringEntries by remember { recurringEntriesFlow(context) }.collectAsState(initial = emptyList())
    val settings by remember { appSettingsFlow(context) }.collectAsState(initial = AppSettings())
    val symbol = currencySymbol(settings.currency)
    
    var showAddSheet by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntry?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Computations
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    val categoryColors = mapOf(
        "Food"          to EssentialDot,
        "Transport"     to LuxuryDot,
        "Utilities"     to ExtraDot,
        "Entertainment" to SalaryDot,
        "Shopping"      to FreelanceDot,
        "Health"        to InvestmentDot,
        "Other"         to Primary
    )

    val categoryIcons = mapOf(
        "Food"          to Icons.Outlined.Restaurant,
        "Transport"     to Icons.Outlined.DirectionsCar,
        "Utilities"     to Icons.Outlined.HomeWork,
        "Entertainment" to Icons.Outlined.PlayArrow,
        "Shopping"      to Icons.Outlined.ShoppingBag,
        "Health"        to Icons.Outlined.FavoriteBorder,
        "Other"         to Icons.Outlined.List
    )

    val groupedByCat = expenses.groupBy { it.tag }
    val allocations = groupedByCat.entries.map { (tag, entries) ->
        val amount = entries.sumOf { it.amount }
        val percent = if (totalExpenses > 0) ((amount / totalExpenses) * 100).toInt() else 0
        val color = categoryColors[tag] ?: Primary
        val limit = budgets.firstOrNull { it.category == tag }?.limit
        AllocationCategory(tag, percent, color, percent / 100f, amount, limit)
    }.sortedByDescending { it.percent }

    val dueExpenseEntries = recurringEntries
        .filter { it.enabled && it.kind == "Expense" && it.nextDueEpochDay <= LocalDate.now().plusDays(7).toEpochDay() }
        .sortedBy { it.nextDueEpochDay }

    val transactions = expenses.sortedByDescending { it.dateEpochDay }.map { entry ->
        val color = categoryColors[entry.tag] ?: Primary
        val icon = categoryIcons[entry.tag] ?: Icons.Outlined.ShoppingBag
        
        val date = LocalDate.ofEpochDay(entry.dateEpochDay)
        val dateStr = date.format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))
        
        Transaction(
            id = entry.id,
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

            AnimatedSection(visible, 80) { ExpenseHeroSection(visible, netBalance, symbol) }

            Spacer(Modifier.height(36.dp))

            AnimatedSection(visible, 140) {
                CoachSummarySection(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netBalance = netBalance,
                    allocations = allocations,
                    currencySymbol = symbol
                )
            }

            Spacer(Modifier.height(48.dp))

            AnimatedSection(visible, 220) { AllocationSection(visible, totalExpenses, allocations, symbol) }

            Spacer(Modifier.height(48.dp))

            AnimatedSection(visible, 300) {
                DueRecurringExpenseSection(
                    entries = dueExpenseEntries,
                    currencySymbol = symbol,
                    onAddNow = { recurring ->
                        coroutineScope.launch {
                            saveExpenseEntry(
                                context,
                                ExpenseEntry(
                                    id = UUID.randomUUID().toString(),
                                    name = recurring.name,
                                    tag = recurring.category,
                                    amount = recurring.amount,
                                    dateEpochDay = LocalDate.now().toEpochDay()
                                )
                            )
                            saveRecurringEntry(
                                context,
                                recurring.copy(nextDueEpochDay = nextRecurringDue(recurring).toEpochDay())
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(48.dp))

            AnimatedSection(visible, 380) {
                RecentActivitySection(
                    visible = visible,
                    transactions = transactions,
                    currencySymbol = symbol,
                    onEdit = { entryId ->
                        editingExpense = expenses.find { it.id == entryId }
                    },
                    onDelete = { entryId ->
                        coroutineScope.launch {
                            deleteExpenseEntry(context, entryId)
                        }
                    }
                )
            }

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

    if (showAddSheet || editingExpense != null) {
        ExpenseBottomSheet(
            existing = editingExpense,
            onDismiss = { showAddSheet = false; editingExpense = null },
            onSave = { name, tag, amount, epochDay ->
                coroutineScope.launch {
                    val current = editingExpense
                    if (current != null) {
                        updateExpenseEntry(
                            context,
                            ExpenseEntry(
                                id = current.id,
                                name = name,
                                tag = tag,
                                amount = amount,
                                dateEpochDay = epochDay
                            )
                        )
                    } else {
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
                showAddSheet = false
                editingExpense = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheet(
    existing: ExpenseEntry? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Long) -> Unit
) {
    val isEditMode = existing != null
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var amount by remember(existing) { mutableStateOf(existing?.let { "%.2f".format(it.amount) } ?: "") }
    
    val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Shopping", "Health", "Other")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember(existing) { mutableStateOf(existing?.tag ?: categories[0]) }

    val initialMillis = remember(existing) {
        if (existing != null) {
            LocalDate.ofEpochDay(existing.dateEpochDay)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        } else {
            Instant.now().toEpochMilli()
        }
    }
    var selectedDateMillis by remember(existing) { mutableLongStateOf(initialMillis) }
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
            Text(if (isEditMode) "Edit Expense" else "Add Expense", style = MaterialTheme.typography.headlineMedium, color = OnSurface)

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
                Text(if (isEditMode) "Update Expense" else "Save Expense", style = MaterialTheme.typography.labelLarge)
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
        Spacer(Modifier.size(36.dp))

        Text(
            text  = "Focus",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )

        Spacer(Modifier.size(36.dp))
    }
}

// ── Hero section ───────────────────────────────────────────────

@Composable
private fun ExpenseHeroSection(visible: Boolean, netBalance: Double, currencySymbol: String) {
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
            text      = "$sign$currencySymbol${"%.2f".format(Math.abs(animVal))}",
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
private fun CoachSummarySection(
    totalIncome: Double,
    totalExpenses: Double,
    netBalance: Double,
    allocations: List<AllocationCategory>,
    currencySymbol: String
) {
    val topCategory = allocations.maxByOrNull { it.spent }
    val insight = when {
        totalExpenses <= 0 -> "Start with one expense entry to unlock useful patterns."
        topCategory != null -> "${topCategory.label} is your biggest spending category this month."
        else -> "Your month is still taking shape."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(20.dp)
    ) {
        Text("Finance Coach", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
            CoachMetric("Income", "$currencySymbol${"%.0f".format(totalIncome)}", Modifier.weight(1f))
            CoachMetric("Spent", "$currencySymbol${"%.0f".format(totalExpenses)}", Modifier.weight(1f))
            CoachMetric("Left", "$currencySymbol${"%.0f".format(netBalance)}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Text(insight, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
    }
}

@Composable
private fun CoachMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = OnSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllocationSection(
    visible: Boolean,
    totalExpenses: Double,
    allocations: List<AllocationCategory>,
    currencySymbol: String
) {
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
                text  = "Total Exp: $currencySymbol${"%.0f".format(totalExpenses)}",
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                allocations.forEach { alloc ->
                    AllocationLegendItem(alloc, currencySymbol)
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
private fun AllocationLegendItem(alloc: AllocationCategory, currencySymbol: String) {
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
        alloc.limit?.let { limit ->
            val used = if (limit > 0) ((alloc.spent / limit) * 100).toInt() else 0
            Text(
                text = "$currencySymbol${"%.0f".format(alloc.spent)} / $currencySymbol${"%.0f".format(limit)} ($used%)",
                style = MaterialTheme.typography.bodySmall,
                color = if (alloc.spent > limit) Error else OnSurfaceVariant
            )
        }
    }
}

// ── Recent Activity ────────────────────────────────────────────

@Composable
private fun DueRecurringExpenseSection(
    entries: List<RecurringEntry>,
    currencySymbol: String,
    onAddNow: (RecurringEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text("Due Soon", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        Spacer(Modifier.height(16.dp))
        if (entries.isEmpty()) {
            Text("No recurring expenses due soon.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        } else {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.name, style = MaterialTheme.typography.titleMedium, color = OnSurface)
                        Text(
                            "${entry.category} • ${LocalDate.ofEpochDay(entry.nextDueEpochDay).format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onAddNow(entry) }) {
                        Text("Add $currencySymbol${"%.0f".format(entry.amount)}", color = Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivitySection(
    visible: Boolean,
    transactions: List<Transaction>,
    currencySymbol: String,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
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
                    TransactionRow(tx = tx, currencySymbol = currencySymbol, onEdit = onEdit, onDelete = onDelete)
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
private fun TransactionRow(
    tx: Transaction,
    currencySymbol: String,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = "-$currencySymbol${"%.2f".format(tx.amount)}",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurface
            )
            IconButton(onClick = { onEdit(tx.id) }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit expense",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = { onDelete(tx.id) }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete expense",
                    tint = Error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun nextRecurringDue(entry: RecurringEntry): LocalDate {
    val base = LocalDate.ofEpochDay(entry.nextDueEpochDay)
    return when (entry.repeat) {
        "Weekly" -> base.plusWeeks(1)
        "Yearly" -> base.plusYears(1)
        else -> base.plusMonths(1)
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
