package com.example.aurafarm2.features.expenses

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// ── Shared DataStore instances ────────────────────────────────

val Context.expenseDataStore by preferencesDataStore(name = "expense_store")
val EXPENSE_ENTRIES_KEY = stringPreferencesKey("expense_entries_json")

val Context.incomeDataStore by preferencesDataStore(name = "income_store")
val INCOME_ENTRIES_KEY = stringPreferencesKey("income_entries_json")

// ── Models ─────────────────────────────────────────────────────

data class ExpenseEntry(
    val id: String,
    val name: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

data class IncomeEntry(
    val id: String,
    val source: String,
    val tag: String,
    val amount: Double,
    val dateEpochDay: Long
)

// ── Expense Operations ────────────────────────────────────────

fun expenseEntriesFlow(context: Context): Flow<List<ExpenseEntry>> =
    context.expenseDataStore.data.map { prefs ->
        val raw = prefs[EXPENSE_ENTRIES_KEY] ?: "[]"
        decodeExpenseEntries(raw)
    }

suspend fun saveExpenseEntry(context: Context, entry: ExpenseEntry) {
    context.expenseDataStore.edit { prefs ->
        val current = decodeExpenseEntries(prefs[EXPENSE_ENTRIES_KEY] ?: "[]")
        prefs[EXPENSE_ENTRIES_KEY] = encodeExpenseEntries(current + entry)
    }
}

suspend fun deleteExpenseEntry(context: Context, entryId: String) {
    context.expenseDataStore.edit { prefs ->
        val current = decodeExpenseEntries(prefs[EXPENSE_ENTRIES_KEY] ?: "[]")
        prefs[EXPENSE_ENTRIES_KEY] = encodeExpenseEntries(current.filterNot { it.id == entryId })
    }
}

fun encodeExpenseEntries(entries: List<ExpenseEntry>): String {
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

fun decodeExpenseEntries(raw: String): List<ExpenseEntry> {
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

// ── Income Operations ─────────────────────────────────────────

fun incomeEntriesFlow(context: Context): Flow<List<IncomeEntry>> =
    context.incomeDataStore.data.map { prefs ->
        val raw = prefs[INCOME_ENTRIES_KEY] ?: "[]"
        decodeIncomeEntries(raw)
    }

suspend fun saveIncomeEntry(context: Context, entry: IncomeEntry) {
    context.incomeDataStore.edit { prefs ->
        val current = decodeIncomeEntries(prefs[INCOME_ENTRIES_KEY] ?: "[]")
        prefs[INCOME_ENTRIES_KEY] = encodeIncomeEntries(current + entry)
    }
}

suspend fun deleteIncomeEntry(context: Context, entryId: String) {
    context.incomeDataStore.edit { prefs ->
        val current = decodeIncomeEntries(prefs[INCOME_ENTRIES_KEY] ?: "[]")
        prefs[INCOME_ENTRIES_KEY] = encodeIncomeEntries(current.filterNot { it.id == entryId })
    }
}

fun encodeIncomeEntries(entries: List<IncomeEntry>): String {
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

fun decodeIncomeEntries(raw: String): List<IncomeEntry> {
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
