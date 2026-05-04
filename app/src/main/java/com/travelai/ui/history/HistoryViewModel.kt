package com.travelai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.StoredChatSessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.getSessions().map { session ->
                    session.toHistorySession()
                }
            }.onSuccess { sessions ->
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Khong the tai lich su."
                    )
                }
            }
        }
    }

    private fun StoredChatSessionSummary.toHistorySession(): HistorySession =
        HistorySession(
            id = id,
            title = title,
            createdAtText = formatTimestamp(createdAt)
        )

    private fun formatTimestamp(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(HISTORY_DATE_FORMATTER)
}

data class HistoryUiState(
    val sessions: List<HistorySession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class HistorySession(
    val id: Long,
    val title: String,
    val createdAtText: String
)

private val HISTORY_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
