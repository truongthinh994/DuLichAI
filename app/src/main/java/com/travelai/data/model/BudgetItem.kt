package com.travelai.data.model

import java.text.NumberFormat
import java.util.Locale

data class BudgetItem(
    val id: Long,
    val sessionId: Long,
    val category: BudgetCategory,
    val title: String,
    val amountVnd: Long,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long
)

enum class BudgetCategory(val label: String) {
    FOOD("Ăn uống"),
    TRANSPORT("Di chuyển"),
    TICKETS("Vé tham quan"),
    HOTEL("Khách sạn"),
    INCIDENTAL("Phát sinh")
}

fun List<BudgetItem>.totalAmountVnd(): Long = sumOf { it.amountVnd }

fun parseBudgetAmount(value: String): Long? {
    val digits = value.filter { it.isDigit() }
    if (digits.isBlank()) return null
    return digits.toLongOrNull()?.takeIf { it > 0L }
}

fun formatBudgetAmount(amountVnd: Long): String =
    "${BUDGET_AMOUNT_FORMATTER.format(amountVnd)} đ"

private val BUDGET_AMOUNT_FORMATTER: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
