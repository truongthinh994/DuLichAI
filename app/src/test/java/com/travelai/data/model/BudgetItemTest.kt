package com.travelai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetItemTest {
    @Test
    fun parseBudgetAmount_acceptsFormattedVietnameseCurrency() {
        assertEquals(1_250_000L, parseBudgetAmount("1.250.000 đ"))
        assertEquals(350_000L, parseBudgetAmount("350000"))
    }

    @Test
    fun parseBudgetAmount_rejectsBlankAndZero() {
        assertNull(parseBudgetAmount(""))
        assertNull(parseBudgetAmount("0"))
    }

    @Test
    fun totalAmountVnd_sumsBudgetItems() {
        val items = listOf(
            budgetItem(amountVnd = 100_000L),
            budgetItem(id = 2L, amountVnd = 250_000L)
        )

        assertEquals(350_000L, items.totalAmountVnd())
    }

    @Test
    fun formatBudgetAmount_appendsVietnameseDongSuffix() {
        assertTrue(formatBudgetAmount(1_000_000L).endsWith("đ"))
    }

    private fun budgetItem(
        id: Long = 1L,
        amountVnd: Long
    ): BudgetItem = BudgetItem(
        id = id,
        sessionId = 10L,
        category = BudgetCategory.FOOD,
        title = "Ăn uống",
        amountVnd = amountVnd,
        note = "",
        createdAt = 1L,
        updatedAt = 1L
    )
}
