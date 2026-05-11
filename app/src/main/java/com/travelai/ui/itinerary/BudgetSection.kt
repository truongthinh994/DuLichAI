package com.travelai.ui.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.formatBudgetAmount
import com.travelai.data.model.totalAmountVnd

@Composable
fun BudgetSection(
    budgetItems: List<BudgetItem>,
    formState: BudgetFormState,
    errorMessage: String?,
    isSaving: Boolean,
    onCategoryChange: (BudgetCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onEdit: (BudgetItem) -> Unit,
    onDelete: (BudgetItem) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BudgetSummary(budgetItems = budgetItems)
        BudgetForm(
            formState = formState,
            errorMessage = errorMessage,
            isSaving = isSaving,
            onCategoryChange = onCategoryChange,
            onTitleChange = onTitleChange,
            onAmountChange = onAmountChange,
            onNoteChange = onNoteChange,
            onSave = onSave,
            onCancelEdit = onCancelEdit
        )
        BudgetItemList(
            budgetItems = budgetItems,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun BudgetSummary(
    budgetItems: List<BudgetItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Ngân sách",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Tổng dự kiến: ${formatBudgetAmount(budgetItems.totalAmountVnd())}",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun BudgetForm(
    formState: BudgetFormState,
    errorMessage: String?,
    isSaving: Boolean,
    onCategoryChange: (BudgetCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { BudgetCategory.entries.toList() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (formState.editingItemId == null) "Thêm khoản chi" else "Sửa khoản chi",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = categories,
                    key = { category -> category.name }
                ) { category ->
                    CategoryButton(
                        category = category,
                        selected = formState.category == category,
                        onClick = { onCategoryChange(category) }
                    )
                }
            }
            OutlinedTextField(
                value = formState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên khoản chi") },
                singleLine = true
            )
            OutlinedTextField(
                value = formState.amountText,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền dự kiến") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = formState.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú") },
                minLines = 2,
                maxLines = 3
            )
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (formState.editingItemId != null) {
                    TextButton(
                        onClick = onCancelEdit,
                        enabled = !isSaving
                    ) {
                        Text("Hủy")
                    }
                }
                Button(
                    onClick = onSave,
                    enabled = !isSaving
                ) {
                    Text(if (formState.editingItemId == null) "Thêm" else "Lưu")
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: BudgetCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(category.label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(category.label)
        }
    }
}

@Composable
private fun BudgetItemList(
    budgetItems: List<BudgetItem>,
    onEdit: (BudgetItem) -> Unit,
    onDelete: (BudgetItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (budgetItems.isEmpty()) {
            Text(
                text = "Chưa có khoản chi nào.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            return@Column
        }

        budgetItems.forEach { item ->
            BudgetItemCard(
                item = item,
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun BudgetItemCard(
    item: BudgetItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = formatBudgetAmount(item.amountVnd),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Sửa")
                }
                TextButton(onClick = onDelete) {
                    Text("Xóa")
                }
            }
        }
    }
}
