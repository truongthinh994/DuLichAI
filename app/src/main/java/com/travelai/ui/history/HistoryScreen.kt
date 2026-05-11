package com.travelai.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.share.shareTripText
import com.travelai.ui.theme.TravelAITheme

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onOpenItinerary: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { exportText ->
            shareTripText(context, exportText)
            viewModel.consumeShareText()
        }
    }

    HistoryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSessionClick = onSessionClick,
        onOpenItinerary = onOpenItinerary,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onStartRename = viewModel::startRenameSession,
        onRenameTitleChange = viewModel::onRenameTitleChange,
        onConfirmRename = viewModel::confirmRenameSession,
        onDismissRename = viewModel::dismissRenameSession,
        onRequestDelete = viewModel::requestDeleteSession,
        onConfirmDelete = viewModel::confirmDeleteSession,
        onDismissDelete = viewModel::dismissDeleteSession,
        onTogglePinned = viewModel::togglePinned,
        onShareSession = viewModel::shareSession
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStartRename: (HistorySession) -> Unit,
    onRenameTitleChange: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onDismissRename: () -> Unit,
    onRequestDelete: (HistorySession) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onTogglePinned: (HistorySession) -> Unit,
    onShareSession: (HistorySession) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Trip Library") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TripLibrarySearchField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            when {
                uiState.isLoading -> CenterContent(modifier = Modifier.weight(1f)) {
                    HistoryLoadingState()
                }

                uiState.errorMessage != null -> CenterContent(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                uiState.sessions.isEmpty() && uiState.hasSearchQuery -> CenterContent(
                    modifier = Modifier.weight(1f)
                ) {
                    NoSearchResultsState(query = uiState.searchQuery)
                }

                uiState.sessions.isEmpty() -> CenterContent(modifier = Modifier.weight(1f)) {
                    EmptyHistoryState()
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.sessions,
                        key = { session -> session.id }
                    ) { session ->
                        HistorySessionRow(
                            session = session,
                            isSaving = uiState.isSavingSession,
                            isSharing = uiState.sharingSessionId == session.id,
                            onClick = { onSessionClick(session.id) },
                            onOpenItinerary = { onOpenItinerary(session.id) },
                            onStartRename = { onStartRename(session) },
                            onRequestDelete = { onRequestDelete(session) },
                            onTogglePinned = { onTogglePinned(session) },
                            onShareSession = { onShareSession(session) }
                        )
                    }
                }
            }
        }
    }

    uiState.renamingSession?.let { session ->
        RenameSessionDialog(
            session = session,
            title = uiState.renameTitle,
            errorMessage = uiState.renameErrorMessage,
            isSaving = uiState.isSavingSession,
            onTitleChange = onRenameTitleChange,
            onConfirm = onConfirmRename,
            onDismiss = onDismissRename
        )
    }

    uiState.deletingSession?.let { session ->
        DeleteSessionDialog(
            session = session,
            isSaving = uiState.isSavingSession,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete
        )
    }
}

@Composable
private fun TripLibrarySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = { Text("Tìm chuyến đi") },
        placeholder = { Text("Nhập tên chuyến đi") }
    )
}

@Composable
private fun HistoryLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Đang tải thư viện chuyến đi...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyHistoryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmptyHistoryIllustration()
        Text(
            text = "Chưa có chuyến đi nào",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tạo chuyến mới từ Planner để lưu lịch trình, ngân sách và checklist vào thư viện.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Không tìm thấy chuyến đi",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Không có title nào khớp với \"$query\".",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyHistoryIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                )
        )
        Column(
            modifier = Modifier
                .size(width = 54.dp, height = 62.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun HistorySessionRow(
    session: HistorySession,
    isSaving: Boolean,
    isSharing: Boolean,
    onClick: () -> Unit,
    onOpenItinerary: () -> Unit,
    onStartRename: () -> Unit,
    onRequestDelete: () -> Unit,
    onTogglePinned: () -> Unit,
    onShareSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (session.isPinned) {
                    PinnedBadge()
                }
            }
            Text(
                text = session.createdAtText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick) {
                    Text("Chat")
                }
                TextButton(onClick = onOpenItinerary) {
                    Text("Lịch trình")
                }
                SessionOverflowMenu(
                    isPinned = session.isPinned,
                    isSaving = isSaving,
                    isSharing = isSharing,
                    onShareSession = onShareSession,
                    onTogglePinned = onTogglePinned,
                    onStartRename = onStartRename,
                    onRequestDelete = onRequestDelete
                )
            }
        }
    }
}

@Composable
private fun SessionOverflowMenu(
    isPinned: Boolean,
    isSaving: Boolean,
    isSharing: Boolean,
    onShareSession: () -> Unit,
    onTogglePinned: () -> Unit,
    onStartRename: () -> Unit,
    onRequestDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Thao tác khác")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (isSharing) "Đang xuất..." else "Chia sẻ") },
                enabled = !isSharing,
                onClick = {
                    expanded = false
                    onShareSession()
                }
            )
            DropdownMenuItem(
                text = { Text(if (isPinned) "Bỏ ghim" else "Ghim") },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onTogglePinned()
                }
            )
            DropdownMenuItem(
                text = { Text("Đổi tên") },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onStartRename()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Xóa",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onRequestDelete()
                }
            )
        }
    }
}

@Composable
private fun PinnedBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "Đã ghim",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun RenameSessionDialog(
    session: HistorySession,
    title: String,
    errorMessage: String?,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên chuyến đi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = session.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    label = { Text("Tên mới") }
                )
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun DeleteSessionDialog(
    session: HistorySession,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa chuyến đi?") },
        text = {
            Text("Xóa \"${session.title}\" khỏi máy này. Tin nhắn, lịch trình, ngân sách và checklist sẽ bị xóa theo.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text(
                    text = "Xóa",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Hủy")
            }
        }
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
private fun HistoryScreenPreview() {
    TravelAITheme {
        HistoryScreenContent(
            uiState = HistoryUiState(
                sessions = listOf(
                    HistorySession(
                        id = 1L,
                        title = "3 ngày Đà Nẵng",
                        createdAtText = "04/05/2026 14:30",
                        isPinned = true
                    ),
                    HistorySession(
                        id = 2L,
                        title = "2 ngày Hội An",
                        createdAtText = "05/05/2026 09:15",
                        isPinned = false
                    )
                )
            ),
            onBack = {},
            onSessionClick = {},
            onOpenItinerary = {},
            onSearchQueryChange = {},
            onStartRename = {},
            onRenameTitleChange = {},
            onConfirmRename = {},
            onDismissRename = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onTogglePinned = {},
            onShareSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyHistoryScreenPreview() {
    TravelAITheme {
        HistoryScreenContent(
            uiState = HistoryUiState(),
            onBack = {},
            onSessionClick = {},
            onOpenItinerary = {},
            onSearchQueryChange = {},
            onStartRename = {},
            onRenameTitleChange = {},
            onConfirmRename = {},
            onDismissRename = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onTogglePinned = {},
            onShareSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingHistoryScreenPreview() {
    TravelAITheme {
        HistoryScreenContent(
            uiState = HistoryUiState(isLoading = true),
            onBack = {},
            onSessionClick = {},
            onOpenItinerary = {},
            onSearchQueryChange = {},
            onStartRename = {},
            onRenameTitleChange = {},
            onConfirmRename = {},
            onDismissRename = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onTogglePinned = {},
            onShareSession = {}
        )
    }
}
