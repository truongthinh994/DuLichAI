package com.travelai.ui.itinerary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.parseBudgetAmount
import com.travelai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState(isLoading = true))
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private val requestedSessionId: Long? =
        savedStateHandle.get<Long>(SESSION_ID_ARG)
            ?: savedStateHandle.get<String>(SESSION_ID_ARG)?.toLongOrNull()

    init {
        loadItinerary()
    }

    fun loadItinerary() {
        val sessionId = requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Không tìm thấy chuyến đi."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.loadSession(sessionId)
                    ?: throw IllegalArgumentException("Không tìm thấy chuyến đi.")
            }.onSuccess { session ->
                val snapshot = session.tripPlanSnapshot
                val fallbackText = snapshot?.rawResponse
                    ?.takeIf { it.isNotBlank() }
                    ?: session.messages
                        .lastOrNull { it.role == ROLE_ASSISTANT }
                        ?.content
                        .orEmpty()

                _uiState.update {
                    it.copy(
                        sessionId = session.id,
                        title = session.title,
                        days = snapshot?.days.orEmpty(),
                        rawText = fallbackText,
                        budgetItems = session.budgetItems,
                        checklistItems = session.checklistItems,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải lịch trình."
                    )
                }
            }
        }
    }

    fun onBudgetCategoryChange(category: BudgetCategory) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(category = category),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetTitleChange(title: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(title = title),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetAmountChange(amountText: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(amountText = amountText),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetNoteChange(note: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(note = note),
                budgetErrorMessage = null
            )
        }
    }

    fun saveBudgetItem() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update { it.copy(budgetErrorMessage = "Không tìm thấy chuyến đi.") }
            return
        }

        val form = state.budgetForm
        val amountVnd = parseBudgetAmount(form.amountText)
        if (amountVnd == null) {
            _uiState.update { it.copy(budgetErrorMessage = "Nhập chi phí lớn hơn 0.") }
            return
        }

        val title = form.title.trim().ifBlank { form.category.label }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBudgetSaving = true,
                    budgetErrorMessage = null
                )
            }

            runCatching {
                if (form.editingItemId == null) {
                    chatRepository.addBudgetItem(
                        sessionId = sessionId,
                        category = form.category,
                        title = title,
                        amountVnd = amountVnd,
                        note = form.note
                    )
                } else {
                    chatRepository.updateBudgetItem(
                        sessionId = sessionId,
                        itemId = form.editingItemId,
                        category = form.category,
                        title = title,
                        amountVnd = amountVnd,
                        note = form.note
                    )
                }
                chatRepository.getBudgetItems(sessionId)
            }.onSuccess { budgetItems ->
                _uiState.update {
                    it.copy(
                        budgetItems = budgetItems,
                        budgetForm = BudgetFormState(),
                        isBudgetSaving = false,
                        budgetErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isBudgetSaving = false,
                        budgetErrorMessage = throwable.message ?: "Không thể lưu budget."
                    )
                }
            }
        }
    }

    fun editBudgetItem(item: BudgetItem) {
        _uiState.update {
            it.copy(
                budgetForm = BudgetFormState(
                    editingItemId = item.id,
                    category = item.category,
                    title = item.title,
                    amountText = item.amountVnd.toString(),
                    note = item.note
                ),
                budgetErrorMessage = null
            )
        }
    }

    fun cancelBudgetEdit() {
        _uiState.update {
            it.copy(
                budgetForm = BudgetFormState(),
                budgetErrorMessage = null
            )
        }
    }

    fun deleteBudgetItem(item: BudgetItem) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBudgetSaving = true,
                    budgetErrorMessage = null
                )
            }

            runCatching {
                chatRepository.deleteBudgetItem(sessionId = sessionId, itemId = item.id)
                chatRepository.getBudgetItems(sessionId)
            }.onSuccess { budgetItems ->
                _uiState.update {
                    it.copy(
                        budgetItems = budgetItems,
                        budgetForm = if (it.budgetForm.editingItemId == item.id) {
                            BudgetFormState()
                        } else {
                            it.budgetForm
                        },
                        isBudgetSaving = false,
                        budgetErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isBudgetSaving = false,
                        budgetErrorMessage = throwable.message ?: "Không thể xóa budget."
                    )
                }
            }
        }
    }

    fun onChecklistTitleChange(title: String) {
        _uiState.update {
            it.copy(
                checklistDraftTitle = title,
                checklistErrorMessage = null
            )
        }
    }

    fun addChecklistItem() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update { it.copy(checklistErrorMessage = "Không tìm thấy chuyến đi.") }
            return
        }

        val title = state.checklistDraftTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(checklistErrorMessage = "Nhập việc cần chuẩn bị.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.addChecklistItem(sessionId = sessionId, title = title)
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        checklistDraftTitle = "",
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể lưu checklist."
                    )
                }
            }
        }
    }

    fun toggleChecklistItem(item: ChecklistItem, isChecked: Boolean) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.updateChecklistItemChecked(
                    sessionId = sessionId,
                    itemId = item.id,
                    isChecked = isChecked
                )
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể cập nhật checklist."
                    )
                }
            }
        }
    }

    fun deleteChecklistItem(item: ChecklistItem) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.deleteChecklistItem(sessionId = sessionId, itemId = item.id)
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể xóa checklist."
                    )
                }
            }
        }
    }

    private companion object {
        const val SESSION_ID_ARG = "sessionId"
        const val ROLE_ASSISTANT = "assistant"
    }
}

data class ItineraryUiState(
    val sessionId: Long? = null,
    val title: String = "",
    val days: List<TripPlanDay> = emptyList(),
    val rawText: String = "",
    val budgetItems: List<BudgetItem> = emptyList(),
    val budgetForm: BudgetFormState = BudgetFormState(),
    val budgetErrorMessage: String? = null,
    val isBudgetSaving: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val checklistDraftTitle: String = "",
    val checklistErrorMessage: String? = null,
    val isChecklistSaving: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class BudgetFormState(
    val editingItemId: Long? = null,
    val category: BudgetCategory = BudgetCategory.FOOD,
    val title: String = "",
    val amountText: String = "",
    val note: String = ""
)
