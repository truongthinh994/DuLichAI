package com.travelai.ui.chat

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.chat.components.ChatBubble
import com.travelai.ui.chat.components.MessageInput
import com.travelai.ui.share.shareTripText
import com.travelai.ui.theme.TravelAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenPlanner: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenItinerary: (Long) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreenContent(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::sendMessage,
        onRetry = viewModel::retryLastMessage,
        onShare = viewModel::shareCurrentSession,
        onShareConsumed = viewModel::consumeShareText,
        onOpenPlanner = onOpenPlanner,
        onOpenHistory = onOpenHistory,
        onOpenItinerary = onOpenItinerary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onShareConsumed: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenItinerary: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { exportText ->
            shareTripText(context, exportText)
            onShareConsumed()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("TravelAI") },
                actions = {
                    IconButton(
                        onClick = onShare,
                        enabled = uiState.sessionId != null && !uiState.isSharing
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Chia sẻ")
                    }
                    IconButton(onClick = onOpenPlanner) {
                        Icon(Icons.Filled.Add, contentDescription = "Tạo chuyến mới")
                    }
                    IconButton(
                        onClick = { uiState.sessionId?.let(onOpenItinerary) },
                        enabled = uiState.sessionId != null
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Xem lịch trình")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.Menu, contentDescription = "Trip Library")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                value = uiState.inputText,
                onValueChange = onInputChange,
                onSend = onSend,
                isLoading = uiState.isLoading,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        ChatMessages(
            uiState = uiState,
            onRetry = onRetry,
            onCopyAssistantMessage = { content ->
                clipboardManager.setText(AnnotatedString(content))
                Toast.makeText(context, "Đã copy lịch trình", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun ChatMessages(
    uiState: ChatUiState,
    onRetry: () -> Unit,
    onCopyAssistantMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (
        uiState.messages.isEmpty() &&
        !uiState.isLoading &&
        uiState.errorMessage == null &&
        uiState.offlineBannerMessage == null
    ) {
        EmptyChatPlaceholder(modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    val itemCount = uiState.messages.size +
        (if (uiState.offlineBannerMessage != null) 1 else 0) +
        (if (uiState.isLoading) 1 else 0) +
        (if (uiState.errorMessage != null) 1 else 0)

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        uiState.offlineBannerMessage?.let { offlineMessage ->
            item(key = "offline-banner") {
                OfflineBanner(message = offlineMessage)
            }
        }

        itemsIndexed(
            items = uiState.messages,
            key = { index, _ -> "message-$index" }
        ) { _, message ->
            ChatBubble(
                role = message.role,
                content = message.content,
                onLongPress = if (message.role == ChatRole.ASSISTANT) {
                    { onCopyAssistantMessage(message.content) }
                } else {
                    null
                }
            )
        }

        if (uiState.isLoading) {
            item(key = "loading") {
                AssistantLoadingIndicator()
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            item(key = "error") {
                ErrorMessageCard(
                    message = errorMessage,
                    canRetry = uiState.canRetry,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            if (canRetry) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Thử lại")
                }
            }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hỏi tôi về chuyến đi của bạn...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AssistantLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.USER,
                        content = "Gợi ý 3 ngày Đà Nẵng"
                    ),
                    ChatMessage(
                        role = ChatRole.ASSISTANT,
                        content = "Bạn có thể dành ngày đầu tiên để khám phá bán đảo Sơn Trà."
                    )
                )
            ),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.USER,
                        content = "Gợi ý 3 ngày Đà Nẵng"
                    )
                ),
                isLoading = true
            ),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetryErrorChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.USER,
                        content = "Gợi ý 3 ngày Đà Nẵng"
                    )
                ),
                errorMessage = "Không phản hồi, thử lại?",
                canRetry = true
            ),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(
                inputText = "Gợi ý 3 ngày Đà Nẵng",
                offlineBannerMessage = "Không có kết nối internet. Kiểm tra mạng rồi thử lại."
            ),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {}
        )
    }
}
