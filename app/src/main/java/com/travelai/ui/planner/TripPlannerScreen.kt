package com.travelai.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.theme.TravelAITheme

@Composable
fun TripPlannerScreen(
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onCreateItinerary: (Long) -> Unit,
    viewModel: TripPlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdSessionId) {
        val sessionId = uiState.createdSessionId ?: return@LaunchedEffect
        onCreateItinerary(sessionId)
        viewModel.consumeCreatedSessionId()
    }

    TripPlannerContent(
        uiState = uiState,
        onDestinationChange = viewModel::onDestinationChange,
        onDaysChange = viewModel::onDaysChange,
        onBudgetChange = viewModel::onBudgetChange,
        onPeopleChange = viewModel::onPeopleChange,
        onTravelStyleChange = viewModel::onTravelStyleChange,
        onTransportChange = viewModel::onTransportChange,
        onNoteChange = viewModel::onNoteChange,
        onCreateTrip = viewModel::createTrip,
        onOpenChat = onOpenChat,
        onOpenHistory = onOpenHistory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripPlannerContent(
    uiState: TripPlannerUiState,
    onDestinationChange: (String) -> Unit,
    onDaysChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onPeopleChange: (String) -> Unit,
    onTravelStyleChange: (String) -> Unit,
    onTransportChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCreateTrip: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tạo chuyến đi") },
                actions = {
                    TextButton(onClick = onOpenChat) {
                        Text("Chat")
                    }
                    TextButton(onClick = onOpenHistory) {
                        Text("Lịch sử")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onCreateTrip,
                    enabled = !uiState.isCreating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Text(if (uiState.isCreating) "Đang tạo..." else "Tạo lịch trình")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nhập thông tin chính để TravelAI lập lịch trình sát nhu cầu hơn.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            PlannerTextField(
                value = uiState.destination,
                onValueChange = onDestinationChange,
                label = "Điểm đến",
                placeholder = "Ví dụ: Đà Nẵng"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlannerTextField(
                    value = uiState.days,
                    onValueChange = onDaysChange,
                    label = "Số ngày",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                PlannerTextField(
                    value = uiState.people,
                    onValueChange = onPeopleChange,
                    label = "Số người",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            PlannerTextField(
                value = uiState.budget,
                onValueChange = onBudgetChange,
                label = "Ngân sách",
                placeholder = "Ví dụ: 5 triệu / người"
            )
            PlannerTextField(
                value = uiState.travelStyle,
                onValueChange = onTravelStyleChange,
                label = "Phong cách",
                placeholder = "Nghỉ dưỡng, khám phá, ăn uống..."
            )
            PlannerTextField(
                value = uiState.transport,
                onValueChange = onTransportChange,
                label = "Phương tiện",
                placeholder = "Xe máy, taxi, đi bộ..."
            )
            PlannerTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = "Ghi chú thêm",
                placeholder = "Trẻ nhỏ, người lớn tuổi, không ăn cay...",
                minLines = 3
            )

            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PlannerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder) }
        } else {
            null
        },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Preview(showBackground = true)
@Composable
private fun TripPlannerScreenPreview() {
    TravelAITheme {
        TripPlannerContent(
            uiState = TripPlannerUiState(destination = "Đà Nẵng", budget = "5 triệu"),
            onDestinationChange = {},
            onDaysChange = {},
            onBudgetChange = {},
            onPeopleChange = {},
            onTravelStyleChange = {},
            onTransportChange = {},
            onNoteChange = {},
            onCreateTrip = {},
            onOpenChat = {},
            onOpenHistory = {}
        )
    }
}
