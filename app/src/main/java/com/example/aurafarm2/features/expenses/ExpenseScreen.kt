package com.example.aurafarm2.features.expenses

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aurafarm2.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// ── Local persistence (DataStore) ────────────────────────────────

private val Context.expenseDataStore by preferencesDataStore(name = "expense_store")
private val EXPENSE_ENTRIES_KEY = stringPreferencesKey("expense_entries_json")

private val Context.incomeDataStore by preferencesDataStore(name = "income_store")
private val INCOME_ENTRIES_KEY = stringPreferencesKey("income_entries_json")

private data class ExpenseEntry(
    val id: String,
    val name: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

private data class IncomeEntry(
    val id: String,
    val source: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

private fun expenseEntriesFlow(context: Context): Flow<List<ExpenseEntry>> =
    context.expenseDataStore.data.map { prefs ->
        val raw = prefs[EXPENSE_ENTRIES_KEY] ?: "[]"
        decodeExpenseEntries(raw)
    }

private fun incomeEntriesFlow(context: Context): Flow<List<IncomeEntry>> =
    context.incomeDataStore.data.map { prefs ->
        val raw = prefs[INCOME_ENTRIES_KEY] ?: "[]"
        decodeIncomeEntries(raw)
    }

private suspend fun saveExpenseEntry(context: Context, entry: ExpenseEntry) {
    context.expenseDataStore.edit { prefs ->
        val current = decodeExpenseEntries(prefs[EXPENSE_ENTRIES_KEY] ?: "[]")
        prefs[EXPENSE_ENTRIES_KEY] = encodeExpenseEntries(current + entry)
    }
}

private fun encodeExpenseEntries(entries: List<ExpenseEntry>): String {
    val array = JSONArray()
    entries.forEach { entry ->
        val obj = JSONObject()
        obj.put("id", entry.id)
        obj.put("name", entry.name)
        obj.put("tag", entry.tag)
        obj.put("amount", entry.amount)
        obj.put("dateEpochDay", entry.dateEpochDay)
        array.put(obj)
    }
    return array.toString()
}

private fun decodeExpenseEntries(raw: String): List<ExpenseEntry> {
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    ExpenseEntry(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        tag = obj.optString("tag"),
                        amount = obj.optDouble("amount"),
                        dateEpochDay = obj.optLong("dateEpochDay")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

private fun decodeIncomeEntries(raw: String): List<IncomeEntry> {
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    IncomeEntry(
                        id = obj.optString("id"),
                        source = obj.optString("source"),
                        tag = obj.optString("tag"),
                        amount = obj.optDouble("amount"),
                        dateEpochDay = obj.optLong("dateEpochDay")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }
}

// ── UI models ───────────────────────────────────────────────────

private data class ExpenseSourceUi(
    val icon: ImageVector,
    val name: String,
    val tag: String,
    val amount: Double,
    val tagColor: Color
)

private data class ExpenseCategoryUi(
    val label: String,
    val amount: Double,
    val color: Color,
    val fraction: Float
)

private val DEFAULT_TAGS = listOf(
    "Groceries",
    "Dining",
    "Travel",
    "Subscription",
    "Utilities",
    "Health",
    "Shopping",
    "Other"
)

// ── Root screen ────────────────────────────────────────────────

@Composable
fun ExpenseScreen(onAddClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val expenseEntries by remember {
        expenseEntriesFlow(context)
    }.collectAsState(initial = emptyList())

    val incomeEntries by remember {
        incomeEntriesFlow(context)
    }.collectAsState(initial = emptyList())

    val totalExpenses by remember(expenseEntries) {
        derivedStateOf { expenseEntries.sumOf { it.amount } }
    }

    val totalIncome by remember(incomeEntries) {
        derivedStateOf { incomeEntries.sumOf { it.amount } }
    }

    val netAmount by remember(totalIncome, totalExpenses) {
        derivedStateOf { totalIncome - totalExpenses }
    }

    val categories by remember(expenseEntries, totalExpenses) {
        derivedStateOf {
            val grouped = expenseEntries.groupBy { it.tag.ifBlank { "Other" } }
            val raw = grouped.map { (tag, list) ->
                ExpenseCategoryUi(
                    label = tag,
                    amount = list.sumOf { it.amount },
                    color = colorForTag(tag),
                    fraction = 0f
                )
            }
            raw.sortedByDescending { it.amount }
                .take(3)
                .map { cat ->
                    cat.copy(fraction = if (totalExpenses > 0) (cat.amount / totalExpenses).toFloat() else 0f)
                }
        }
    }

    val sources by remember(expenseEntries) {
        derivedStateOf {
            expenseEntries
                .sortedByDescending { it.dateEpochDay }
                .map { entry ->
                    val date = LocalDate.ofEpochDay(entry.dateEpochDay)
                    val dateText = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    ExpenseSourceUi(
                        icon = iconForTag(entry.tag),
                        name = entry.name,
                        tag = "${entry.tag} • $dateText",
                        amount = entry.amount,
                        tagColor = colorForTag(entry.tag)
                    )
                }
        }
    }

    var showAddSheet by remember { mutableStateOf(false) }

    // Master trigger — flips to true on first composition,
    // driving every entry animation on the screen.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
            AnimatedSection(visible = visible, delayMs = 0) {
                ExpenseTopBar()
            }

            AnimatedSection(visible = visible, delayMs = 80) {
                HeroNetSection(visible = visible, net = netAmount)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedSection(visible = visible, delayMs = 180) {
                AllocationSection(visible = visible, categories = categories)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedSection(visible = visible, delayMs = 260) {
                RecentActivitySection(visible = visible, transactions = sources)
            }

            Spacer(Modifier.height(96.dp))
        }

        // FAB — springs in with a slight delay
        val fabScale by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "fab_scale"
        )

        FloatingAddButton(
            onClick = {
                onAddClick()
                showAddSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .scale(fabScale)
        )
    }

    if (showAddSheet) {
        AddExpenseBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, tag, amount, date ->
                scope.launch {
                    saveExpenseEntry(
                        context,
                        ExpenseEntry(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            tag = tag,
                            amount = amount,
                            dateEpochDay = date.toEpochDay()
                        )
                    )
                }
            }
        )
    }
}

// ── Generic animated wrapper ───────────────────────────────────
// Slides up + fades in. Every section uses this with a staggered delay.

@Composable
private fun AnimatedSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = delayMs,
            easing = FastOutSlowInEasing
        ),
        label = "section_offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMs,
            easing = FastOutSlowInEasing
        ),
        label = "section_alpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                this.alpha = alpha
            }
    ) {
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh)
                .border(1.dp, OutlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
        }

        Text(
            text = "Expenses",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = OnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Hero section ───────────────────────────────────────────────

@Composable
private fun HeroNetSection(visible: Boolean, net: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MONTHLY NET",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Animated count-up for the hero number
        CountUpNumber(target = net, visible = visible)

        Spacer(Modifier.height(20.dp))

        InOutPill(income = 0.0, expense = 0.0, actualIncome = null, actualExpense = null)
    }
}

// Counts up from 0 to target when visible flips true
@Composable
private fun CountUpNumber(target: Double, visible: Boolean) {
    val animatedValue by animateFloatAsState(
        targetValue = if (visible) target.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "count_up"
    )

    Text(
        text = formatCurrency(animatedValue.toDouble()),
        style = MaterialTheme.typography.displayLarge,
        color = OnSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun InOutPill(
    income: Double,
    expense: Double,
    actualIncome: Double?,
    actualExpense: Double?
) {
    val incomeValue = actualIncome ?: income
    val expenseValue = actualExpense ?: expense

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant, RoundedCornerShape(100.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(8.dp).background(IncomeGreen, CircleShape))
            Text(
                text = "+${formatCurrency(incomeValue, 0)} In",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }

        Box(Modifier.width(1.dp).height(16.dp).background(OutlineVariant))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(8.dp).background(ExpenseRed, CircleShape))
            Text(
                text = "-${formatCurrency(expenseValue, 0)} Out",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
    }
}

// ── Allocation section ─────────────────────────────────────────

@Composable
private fun AllocationSection(visible: Boolean, categories: List<ExpenseCategoryUi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Allocation",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        AllocationBar(visible = visible, categories = categories)

        Spacer(Modifier.height(12.dp))

        val tiles = if (categories.isEmpty()) {
            listOf(
                ExpenseCategoryUi("Essential", 0.0, EssentialDot, 0f),
                ExpenseCategoryUi("Luxury", 0.0, LuxuryDot, 0f),
                ExpenseCategoryUi("Extra", 0.0, ExtraDot, 0f)
            )
        } else {
            categories.padToSize(3) { index ->
                ExpenseCategoryUi(
                    label = "Other",
                    amount = 0.0,
                    color = fallbackColor(index),
                    fraction = 0f
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tiles.forEachIndexed { index, alloc ->
                // Each tile springs in with its own delay
                val tileScale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "tile_scale_$index"
                )
                val tileAlpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(300, delayMillis = 300 + index * 80),
                    label = "tile_alpha_$index"
                )

                AllocationTile(
                    allocation = alloc,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = tileScale
                            scaleY = tileScale
                            alpha = tileAlpha
                        }
                )
            }
        }
    }
}

@Composable
private fun AllocationBar(visible: Boolean, categories: List<ExpenseCategoryUi>) {
    // Bar animates width from 0 → full when visible
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "alloc_bar"
    )

    val gradientColorsRaw = if (categories.isEmpty()) {
        listOf(EssentialDot, LuxuryDot, ExtraDot)
    } else {
        categories.map { it.color }
    }
    val gradientColors = when {
        gradientColorsRaw.isEmpty() -> listOf(EssentialDot, LuxuryDot)
        gradientColorsRaw.size == 1 -> listOf(gradientColorsRaw.first(), gradientColorsRaw.first())
        else -> gradientColorsRaw
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(colors = gradientColors)
                )
        )
    }
}

@Composable
private fun AllocationTile(
    allocation: ExpenseCategoryUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(7.dp).background(allocation.color, CircleShape))
            Text(
                text = allocation.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = formatCurrency(allocation.amount, 0),
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Recent activity ────────────────────────────────────────────

@Composable
private fun RecentActivitySection(visible: Boolean, transactions: List<ExpenseSourceUi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "No expenses added yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            return
        }

        transactions.forEachIndexed { index, transaction ->
            // Each row slides in from right, staggered
            val rowOffset by animateFloatAsState(
                targetValue = if (visible) 0f else 60f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = 300 + index * 70,
                    easing = FastOutSlowInEasing
                ),
                label = "row_offset_$index"
            )
            val rowAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(300, delayMillis = 300 + index * 70),
                label = "row_alpha_$index"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = rowOffset.dp.toPx()
                    alpha = rowAlpha
                }
            ) {
                TransactionRow(transaction = transaction)
            }

            if (index < transactions.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 64.dp)
                        .height(1.dp)
                        .background(Divider)
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: ExpenseSourceUi) {
    // Press scale — spring back on release
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh)
                        .border(1.dp, OutlineVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .border(1.5.dp, Background, CircleShape)
                        .background(transaction.tagColor, CircleShape)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }

            Column {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                Text(
                    text = transaction.tag,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        Text(
            text = "-${formatCurrency(transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
    }
}

// ── Add expense sheet ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, tag: String, amount: Double, date: LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf(DEFAULT_TAGS.first()) }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val nameError = attemptedSave && name.isBlank()
    val tagError = attemptedSave && tag.isBlank()
    val amountError = attemptedSave && amountValue <= 0.0
    val amountFormatOk = amountText.isEmpty() || amountText.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))

    val isValid by remember(name, tag, amountText, amountFormatOk) {
        derivedStateOf { name.isNotBlank() && tag.isNotBlank() && amountValue > 0.0 && amountFormatOk }
    }

    if (showDatePicker) {
        val initialMillis = selectedDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLow,
        contentColor = OnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Merchant / Name") },
                singleLine = true,
                isError = nameError,
                supportingText = {
                    if (nameError) Text("Name is required.")
                },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = tagMenuExpanded,
                onExpandedChange = { tagMenuExpanded = !tagMenuExpanded }
            ) {
                OutlinedTextField(
                    value = tag,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagMenuExpanded)
                    },
                    isError = tagError,
                    supportingText = {
                        if (tagError) Text("Pick a category.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = tagMenuExpanded,
                    onDismissRequest = { tagMenuExpanded = false }
                ) {
                    DEFAULT_TAGS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                tag = option
                                tagMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))) {
                        amountText = input
                    }
                },
                label = { Text("Amount") },
                singleLine = true,
                isError = amountError || !amountFormatOk,
                supportingText = {
                    when {
                        !amountFormatOk -> Text("Use up to 7 digits and 2 decimals.")
                        amountError -> Text("Amount must be greater than 0.")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceVariant
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text(selectedDate.format(formatter))
                }
            }

            val saveScale by animateFloatAsState(
                targetValue = if (isValid) 1f else 0.98f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "save_button_scale"
            )

            FilledTonalButton(
                onClick = {
                    attemptedSave = true
                    if (isValid) {
                        onSave(name.trim(), tag.trim(), amountValue, selectedDate)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(saveScale)
            ) {
                Text("Save Expense")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────

private fun iconForTag(tag: String): ImageVector {
    return when (tag.trim().lowercase()) {
        "groceries" -> Icons.Outlined.ShoppingCart
        "dining" -> Icons.Outlined.LocalCafe
        "travel" -> Icons.Outlined.FlightTakeoff
        "subscription" -> Icons.Outlined.Subscriptions
        "utilities" -> Icons.Outlined.Bolt
        "health" -> Icons.Outlined.MonitorHeart
        "shopping" -> Icons.Outlined.ShoppingBag
        else -> Icons.Outlined.ReceiptLong
    }
}

private fun colorForTag(tag: String): Color {
    return when (tag.trim().lowercase()) {
        "groceries" -> EssentialDot
        "dining" -> LuxuryDot
        "travel" -> LuxuryDot
        "subscription" -> ExtraDot
        "utilities" -> SecondaryFixedDim
        "health" -> Secondary
        "shopping" -> ExtraDot
        else -> Secondary
    }
}

private fun fallbackColor(index: Int): Color {
    return listOf(EssentialDot, LuxuryDot, ExtraDot)[index % 3]
}

private fun formatCurrency(amount: Double, maxFractionDigits: Int = 2): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = maxFractionDigits
        minimumFractionDigits = if (maxFractionDigits == 0) 0 else 2
    }
    return formatter.format(amount)
}

private inline fun <T> List<T>.padToSize(size: Int, filler: (Int) -> T): List<T> {
    if (this.size >= size) return this
    val result = this.toMutableList()
    for (i in this.size until size) {
        result.add(filler(i))
    }
    return result
}

// ── FAB — no glow ──────────────────────────────────────────────

@Composable
private fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Press feedback
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_press"
    )

    FilledIconButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier
            .size(52.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(12.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = SurfaceContainerHigh
        )
    ) {
        // Rotate icon on press
        val iconRotation by animateFloatAsState(
            targetValue = if (pressed) 90f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "icon_rotate"
        )

        LaunchedEffect(pressed) {
            if (pressed) {
                delay(300)
                pressed = false
            }
        }

        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add expense",
            tint = Primary,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF13131A)
@Composable
fun ExpenseScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme {
        ExpenseScreen()
    }
}
