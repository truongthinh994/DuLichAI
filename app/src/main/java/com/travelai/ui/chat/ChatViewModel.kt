package com.travelai.ui.chat

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.api.DeepSeekMessage
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.StoredChatMessage
import com.travelai.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long? = null
    private var pendingRetry: PendingRetry? = null
    private val requestedSessionId: Long? = savedStateHandle
        .get<String>(SESSION_ID_ARG)
        ?.toLongOrNull()
        ?.takeIf { it > 0 }

    init {
        loadInitialSession()
    }

    fun onInputChange(inputText: String) {
        pendingRetry = null
        _uiState.update {
            it.copy(
                inputText = inputText,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val messageText = state.inputText.trim()
        if (messageText.isBlank() || state.isLoading) return
        pendingRetry = null

        if (!hasInternetConnection()) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    canRetry = false,
                    offlineBannerMessage = OFFLINE_MESSAGE
                )
            }
            return
        }

        val userMessage = ChatMessage(
            role = ChatRole.USER,
            content = messageText
        )
        val updatedMessages = state.messages + userMessage

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                inputText = "",
                isLoading = true,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }

        viewModelScope.launch {
            var retryCandidate: PendingRetry? = null

            try {
                val sessionId = getOrCreateSessionId(messageText)
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    role = ROLE_USER,
                    content = messageText
                )

                val deepSeekMessages = buildDeepSeekMessages(updatedMessages)
                retryCandidate = PendingRetry(
                    sessionId = sessionId,
                    messages = deepSeekMessages
                )

                val response = requestAssistantResponse(deepSeekMessages)
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    role = ROLE_ASSISTANT,
                    content = response
                )

                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            role = ChatRole.ASSISTANT,
                            content = response
                        ),
                        isLoading = false,
                        errorMessage = null,
                        canRetry = false,
                        offlineBannerMessage = null
                    )
                }
                pendingRetry = null
            } catch (throwable: Throwable) {
                handleSendFailure(
                    throwable = throwable,
                    retryCandidate = retryCandidate
                )
            }
        }
    }

    fun retryLastMessage() {
        val retry = pendingRetry ?: return
        if (_uiState.value.isLoading) return

        if (!hasInternetConnection()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    offlineBannerMessage = OFFLINE_MESSAGE
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = requestAssistantResponse(retry.messages)
                chatRepository.saveMessage(
                    sessionId = retry.sessionId,
                    role = ROLE_ASSISTANT,
                    content = response
                )

                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            role = ChatRole.ASSISTANT,
                            content = response
                        ),
                        isLoading = false,
                        errorMessage = null,
                        canRetry = false,
                        offlineBannerMessage = null
                    )
                }
                pendingRetry = null
            } catch (throwable: Throwable) {
                handleSendFailure(
                    throwable = throwable,
                    retryCandidate = retry
                )
            }
        }
    }

    private fun loadInitialSession() {
        viewModelScope.launch {
            runCatching {
                requestedSessionId?.let { sessionId ->
                    chatRepository.loadSession(sessionId)
                } ?: chatRepository.loadLatestSession()
            }.onSuccess { session ->
                if (
                    session != null &&
                    currentSessionId == null &&
                    _uiState.value.messages.isEmpty() &&
                    !_uiState.value.isLoading
                ) {
                    currentSessionId = session.id
                    _uiState.update {
                        it.copy(
                            messages = session.messages.mapNotNull { message ->
                                message.toChatMessage()
                            },
                            errorMessage = null,
                            canRetry = false,
                            offlineBannerMessage = null
                        )
                    }
                }
            }.onFailure { throwable ->
                val chatError = throwable.toChatError()
                _uiState.update {
                    it.copy(
                        errorMessage = chatError.message,
                        canRetry = false,
                        offlineBannerMessage = chatError.offlineBannerMessage
                    )
                }
            }
        }
    }

    private suspend fun requestAssistantResponse(messages: List<DeepSeekMessage>): String =
        withTimeout(API_RESPONSE_TIMEOUT_MS) {
            chatRepository.sendMessage(messages)
        }

    private fun handleSendFailure(
        throwable: Throwable,
        retryCandidate: PendingRetry?
    ) {
        if (throwable is CancellationException && throwable !is TimeoutCancellationException) {
            throw throwable
        }

        val chatError = throwable.toChatError()
        pendingRetry = if (chatError.canRetry) retryCandidate else null

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = chatError.message,
                canRetry = chatError.canRetry,
                offlineBannerMessage = chatError.offlineBannerMessage
            )
        }
    }

    private suspend fun getOrCreateSessionId(firstMessage: String): Long {
        currentSessionId?.let { return it }
        return chatRepository.createSession(firstMessage).also { sessionId ->
            currentSessionId = sessionId
        }
    }

    private fun buildDeepSeekMessages(messages: List<ChatMessage>): List<DeepSeekMessage> {
        val conversationMessages = messages.map { it.toDeepSeekMessage() }
        return listOf(
            DeepSeekMessage(
                role = ROLE_SYSTEM,
                content = Constants.SYSTEM_PROMPT
            )
        ) + trimContextMessages(conversationMessages)
    }

    private fun trimContextMessages(messages: List<DeepSeekMessage>): List<DeepSeekMessage> {
        var totalChars = Constants.SYSTEM_PROMPT.length
        val keptReversed = mutableListOf<DeepSeekMessage>()

        for (message in messages.asReversed()) {
            val messageChars = message.role.length + message.content.length
            if (keptReversed.isEmpty() || totalChars + messageChars <= Constants.MAX_CONTEXT_CHARS) {
                keptReversed += message
                totalChars += messageChars
            }
        }

        return keptReversed.asReversed()
    }

    private fun ChatMessage.toDeepSeekMessage(): DeepSeekMessage = DeepSeekMessage(
        role = when (role) {
            ChatRole.USER -> ROLE_USER
            ChatRole.ASSISTANT -> ROLE_ASSISTANT
        },
        content = content
    )

    private fun StoredChatMessage.toChatMessage(): ChatMessage? {
        val chatRole = when (role) {
            ROLE_USER -> ChatRole.USER
            ROLE_ASSISTANT -> ChatRole.ASSISTANT
            else -> return null
        }
        return ChatMessage(
            role = chatRole,
            content = content
        )
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun Throwable.toChatError(): ChatError = when (this) {
        is TimeoutCancellationException,
        is SocketTimeoutException -> ChatError(
            message = TIMEOUT_MESSAGE,
            canRetry = true
        )

        is IOException -> {
            val offlineBannerMessage = if (!hasInternetConnection()) OFFLINE_MESSAGE else null
            ChatError(
                message = offlineBannerMessage ?: "Kết nối không ổn định. Vui lòng thử lại.",
                canRetry = false,
                offlineBannerMessage = offlineBannerMessage
            )
        }

        is HttpException -> ChatError(
            message = toHttpUserMessage(),
            canRetry = false
        )

        else -> ChatError(
            message = message ?: "Đã có lỗi xảy ra. Vui lòng thử lại.",
            canRetry = false
        )
    }

    private fun HttpException.toHttpUserMessage(): String = when (code()) {
        401 -> "DeepSeek từ chối API key (401). Kiểm tra DEEPSEEK_API_KEY."
        403 -> "DeepSeek không cho phép truy cập (403). Kiểm tra quyền API key."
        429 -> "DeepSeek đang giới hạn lượt gọi (429). Vui lòng thử lại sau."
        in 500..599 -> "DeepSeek đang lỗi máy chủ (${code()}). Vui lòng thử lại sau."
        else -> "DeepSeek trả về lỗi ${code()}. Vui lòng thử lại."
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false,
    val offlineBannerMessage: String? = null
)

data class ChatMessage(
    val role: ChatRole,
    val content: String
)

enum class ChatRole {
    USER,
    ASSISTANT
}

private data class PendingRetry(
    val sessionId: Long,
    val messages: List<DeepSeekMessage>
)

private data class ChatError(
    val message: String?,
    val canRetry: Boolean,
    val offlineBannerMessage: String? = null
)

private const val ROLE_SYSTEM = "system"
private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"
private const val SESSION_ID_ARG = "sessionId"
private const val API_RESPONSE_TIMEOUT_MS = 15_000L
private const val TIMEOUT_MESSAGE = "Không phản hồi, thử lại?"
private const val OFFLINE_MESSAGE = "Không có kết nối internet. Kiểm tra mạng rồi thử lại."
