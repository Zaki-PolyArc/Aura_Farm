package com.example.aurafarm2.features.expenses

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionAutomationTest {
    @Test
    fun extractsCommonInrAmounts() {
        assertEquals(1250.0, TransactionExtractor.extract("₹1,250 paid to Swiggy", "app", 1L)?.amount ?: 0.0, 0.001)
        assertEquals(1250.0, TransactionExtractor.extract("INR 1250 debited at Uber", "app", 1L)?.amount ?: 0.0, 0.001)
        assertEquals(1250.0, TransactionExtractor.extract("Rs.1250 paid to Zomato", "app", 1L)?.amount ?: 0.0, 0.001)
        assertEquals(1250.50, TransactionExtractor.extract("Rs 1,250.50 paid to Amazon", "app", 1L)?.amount ?: 0.0, 0.001)
    }

    @Test
    fun filtersOtpAndPromos() {
        assertNull(TransactionExtractor.extract("OTP 123456 for Rs 1250 transaction", "app", 1L))
        assertNull(TransactionExtractor.extract("Get 20% cashback today", "app", 1L))
    }

    @Test
    fun detectsIncomeAndExpense() {
        assertEquals("Expense", TransactionExtractor.extract("₹450 paid to Swiggy using UPI", "phonepe", 1L)?.type)
        assertEquals("Income", TransactionExtractor.extract("Salary credited ₹45000", "bank", 1L)?.type)
    }

    @Test
    fun categorizationPriorityUsesCorrectionThenRulesThenFallback() {
        val corrections = listOf(CategoryCorrection("XYZ Fuel Station", "Transport"))
        assertEquals("Transport", TransactionCategorizer.categorize("XYZ Fuel Station", corrections))
        assertEquals("Food", TransactionCategorizer.categorize("Swiggy", emptyList()))
        assertNotNull(BasicMerchantClassifier.predict("pizza outlet", emptyList()))
    }
}
