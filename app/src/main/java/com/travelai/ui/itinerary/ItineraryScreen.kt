package com.travelai.ui.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriod
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.ui.theme.TravelAITheme

@Composable
fun ItineraryScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ItineraryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onOpenChat = onOpenChat,
        onBudgetCategoryChange = viewModel::onBudgetCategoryChange,
        onBudgetTitleChange = viewModel::onBudgetTitleChange,
        onBudgetAmountChange = viewModel::onBudgetAmountChange,
        onBudgetNoteChange = viewModel::onBudgetNoteChange,
        onSaveBudgetItem = viewModel::saveBudgetItem,
        onEditBudgetItem = viewModel::editBudgetItem,
        onDeleteBudgetItem = viewModel::deleteBudgetItem,
        onCancelBudgetEdit = viewModel::cancelBudgetEdit,
        onChecklistTitleChange = viewModel::onChecklistTitleChange,
        onAddChecklistItem = viewModel::addChecklistItem,
        onToggleChecklistItem = viewModel::toggleChecklistItem,
        onDeleteChecklistItem = viewModel::deleteChecklistItem
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryScreenContent(
    uiState: ItineraryUiState,
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onBudgetCategoryChange: (BudgetCategory) -> Unit = {},
    onBudgetTitleChange: (String) -> Unit = {},
    onBudgetAmountChange: (String) -> Unit = {},
    onBudgetNoteChange: (String) -> Unit = {},
    onSaveBudgetItem: () -> Unit = {},
    onEditBudgetItem: (BudgetItem) -> Unit = {},
    onDeleteBudgetItem: (BudgetItem) -> Unit = {},
    onCancelBudgetEdit: () -> Unit = {},
    onChecklistTitleChange: (String) -> Unit = {},
    onAddChecklistItem: () -> Unit = {},
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit = { _, _ -> },
    onDeleteChecklistItem: (ChecklistItem) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title.ifBlank { "Lịch trình" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Quay lại")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { uiState.sessionId?.let(onOpenChat) },
                        enabled = uiState.sessionId != null
                    ) {
                        Text("Chat")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                LoadingItineraryState()
            }

            uiState.errorMessage != null -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            uiState.days.isNotEmpty() -> ParsedItineraryContent(
                days = uiState.days,
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.rawText.isNotBlank() -> RawItineraryContent(
                rawText = uiState.rawText,
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> EmptyItineraryContent(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            }
        }
    }

@Composable
private fun ParsedItineraryContent(
    days: List<TripPlanDay>,
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(days.size) {
        if (selectedDayIndex > days.lastIndex) {
            selectedDayIndex = 0
        }
    }

    val selectedIndex = selectedDayIndex.coerceIn(0, days.lastIndex)
    val selectedDay = days[selectedIndex]

    Column(modifier = modifier) {
        ScrollableTabRow(selectedTabIndex = selectedIndex) {
            days.forEachIndexed { index, day ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedDayIndex = index },
                    text = { Text("Ngày ${day.dayNumber}") }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "day-header-${selectedDay.dayNumber}") {
                DayHeader(day = selectedDay)
            }
            items(
                items = selectedDay.periods,
                key = { period -> period.period.name }
            ) { period ->
                PeriodCard(period = period)
            }
            item(key = "trip-planning-sections") {
                TripPlanningSections(
                    uiState = uiState,
                    onBudgetCategoryChange = onBudgetCategoryChange,
                    onBudgetTitleChange = onBudgetTitleChange,
                    onBudgetAmountChange = onBudgetAmountChange,
                    onBudgetNoteChange = onBudgetNoteChange,
                    onSaveBudgetItem = onSaveBudgetItem,
                    onEditBudgetItem = onEditBudgetItem,
                    onDeleteBudgetItem = onDeleteBudgetItem,
                    onCancelBudgetEdit = onCancelBudgetEdit,
                    onChecklistTitleChange = onChecklistTitleChange,
                    onAddChecklistItem = onAddChecklistItem,
                    onToggleChecklistItem = onToggleChecklistItem,
                    onDeleteChecklistItem = onDeleteChecklistItem
                )
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: TripPlanDay,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Ngày ${day.dayNumber}",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
            if (day.title.isNotBlank()) {
                Text(
                    text = day.title,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PeriodCard(
    period: TripPlanPeriod,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = period.period.label,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = period.content,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RawItineraryContent(
    rawText: String,
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bản lịch trình gốc",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = rawText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item(key = "trip-planning-sections") {
            TripPlanningSections(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem
            )
        }
    }
}

@Composable
private fun EmptyItineraryContent(
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "empty-itinerary") {
            EmptyItineraryState(modifier = Modifier.fillMaxWidth())
        }
        item(key = "trip-planning-sections") {
            TripPlanningSections(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem
            )
        }
    }
}

@Composable
private fun TripPlanningSections(
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BudgetSection(
            budgetItems = uiState.budgetItems,
            formState = uiState.budgetForm,
            errorMessage = uiState.budgetErrorMessage,
            isSaving = uiState.isBudgetSaving,
            onCategoryChange = onBudgetCategoryChange,
            onTitleChange = onBudgetTitleChange,
            onAmountChange = onBudgetAmountChange,
            onNoteChange = onBudgetNoteChange,
            onSave = onSaveBudgetItem,
            onEdit = onEditBudgetItem,
            onDelete = onDeleteBudgetItem,
            onCancelEdit = onCancelBudgetEdit
        )
        ChecklistSection(
            checklistItems = uiState.checklistItems,
            draftTitle = uiState.checklistDraftTitle,
            errorMessage = uiState.checklistErrorMessage,
            isSaving = uiState.isChecklistSaving,
            onDraftTitleChange = onChecklistTitleChange,
            onAdd = onAddChecklistItem,
            onToggle = onToggleChecklistItem,
            onDelete = onDeleteChecklistItem
        )
    }
}

@Composable
private fun LoadingItineraryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Đang tải lịch trình...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyItineraryState(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Chưa có lịch trình cho chuyến đi này.",
        modifier = modifier.padding(24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CenterContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ParsedItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(
                sessionId = 1L,
                title = "3 ngày Đà Nẵng",
                days = remember { sampleDays() }
            ),
            onBack = {},
            onOpenChat = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RawItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(
                sessionId = 1L,
                title = "Chuyến đi mới",
                rawText = "TravelAI chưa nhận diện được cấu trúc ngày/buổi, nhưng vẫn giữ bản trả lời gốc để bạn đọc lại."
            ),
            onBack = {},
            onOpenChat = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(isLoading = true),
            onBack = {},
            onOpenChat = {}
        )
    }
}

private fun sampleDays(): List<TripPlanDay> = listOf(
    TripPlanDay(
        dayNumber = 1,
        title = "Bãi biển Mỹ Khê và Sơn Trà",
        periods = listOf(
            TripPlanPeriod(
                period = TripPlanPeriodType.MORNING,
                content = "Tắm biển Mỹ Khê, ăn sáng mì Quảng."
            ),
            TripPlanPeriod(
                period = TripPlanPeriodType.AFTERNOON,
                content = "Tham quan chùa Linh Ứng và bán đảo Sơn Trà."
            ),
            TripPlanPeriod(
                period = TripPlanPeriodType.EVENING,
                content = "Dạo cầu Rồng và ăn hải sản ven biển."
            )
        )
    ),
    TripPlanDay(
        dayNumber = 2,
        title = "Hội An",
        periods = listOf(
            TripPlanPeriod(
                period = TripPlanPeriodType.MORNING,
                content = "Di chuyển đến phố cổ Hội An."
            )
        )
    )
)
