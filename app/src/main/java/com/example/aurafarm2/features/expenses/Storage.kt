package com.example.aurafarm2.features.expenses

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

// ── Shared DataStore instances ────────────────────────────────

val Context.expenseDataStore by preferencesDataStore(name = "expense_store")
val EXPENSE_ENTRIES_KEY = stringPreferencesKey("expense_entries_json")

val Context.incomeDataStore by preferencesDataStore(name = "income_store")
val INCOME_ENTRIES_KEY = stringPreferencesKey("income_entries_json")

val Context.settingsDataStore by preferencesDataStore(name = "app_settings")
val FULL_NAME_KEY = stringPreferencesKey("full_name")
val EMAIL_KEY = stringPreferencesKey("email")
val CURRENCY_KEY = stringPreferencesKey("currency")
val APPEARANCE_KEY = stringPreferencesKey("appearance")
val PASSWORD_SALT_KEY = stringPreferencesKey("password_salt")
val PASSWORD_HASH_KEY = stringPreferencesKey("password_hash")
val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
val REMINDERS_KEY = stringPreferencesKey("reminders_json")
val BUDGETS_KEY = stringPreferencesKey("budgets_json")
val RECURRING_ENTRIES_KEY = stringPreferencesKey("recurring_entries_json")

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

data class AppSettings(
    val fullName: String = "",
    val email: String = "",
    val currency: String = "USD ($)",
    val appearance: String = "Dark",
    val hasPassword: Boolean = false,
    val biometricEnabled: Boolean = false
)

data class Reminder(
    val id: String,
    val title: String,
    val type: String,
    val hour: Int,
    val minute: Int,
    val repeat: String,
    val note: String,
    val enabled: Boolean
)

data class Budget(
    val id: String,
    val category: String,
    val limit: Double
)

data class RecurringEntry(
    val id: String,
    val kind: String,
    val name: String,
    val category: String,
    val amount: Double,
    val repeat: String,
    val nextDueEpochDay: Long,
    val enabled: Boolean
)

// ── Expense Operations ────────────────────────────────────────

fun appSettingsFlow(context: Context): Flow<AppSettings> =
    context.settingsDataStore.data.map { prefs ->
        AppSettings(
            fullName = prefs[FULL_NAME_KEY] ?: "",
            email = prefs[EMAIL_KEY] ?: "",
            currency = prefs[CURRENCY_KEY] ?: "USD ($)",
            appearance = prefs[APPEARANCE_KEY] ?: "Dark",
            hasPassword = prefs[PASSWORD_HASH_KEY].isNullOrBlank().not(),
            biometricEnabled = prefs[BIOMETRIC_ENABLED_KEY] ?: false
        )
    }

suspend fun savePersonalDetails(context: Context, fullName: String, email: String) {
    context.settingsDataStore.edit { prefs ->
        prefs[FULL_NAME_KEY] = fullName.trim()
        prefs[EMAIL_KEY] = email.trim()
    }
}

suspend fun saveCurrency(context: Context, currency: String) {
    context.settingsDataStore.edit { prefs ->
        prefs[CURRENCY_KEY] = currency
    }
}

suspend fun saveAppearance(context: Context, appearance: String) {
    context.settingsDataStore.edit { prefs ->
        prefs[APPEARANCE_KEY] = appearance
    }
}

suspend fun savePassword(context: Context, password: String) {
    val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
    val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    context.settingsDataStore.edit { prefs ->
        prefs[PASSWORD_SALT_KEY] = salt
        prefs[PASSWORD_HASH_KEY] = hashPassword(password, salt)
    }
}

suspend fun saveBiometricEnabled(context: Context, enabled: Boolean) {
    context.settingsDataStore.edit { prefs ->
        prefs[BIOMETRIC_ENABLED_KEY] = enabled
    }
}

fun remindersFlow(context: Context): Flow<List<Reminder>> =
    context.settingsDataStore.data.map { prefs ->
        decodeReminders(prefs[REMINDERS_KEY] ?: "[]")
    }

suspend fun saveReminder(context: Context, reminder: Reminder) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeReminders(prefs[REMINDERS_KEY] ?: "[]")
        val updated = current.filterNot { it.id == reminder.id } + reminder
        prefs[REMINDERS_KEY] = encodeReminders(updated)
    }
}

suspend fun deleteReminder(context: Context, reminderId: String) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeReminders(prefs[REMINDERS_KEY] ?: "[]")
        prefs[REMINDERS_KEY] = encodeReminders(current.filterNot { it.id == reminderId })
    }
}

fun budgetsFlow(context: Context): Flow<List<Budget>> =
    context.settingsDataStore.data.map { prefs ->
        decodeBudgets(prefs[BUDGETS_KEY] ?: "[]")
    }

suspend fun saveBudget(context: Context, budget: Budget) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeBudgets(prefs[BUDGETS_KEY] ?: "[]")
        val updated = current.filterNot { it.id == budget.id || it.category == budget.category } + budget
        prefs[BUDGETS_KEY] = encodeBudgets(updated)
    }
}

suspend fun deleteBudget(context: Context, budgetId: String) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeBudgets(prefs[BUDGETS_KEY] ?: "[]")
        prefs[BUDGETS_KEY] = encodeBudgets(current.filterNot { it.id == budgetId })
    }
}

fun recurringEntriesFlow(context: Context): Flow<List<RecurringEntry>> =
    context.settingsDataStore.data.map { prefs ->
        decodeRecurringEntries(prefs[RECURRING_ENTRIES_KEY] ?: "[]")
    }

suspend fun saveRecurringEntry(context: Context, entry: RecurringEntry) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeRecurringEntries(prefs[RECURRING_ENTRIES_KEY] ?: "[]")
        val updated = current.filterNot { it.id == entry.id } + entry
        prefs[RECURRING_ENTRIES_KEY] = encodeRecurringEntries(updated)
    }
}

suspend fun deleteRecurringEntry(context: Context, entryId: String) {
    context.settingsDataStore.edit { prefs ->
        val current = decodeRecurringEntries(prefs[RECURRING_ENTRIES_KEY] ?: "[]")
        prefs[RECURRING_ENTRIES_KEY] = encodeRecurringEntries(current.filterNot { it.id == entryId })
    }
}

private fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun currencySymbol(currency: String): String =
    currency.substringAfter("(", "$").substringBefore(")")

fun encodeReminders(reminders: List<Reminder>): String {
    val array = JSONArray()
    reminders.forEach { reminder ->
        val obj = JSONObject()
        obj.put("id", reminder.id)
        obj.put("title", reminder.title)
        obj.put("type", reminder.type)
        obj.put("hour", reminder.hour)
        obj.put("minute", reminder.minute)
        obj.put("repeat", reminder.repeat)
        obj.put("note", reminder.note)
        obj.put("enabled", reminder.enabled)
        array.put(obj)
    }
    return array.toString()
}

fun decodeReminders(raw: String): List<Reminder> =
    runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    Reminder(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        type = obj.optString("type", "Log expense"),
                        hour = obj.optInt("hour", 9),
                        minute = obj.optInt("minute", 0),
                        repeat = obj.optString("repeat", "Once"),
                        note = obj.optString("note"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

fun encodeBudgets(budgets: List<Budget>): String {
    val array = JSONArray()
    budgets.forEach { budget ->
        val obj = JSONObject()
        obj.put("id", budget.id)
        obj.put("category", budget.category)
        obj.put("limit", budget.limit)
        array.put(obj)
    }
    return array.toString()
}

fun decodeBudgets(raw: String): List<Budget> =
    runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    Budget(
                        id = obj.optString("id"),
                        category = obj.optString("category"),
                        limit = obj.optDouble("limit")
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

fun encodeRecurringEntries(entries: List<RecurringEntry>): String {
    val array = JSONArray()
    entries.forEach { entry ->
        val obj = JSONObject()
        obj.put("id", entry.id)
        obj.put("kind", entry.kind)
        obj.put("name", entry.name)
        obj.put("category", entry.category)
        obj.put("amount", entry.amount)
        obj.put("repeat", entry.repeat)
        obj.put("nextDueEpochDay", entry.nextDueEpochDay)
        obj.put("enabled", entry.enabled)
        array.put(obj)
    }
    return array.toString()
}

fun decodeRecurringEntries(raw: String): List<RecurringEntry> =
    runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    RecurringEntry(
                        id = obj.optString("id"),
                        kind = obj.optString("kind", "Expense"),
                        name = obj.optString("name"),
                        category = obj.optString("category"),
                        amount = obj.optDouble("amount"),
                        repeat = obj.optString("repeat", "Monthly"),
                        nextDueEpochDay = obj.optLong("nextDueEpochDay"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        }
    }.getOrElse { emptyList() }

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

suspend fun updateExpenseEntry(context: Context, entry: ExpenseEntry) {
    context.expenseDataStore.edit { prefs ->
        val current = decodeExpenseEntries(prefs[EXPENSE_ENTRIES_KEY] ?: "[]")
        val updated = current.map { if (it.id == entry.id) entry else it }
        prefs[EXPENSE_ENTRIES_KEY] = encodeExpenseEntries(updated)
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

suspend fun updateIncomeEntry(context: Context, entry: IncomeEntry) {
    context.incomeDataStore.edit { prefs ->
        val current = decodeIncomeEntries(prefs[INCOME_ENTRIES_KEY] ?: "[]")
        val updated = current.map { if (it.id == entry.id) entry else it }
        prefs[INCOME_ENTRIES_KEY] = encodeIncomeEntries(updated)
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
