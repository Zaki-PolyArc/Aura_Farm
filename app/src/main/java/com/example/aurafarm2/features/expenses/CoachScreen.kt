package com.example.aurafarm2.features.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate

import java.util.UUID
import com.example.aurafarm2.core.theme.*
import com.example.aurafarm2.features.expenses.components.MoneyTypeNumpadModal

@Composable
fun CoachScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val budgets by budgetsFlow(context).collectAsState(initial = emptyList())
    val recurringEntries by recurringEntriesFlow(context).collectAsState(initial = emptyList())
    val expenses by expenseEntriesFlow(context).collectAsState(initial = emptyList())
    
    val settings by appSettingsFlow(context).collectAsState(initial = AppSettings())
    val symbol = currencySymbol(settings.currency)

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var budgetCategoryToSet by remember { mutableStateOf<String?>(null) }
    var recurringEntryDraft by remember { mutableStateOf<RecurringEntry?>(null) }

    val currentMonthExpenses = remember(expenses) {
        val startOfMonth = LocalDate.now().withDayOfMonth(1).toEpochDay()
        expenses.filter { it.dateEpochDay >= startOfMonth }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Finance Coach",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                SectionHeader(
                    title = "Budgets",
                    actionText = "Manage",
                    onAction = { showBudgetDialog = true }
                )
                Spacer(Modifier.height(8.dp))
                if (budgets.isEmpty()) {
                    EmptyStateCard("No budgets set. Tap Manage to add one.")
                } else {
                    budgets.forEach { budget ->
                        val spent = currentMonthExpenses.filter { it.tag.equals(budget.category, true) }.sumOf { it.amount }
                        val progress = if (budget.limit > 0) (spent / budget.limit).coerceIn(0.0, 1.0).toFloat() else 0f
                        val isOver = spent > budget.limit

                        BudgetCard(
                            category = budget.category,
                            spent = spent,
                            limit = budget.limit,
                            progress = progress,
                            isOver = isOver
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Recurring Payments",
                    actionText = "Manage",
                    onAction = { showRecurringDialog = true }
                )
                Spacer(Modifier.height(8.dp))
                if (recurringEntries.isEmpty()) {
                    EmptyStateCard("No recurring payments set.")
                } else {
                    recurringEntries.filter { it.enabled }.sortedBy { it.nextDueEpochDay }.forEach { entry ->
                        RecurringCard(entry)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        BudgetCategorySelectionSheet(
            onDismiss = { showBudgetDialog = false },
            onSelect = { category ->
                budgetCategoryToSet = category
                showBudgetDialog = false
            }
        )
    }

    if (budgetCategoryToSet != null) {
        val existingLimit = budgets.find { it.category == budgetCategoryToSet }?.limit ?: 0.0
        MoneyTypeNumpadModal(
            title = "Set Limit: $budgetCategoryToSet",
            currencySymbol = symbol,
            initialValue = existingLimit,
            onDismiss = { budgetCategoryToSet = null },
            onConfirm = { limit ->
                val categoryToSave = budgetCategoryToSet
                if (categoryToSave != null) {
                    coroutineScope.launch {
                        val budget = budgets.find { it.category == categoryToSave }
                            ?.copy(limit = limit)
                            ?: Budget(id = UUID.randomUUID().toString(), category = categoryToSave, limit = limit)
                        saveBudget(context, budget)
                    }
                }
                budgetCategoryToSet = null
            }
        )
    }

    if (showRecurringDialog) {
        RecurringDetailsSheet(
            onDismiss = { showRecurringDialog = false },
            onNext = { draft ->
                recurringEntryDraft = draft
                showRecurringDialog = false
            }
        )
    }

    if (recurringEntryDraft != null) {
        MoneyTypeNumpadModal(
            title = "Amount: ${recurringEntryDraft!!.name}",
            currencySymbol = symbol,
            initialValue = recurringEntryDraft!!.amount,
            onDismiss = { recurringEntryDraft = null },
            onConfirm = { amount ->
                val draftToSave = recurringEntryDraft
                if (draftToSave != null) {
                    coroutineScope.launch {
                        saveRecurringEntry(context, draftToSave.copy(amount = amount))
                    }
                }
                recurringEntryDraft = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCategorySelectionSheet(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = listOf("Food", "Transport", "Utilities", "Entertainment", "Shopping", "Health", "Other")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 24.dp)) {
            Text("SELECT CATEGORY", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp), color = Outline)
            Spacer(Modifier.height(24.dp))
            categories.forEach { cat ->
                Text(
                    text = cat,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(cat) }
                        .padding(vertical = 16.dp)
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringDetailsSheet(onDismiss: () -> Unit, onNext: (RecurringEntry) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("Expense") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 24.dp)) {
            Text("NEW RECURRING", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp), color = Outline)
            Spacer(Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (e.g. Netflix)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Outline,
                    cursorColor = Primary
                )
            )
            
            Spacer(Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = kind == "Expense",
                    onClick = { kind = "Expense" },
                    label = { Text("Expense") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                FilterChip(
                    selected = kind == "Income",
                    onClick = { kind = "Income" },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onNext(
                        RecurringEntry(
                            id = UUID.randomUUID().toString(),
                            kind = kind,
                            name = name.ifBlank { "Unnamed" },
                            category = "Other",
                            amount = 0.0,
                            repeat = "Monthly",
                            nextDueEpochDay = LocalDate.now().toEpochDay(),
                            enabled = true
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("NEXT: SET AMOUNT", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
        TextButton(onClick = onAction) {
            Text(actionText, style = MaterialTheme.typography.labelLarge, color = Primary)
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BudgetCard(category: String, spent: Double, limit: Double, progress: Float, isOver: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Text(category.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = Outline)
        Spacer(Modifier.height(8.dp))
        Text(
            text = String.format("%.0f", limit),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light),
            color = Primary
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPENT: ${String.format("%.2f", spent)}",
                style = MaterialTheme.typography.labelMedium,
                color = if (isOver) Error else OnSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (isOver) Error else OnSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(100.dp)),
            color = if (isOver) Error else Primary,
            trackColor = SurfaceContainerHigh
        )
    }
}

@Composable
fun RecurringCard(entry: RecurringEntry) {
    val daysUntil = entry.nextDueEpochDay - LocalDate.now().toEpochDay()
    val statusText = when {
        daysUntil < 0 -> "Overdue by ${-daysUntil} days"
        daysUntil == 0L -> "Due Today"
        daysUntil == 1L -> "Due Tomorrow"
        else -> "Due in $daysUntil days"
    }
    val statusColor = if (daysUntil <= 3) Error else Outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(entry.name.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp), color = Outline)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${entry.amount}",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
                color = if (entry.kind == "Expense") Error else Primary
            )
            Spacer(Modifier.height(8.dp))
            Text(statusText, style = MaterialTheme.typography.labelMedium, color = statusColor)
        }
    }
}
