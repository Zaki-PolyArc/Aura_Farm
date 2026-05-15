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

private val Context.incomeDataStore by preferencesDataStore(name = "income_store")
private val INCOME_ENTRIES_KEY = stringPreferencesKey("income_entries_json")

private val Context.expenseDataStore by preferencesDataStore(name = "expense_store")
private val EXPENSE_ENTRIES_KEY = stringPreferencesKey("expense_entries_json")

private data class IncomeEntry(
    val id: String,
    val source: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

private data class ExpenseEntry(
    val id: String,
    val name: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

private fun incomeEntriesFlow(context: Context): Flow<List<IncomeEntry>> =
    context.incomeDataStore.data.map { prefs ->
        val raw = prefs[INCOME_ENTRIES_KEY] ?: "[]"
        decodeIncomeEntries(raw)
    }

private fun expenseEntriesFlow(context: Context): Flow<List<ExpenseEntry>> =
    context.expenseDataStore.data.map { prefs ->
        val raw = prefs[EXPENSE_ENTRIES_KEY] ?: "[]"
        decodeExpenseEntries(raw)
    }

private suspend fun saveIncomeEntry(context: Context, entry: IncomeEntry) {
    context.incomeDataStore.edit { prefs ->
        val current = decodeIncomeEntries(prefs[INCOME_ENTRIES_KEY] ?: "[]")
        prefs[INCOME_ENTRIES_KEY] = encodeIncomeEntries(current + entry)
    }
}

private fun encodeIncomeEntries(entries: List<IncomeEntry>): String {
    val array = JSONArray()
    entries.forEach { entry ->
        val obj = JSONObject()
        obj.put("id", entry.id)
        obj.put("source", entry.source)
        obj.put("tag", entry.tag)
        obj.put("amount", entry.amount)
        obj.put("dateEpochDay", entry.dateEpochDay)
        array.put(obj)
    }
    return array.toString()
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

// ── UI models ───────────────────────────────────────────────────

private data class IncomeSourceUi(
    val icon: ImageVector,
    val name: String,
    val tag: String,
    val amount: Double,
    val tagColor: Color
)

private data class IncomeCategoryUi(
    val label: String,
    val amount: Double,
    val color: Color,
    val fraction: Float
)

private val DEFAULT_TAGS = listOf("Salary", "Freelance", "Passive", "Business", "Crypto", "Other")

// ── Root screen ────────────────────────────────────────────────

@Composable
fun IncomeScreen(onAddClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val incomeEntries by remember {
        incomeEntriesFlow(context)
    }.collectAsState(initial = emptyList())

    val expenseEntries by remember {
        expenseEntriesFlow(context)
    }.collectAsState(initial = emptyList())

    val totalIncome by remember(incomeEntries) {
        derivedStateOf { incomeEntries.sumOf { it.amount } }
    }

    val totalExpenses by remember(expenseEntries) {
        derivedStateOf { expenseEntries.sumOf { it.amount } }
    }

    val netAmount by remember(totalIncome, totalExpenses) {
        derivedStateOf { totalIncome - totalExpenses }
    }

    val savingsRate by remember(totalIncome, netAmount) {
        derivedStateOf {
            if (totalIncome <= 0.0) 0.0 else (netAmount / totalIncome) * 100.0
        }
    }

    val categories by remember(incomeEntries, totalIncome) {
        derivedStateOf {
            val grouped = incomeEntries.groupBy { it.tag.ifBlank { "Other" } }
            val raw = grouped.map { (tag, list) ->
                IncomeCategoryUi(
                    label = tag,
                    amount = list.sumOf { it.amount },
                    color = colorForTag(tag),
                    fraction = 0f
                )
            }
            raw.sortedByDescending { it.amount }
                .take(4)
                .map { cat ->
                    cat.copy(fraction = if (totalIncome > 0) (cat.amount / totalIncome).toFloat() else 0f)
                }
        }
    }

    val sources by remember(incomeEntries) {
        derivedStateOf {
            incomeEntries
                .sortedByDescending { it.dateEpochDay }
                .map { entry ->
                    val date = LocalDate.ofEpochDay(entry.dateEpochDay)
                    val dateText = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    IncomeSourceUi(
                        icon = iconForTag(entry.tag),
                        name = entry.source,
                        tag = "${entry.tag} • $dateText",
                        amount = entry.amount,
                        tagColor = colorForTag(entry.tag)
                    )
                }
        }
    }

    var showAddSheet by remember { mutableStateOf(false) }

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
            AnimatedIncomeSection(visible = visible, delayMs = 0) {
                IncomeTopBar()
            }

            AnimatedIncomeSection(visible = visible, delayMs = 80) {
                HeroSummarySection(
                    visible = visible,
                    title = "MONTHLY INCOME",
                    value = totalIncome,
                    income = totalIncome,
                    expense = totalExpenses,
                    savingsRate = savingsRate
                )
            }

            Spacer(Modifier.height(32.dp))

            AnimatedIncomeSection(visible = visible, delayMs = 180) {
                IncomeBreakdownSection(visible = visible, categories = categories)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedIncomeSection(visible = visible, delayMs = 260) {
                IncomeSourcesSection(visible = visible, sources = sources)
            }

            Spacer(Modifier.height(96.dp))
        }

        // FAB springs in
        val fabScale by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "fab_scale"
        )

        FloatingIncomeAddButton(
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
        AddIncomeBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { source, tag, amount, date ->
                scope.launch {
                    saveIncomeEntry(
                        context,
                        IncomeEntry(
                            id = UUID.randomUUID().toString(),
                            source = source,
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

@Composable
private fun AnimatedIncomeSection(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "income_offset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "income_alpha"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY.dp.toPx()
            this.alpha = alpha
        }
    ) {
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
            text = "Income",
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
private fun HeroSummarySection(
    visible: Boolean,
    title: String,
    value: Double,
    income: Double,
    expense: Double,
    savingsRate: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        val animatedValue by animateFloatAsState(
            targetValue = if (visible) value.toFloat() else 0f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            label = "hero_count_up"
        )
        Text(
            text = formatCurrency(animatedValue.toDouble()),
            style = MaterialTheme.typography.displayLarge,
            color = OnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        InOutPill(visible = visible, income = income, expense = expense)

        Spacer(Modifier.height(16.dp))

        SavingsRateChip(rate = savingsRate)
    }
}

@Composable
private fun InOutPill(visible: Boolean, income: Double, expense: Double) {
    val animatedIncome by animateFloatAsState(
        targetValue = if (visible) income.toFloat() else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "pill_income"
    )
    val animatedExpense by animateFloatAsState(
        targetValue = if (visible) expense.toFloat() else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "pill_expense"
    )

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
                text = "+${formatCurrency(animatedIncome.toDouble(), 0)} In",
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
                text = "-${formatCurrency(animatedExpense.toDouble(), 0)} Out",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
    }
}

@Composable
private fun SavingsRateChip(rate: Double) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(PrimaryContainer.copy(alpha = 0.12f))
            .border(1.dp, PrimaryContainer.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.TrendingUp,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "${"%.1f".format(rate)}% savings rate",
            style = MaterialTheme.typography.labelLarge,
            color = Primary
        )
    }
}

// ── Breakdown section ──────────────────────────────────────────

@Composable
private fun IncomeBreakdownSection(
    visible: Boolean,
    categories: List<IncomeCategoryUi>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Breakdown",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        val tiles = if (categories.isEmpty()) {
            listOf(
                IncomeCategoryUi("Salary", 0.0, EssentialDot, 0f),
                IncomeCategoryUi("Freelance", 0.0, LuxuryDot, 0f),
                IncomeCategoryUi("Passive", 0.0, SecondaryFixedDim, 0f),
                IncomeCategoryUi("Business", 0.0, ExtraDot, 0f)
            )
        } else {
            categories.padToSize(4) { index ->
                IncomeCategoryUi(
                    label = "Other",
                    amount = 0.0,
                    color = fallbackColor(index),
                    fraction = 0f
                )
            }
        }

        val segments = tiles.mapIndexed { index, tile ->
            val fraction = categories.getOrNull(index)?.fraction ?: 0f
            tile.color to fraction
        }

        SegmentedBar(visible = visible, segments = segments)

        Spacer(Modifier.height(12.dp))

        // 2×2 grid with spring scale-in
        listOf(
            tiles.take(2),
            tiles.drop(2)
        ).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEachIndexed { colIndex, cat ->
                    val tileIndex = rowIndex * 2 + colIndex
                    val tileScale by animateFloatAsState(
                        targetValue = if (visible) 1f else 0.85f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "income_tile_scale_$tileIndex"
                    )
                    val tileAlpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(300, delayMillis = 300 + tileIndex * 80),
                        label = "income_tile_alpha_$tileIndex"
                    )

                    IncomeTile(
                        category = cat,
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
            if (rowIndex == 0) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SegmentedBar(
    visible: Boolean,
    segments: List<Pair<Color, Float>>
) {
    val safeSegments = if (segments.isEmpty()) {
        listOf(EssentialDot to 1f, LuxuryDot to 1f)
    } else {
        segments
    }
    val total = safeSegments.sumOf { it.second.toDouble() }.toFloat()
    val normalized = if (total <= 0f) {
        safeSegments.map { it.first to (1f / safeSegments.size) }
    } else {
        safeSegments.map { it.first to (it.second / total) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            normalized.forEachIndexed { index, (color, fraction) ->
                val animatedFraction by animateFloatAsState(
                    targetValue = if (visible) fraction else 0f,
                    animationSpec = tween(700, delayMillis = 200 + index * 70, easing = FastOutSlowInEasing),
                    label = "segment_$index"
                )
                if (animatedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedFraction)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeTile(
    category: IncomeCategoryUi,
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
            Box(Modifier.size(7.dp).background(category.color, CircleShape))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = formatCurrency(category.amount, 0),
            style = MaterialTheme.typography.titleLarge,
            color = OnSurface
        )
    }
}

// ── Sources list ───────────────────────────────────────────────

@Composable
private fun IncomeSourcesSection(
    visible: Boolean,
    sources: List<IncomeSourceUi>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Sources",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface
        )

        Spacer(Modifier.height(16.dp))

        if (sources.isEmpty()) {
            Text(
                text = "No income added yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            return
        }

        sources.forEachIndexed { index, source ->
            val rowOffset by animateFloatAsState(
                targetValue = if (visible) 0f else 60f,
                animationSpec = tween(400, delayMillis = 300 + index * 70, easing = FastOutSlowInEasing),
                label = "income_row_offset_$index"
            )
            val rowAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(300, delayMillis = 300 + index * 70),
                label = "income_row_alpha_$index"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = rowOffset.dp.toPx()
                    alpha = rowAlpha
                }
            ) {
                IncomeSourceRow(source = source)
            }

            if (index < sources.lastIndex) {
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
private fun IncomeSourceRow(source: IncomeSourceUi) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "income_press_$source"
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
                        imageVector = source.icon,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .border(1.5.dp, Background, CircleShape)
                        .background(source.tagColor, CircleShape)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }

            Column {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurface
                )
                Text(
                    text = source.tag,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        Text(
            text = "+${formatCurrency(source.amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = Secondary
        )
    }
}

// ── Add income sheet ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIncomeBottomSheet(
    onDismiss: () -> Unit,
    onSave: (source: String, tag: String, amount: Double, date: LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    var source by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf(DEFAULT_TAGS.first()) }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val sourceError = attemptedSave && source.isBlank()
    val tagError = attemptedSave && tag.isBlank()
    val amountError = attemptedSave && amountValue <= 0.0
    val amountFormatOk = amountText.isEmpty() || amountText.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))

    val isValid by remember(source, tag, amountText, amountFormatOk) {
        derivedStateOf { source.isNotBlank() && tag.isNotBlank() && amountValue > 0.0 && amountFormatOk }
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
                }) {
                    Text("OK")
                }
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
                text = "Add Income",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source") },
                singleLine = true,
                isError = sourceError,
                supportingText = {
                    if (sourceError) Text("Source is required.")
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
                    label = { Text("Tag / Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagMenuExpanded)
                    },
                    isError = tagError,
                    supportingText = {
                        if (tagError) Text("Pick a tag.")
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
                        onSave(source.trim(), tag.trim(), amountValue, selectedDate)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(saveScale)
            ) {
                Text("Save Income")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────

private fun iconForTag(tag: String): ImageVector {
    return when (tag.trim().lowercase()) {
        "salary" -> Icons.Outlined.Work
        "freelance" -> Icons.Outlined.Laptop
        "passive" -> Icons.Outlined.TrendingUp
        "business" -> Icons.Outlined.Storefront
        "crypto" -> Icons.Outlined.CurrencyBitcoin
        else -> Icons.Outlined.AccountBalanceWallet
    }
}

private fun colorForTag(tag: String): Color {
    return when (tag.trim().lowercase()) {
        "salary" -> EssentialDot
        "freelance" -> LuxuryDot
        "passive" -> SecondaryFixedDim
        "business" -> ExtraDot
        else -> Secondary
    }
}

private fun fallbackColor(index: Int): Color {
    return listOf(EssentialDot, LuxuryDot, SecondaryFixedDim, ExtraDot)[index % 4]
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
private fun FloatingIncomeAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "income_fab_press"
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
        val iconRotation by animateFloatAsState(
            targetValue = if (pressed) 90f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "income_icon_rotate"
        )

        LaunchedEffect(pressed) {
            if (pressed) {
                delay(300)
                pressed = false
            }
        }

        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add income",
            tint = Secondary,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF13131A)
@Composable
fun IncomeScreenPreview() {
    com.example.aurafarm2.core.theme.AppTheme {
        IncomeScreen()
    }
}
