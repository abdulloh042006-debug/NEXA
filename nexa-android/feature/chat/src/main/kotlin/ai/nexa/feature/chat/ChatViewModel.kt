package ai.nexa.feature.chat

import ai.nexa.kernel.chat.ChatSendEvent
import ai.nexa.kernel.chat.ChatSessionPort
import ai.nexa.kernel.chat.ChatTurn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatSession: ChatSessionPort,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState.asStateFlow()
    private var conversationId: String? = null

    init {
        viewModelScope.launch {
            val id = chatSession.createConversation()
            conversationId = id
            chatSession.observeMessages(id).collect { messages ->
                mutableState.update { it.copy(messages = messages, isReady = true) }
            }
        }
    }

    fun updateDraft(value: String) {
        mutableState.update { it.copy(draft = value) }
    }

    fun send() {
        val id = conversationId ?: return
        val content = state.value.draft.trim()
        if (content.isEmpty() || state.value.isLoading) return
        mutableState.update {
            it.copy(draft = "", isLoading = true, streamedReply = "", error = null)
        }
        viewModelScope.launch {
            chatSession.sendMessage(id, content)
                .catch {
                    mutableState.update { state ->
                        state.copy(streamedReply = "", error = ChatUiError.MODEL_UNAVAILABLE)
                    }
                }
                .onCompletion {
                    mutableState.update { state -> state.copy(isLoading = false) }
                }
                .collect { event ->
                    when (event) {
                        ChatSendEvent.UserStored -> Unit
                        ChatSendEvent.ReplyStored -> mutableState.update { it.copy(streamedReply = "") }
                        is ChatSendEvent.ReplyToken -> mutableState.update {
                            it.copy(streamedReply = it.streamedReply + event.text)
                        }
                    }
                }
        }
    }
}

data class ChatUiState(
    val messages: List<ChatTurn> = emptyList(),
    val draft: String = "",
    val streamedReply: String = "",
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val error: ChatUiError? = null,
)

enum class ChatUiError { MODEL_UNAVAILABLE }
