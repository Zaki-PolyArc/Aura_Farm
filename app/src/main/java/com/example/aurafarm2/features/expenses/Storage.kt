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

private fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun currencySymbol(currency: String): String =
    currency.substringAfter("(", "$").substringBefore(")")

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
