package com.travelai.ui.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.completedChecklistCount
import com.travelai.ui.theme.TravelAITheme

@Composable
fun ChecklistSection(
    checklistItems: List<ChecklistItem>,
    draftTitle: String,
    errorMessage: String?,
    isSaving: Boolean,
    onDraftTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggle: (ChecklistItem, Boolean) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChecklistSummary(checklistItems = checklistItems)
        ChecklistForm(
            draftTitle = draftTitle,
            errorMessage = errorMessage,
            isSaving = isSaving,
            onDraftTitleChange = onDraftTitleChange,
            onAdd = onAdd
        )
        ChecklistItemList(
            checklistItems = checklistItems,
            isSaving = isSaving,
            onToggle = onToggle,
            onDelete = onDelete
        )
    }
}

@Composable
private fun ChecklistSummary(
    checklistItems: List<ChecklistItem>,
    modifier: Modifier = Modifier
) {
    val completedCount = remember(checklistItems) { checklistItems.completedChecklistCount() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Checklist chuẩn bị",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Đã xong $completedCount/${checklistItems.size} việc",
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ChecklistForm(
    draftTitle: String,
    errorMessage: String?,
    isSaving: Boolean,
    onDraftTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = draftTitle,
                onValueChange = onDraftTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Việc cần chuẩn bị") },
                placeholder = { Text("Ví dụ: Mang CCCD, sạc dự phòng") },
                singleLine = true
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
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAdd,
                    enabled = !isSaving
                ) {
                    Text("Thêm")
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemList(
    checklistItems: List<ChecklistItem>,
    isSaving: Boolean,
    onToggle: (ChecklistItem, Boolean) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (checklistItems.isEmpty()) {
            Text(
                text = "Chưa có việc chuẩn bị nào.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            return@Column
        }

        checklistItems.forEach { item ->
            ChecklistItemCard(
                item = item,
                isSaving = isSaving,
                onToggle = { isChecked -> onToggle(item, isChecked) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    isSaving: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggle,
                enabled = !isSaving
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isChecked) "Đã xong" else "Cần làm",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(
                onClick = onDelete,
                enabled = !isSaving
            ) {
                Text("Xóa")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChecklistSectionPreview() {
    TravelAITheme {
        ChecklistSection(
            checklistItems = remember {
                listOf(
                    ChecklistItem(
                        id = 1L,
                        sessionId = 10L,
                        title = "Mang CCCD và giấy tờ đặt phòng",
                        isChecked = true,
                        createdAt = 1L,
                        updatedAt = 2L
                    ),
                    ChecklistItem(
                        id = 2L,
                        sessionId = 10L,
                        title = "Chuẩn bị sạc dự phòng",
                        isChecked = false,
                        createdAt = 3L,
                        updatedAt = 3L
                    )
                )
            },
            draftTitle = "",
            errorMessage = null,
            isSaving = false,
            onDraftTitleChange = {},
            onAdd = {},
            onToggle = { _, _ -> },
            onDelete = {}
        )
    }
}
