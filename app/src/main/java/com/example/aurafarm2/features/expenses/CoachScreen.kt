package com.example.aurafarm2.features.expenses

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.example.aurafarm2.core.theme.*

@Composable
fun CoachScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val budgets by budgetsFlow(context).collectAsState(initial = emptyList())
    val recurringEntries by recurringEntriesFlow(context).collectAsState(initial = emptyList())
    val expenses by expenseEntriesFlow(context).collectAsState(initial = emptyList())
    
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }

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
        BudgetManagerDialog(
            budgets = budgets,
            onDismiss = { showBudgetDialog = false },
            onSave = { budget -> coroutineScope.launch { saveBudget(context, budget) } },
            onDelete = { budget -> coroutineScope.launch { deleteBudget(context, budget.id) } }
        )
    }

    if (showRecurringDialog) {
        RecurringManagerDialog(
            entries = recurringEntries,
            onDismiss = { showRecurringDialog = false },
            onSave = { entry -> coroutineScope.launch { saveRecurringEntry(context, entry) } },
            onDelete = { entry -> coroutineScope.launch { deleteRecurringEntry(context, entry.id) } }
        )
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
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
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
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, style = MaterialTheme.typography.bodyLarge, color = Primary)
            Text(
                text = "${String.format("%.2f", spent)} / ${String.format("%.2f", limit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOver) Error else Outline
            )
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(100.dp)),
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
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(entry.name, style = MaterialTheme.typography.bodyLarge, color = Primary)
            Text(statusText, style = MaterialTheme.typography.bodyMedium, color = statusColor)
        }
        Text(
            text = "${if(entry.kind == "Expense") "-" else "+"}${entry.amount}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (entry.kind == "Expense") Error else Primary
        )
    }
}
