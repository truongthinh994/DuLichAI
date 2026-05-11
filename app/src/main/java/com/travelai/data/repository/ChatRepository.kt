package com.travelai.data.repository

import com.google.gson.Gson
import com.travelai.data.api.DeepSeekApi
import com.travelai.data.api.DeepSeekChatRequest
import com.travelai.data.api.DeepSeekMessage
import com.travelai.data.db.ChatDao
import com.travelai.data.db.entities.BudgetItemEntity
import com.travelai.data.db.entities.ChecklistItemEntity
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripExport
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanSnapshot
import com.travelai.data.model.TripProfile
import com.travelai.data.model.toShareText
import com.travelai.data.model.toSessionTitle
import com.travelai.data.parser.ItineraryParser
import javax.inject.Inject
import javax.inject.Named

class ChatRepository @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val chatDao: ChatDao,
    @Named("DeepSeekApiKey") private val apiKey: String
) {
    private val gson = Gson()

    suspend fun loadLatestSession(): StoredChatSession? {
        val session = chatDao.getLatestSession() ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun loadSession(sessionId: Long): StoredChatSession? {
        val session = chatDao.getSession(sessionId) ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun getSessions(): List<StoredChatSessionSummary> =
        chatDao.getSessions().map { session ->
            StoredChatSessionSummary(
                id = session.id,
                title = session.title,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
                isPinned = session.isPinned
            )
        }

    private suspend fun loadSessionFromEntity(session: ChatSessionEntity): StoredChatSession {
        val messages = chatDao.getMessagesForSession(session.id)
        val tripProfile = chatDao.getTripProfile(session.id)
        val tripPlanSnapshot = chatDao.getTripPlanSnapshot(session.id)
        val budgetItems = chatDao.getBudgetItems(session.id)
        val checklistItems = chatDao.getChecklistItems(session.id)

        return StoredChatSession(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isPinned = session.isPinned,
            tripProfile = tripProfile?.toTripProfile(),
            tripPlanSnapshot = tripPlanSnapshot?.toTripPlanSnapshot(gson),
            budgetItems = budgetItems.map { it.toBudgetItem() },
            checklistItems = checklistItems.map { it.toChecklistItem() },
            messages = messages.map {
                StoredChatMessage(
                    role = it.role,
                    content = it.content,
                    createdAt = it.createdAt
                )
            }
        )
    }

    suspend fun createTripSession(profile: TripProfile): Long {
        val now = System.currentTimeMillis()
        return chatDao.insertSessionAndProfile(
            session = ChatSessionEntity(
                title = profile.toSessionTitle(),
                createdAt = now,
                updatedAt = now
            ),
            profile = profile.toEntity(sessionId = 0L, createdAt = now)
        )
    }

    suspend fun createSession(firstMessage: String): Long {
        val now = System.currentTimeMillis()
        return chatDao.insertSession(
            ChatSessionEntity(
                title = createSessionTitle(firstMessage),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun saveMessage(sessionId: Long, role: String, content: String) {
        val now = System.currentTimeMillis()
        chatDao.insertMessageAndTouchSession(
            message = ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                content = content,
                createdAt = now
            ),
            updatedAt = now
        )
    }

    suspend fun getBudgetItems(sessionId: Long): List<BudgetItem> =
        chatDao.getBudgetItems(sessionId).map { it.toBudgetItem() }

    suspend fun getChecklistItems(sessionId: Long): List<ChecklistItem> =
        chatDao.getChecklistItems(sessionId).map { it.toChecklistItem() }

    suspend fun addBudgetItem(
        sessionId: Long,
        category: BudgetCategory,
        title: String,
        amountVnd: Long,
        note: String
    ): BudgetItem {
        val now = System.currentTimeMillis()
        val itemId = chatDao.insertBudgetItemAndTouchSession(
            item = BudgetItemEntity(
                sessionId = sessionId,
                category = category.name,
                title = title.trim(),
                amountVnd = amountVnd,
                note = note.trim(),
                createdAt = now,
                updatedAt = now
            ),
            updatedAt = now
        )
        return BudgetItem(
            id = itemId,
            sessionId = sessionId,
            category = category,
            title = title.trim(),
            amountVnd = amountVnd,
            note = note.trim(),
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun updateBudgetItem(
        sessionId: Long,
        itemId: Long,
        category: BudgetCategory,
        title: String,
        amountVnd: Long,
        note: String
    ) {
        val now = System.currentTimeMillis()
        chatDao.updateBudgetItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            category = category.name,
            title = title.trim(),
            amountVnd = amountVnd,
            note = note.trim(),
            updatedAt = now
        )
    }

    suspend fun deleteBudgetItem(sessionId: Long, itemId: Long) {
        chatDao.deleteBudgetItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun addChecklistItem(
        sessionId: Long,
        title: String
    ): ChecklistItem {
        val now = System.currentTimeMillis()
        val cleanTitle = title.trim()
        val itemId = chatDao.insertChecklistItemAndTouchSession(
            item = ChecklistItemEntity(
                sessionId = sessionId,
                title = cleanTitle,
                isChecked = false,
                createdAt = now,
                updatedAt = now
            ),
            updatedAt = now
        )
        return ChecklistItem(
            id = itemId,
            sessionId = sessionId,
            title = cleanTitle,
            isChecked = false,
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun updateChecklistItemChecked(
        sessionId: Long,
        itemId: Long,
        isChecked: Boolean
    ) {
        val now = System.currentTimeMillis()
        chatDao.updateChecklistItemCheckedAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            isChecked = isChecked,
            updatedAt = now
        )
    }

    suspend fun deleteChecklistItem(sessionId: Long, itemId: Long) {
        chatDao.deleteChecklistItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun renameSession(sessionId: Long, title: String) {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotBlank()) { "Tên chuyến đi không được để trống." }
        chatDao.renameSession(
            sessionId = sessionId,
            title = cleanTitle.take(SESSION_TITLE_MAX_LENGTH).trimEnd(),
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateSessionPinned(sessionId: Long, isPinned: Boolean) {
        chatDao.updateSessionPinned(sessionId = sessionId, isPinned = isPinned)
    }

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun createTripExportText(sessionId: Long): String? =
        loadSession(sessionId)?.toTripExport()?.toShareText()

    suspend fun saveTripPlanSnapshot(
        sessionId: Long,
        rawResponse: String,
        keepRawWhenUnparsed: Boolean
    ): TripPlanSnapshot? {
        if (rawResponse.isBlank()) return null

        val parsedDays = ItineraryParser.parseDays(rawResponse)
        if (parsedDays.isEmpty() && !keepRawWhenUnparsed) return null

        val now = System.currentTimeMillis()
        val existingSnapshot = chatDao.getTripPlanSnapshot(sessionId)
        val snapshot = TripPlanSnapshot(
            sessionId = sessionId,
            rawResponse = rawResponse,
            days = parsedDays,
            createdAt = existingSnapshot?.createdAt ?: now,
            updatedAt = now
        )
        chatDao.upsertTripPlanSnapshot(snapshot.toEntity(gson))
        return snapshot
    }

    suspend fun sendMessage(messages: List<DeepSeekMessage>): String {
        if (messages.none { it.role == ROLE_USER && it.content.isNotBlank() }) {
            throw IllegalArgumentException("Tin nhắn không được để trống.")
        }
        if (apiKey.isBlank()) {
            throw IllegalStateException("Thiếu DEEPSEEK_API_KEY trong local.properties.")
        }

        val response = deepSeekApi.sendMessage(
            DeepSeekChatRequest(
                messages = messages
            )
        )

        return response.choices
            .firstOrNull()
            ?.message
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("DeepSeek không trả về nội dung.")
    }

    private fun createSessionTitle(firstMessage: String): String {
        val title = firstMessage.trim().lineSequence().firstOrNull().orEmpty()
        if (title.isBlank()) return "New trip"
        return if (title.length <= SESSION_TITLE_MAX_LENGTH) {
            title
        } else {
            title.take(SESSION_TITLE_MAX_LENGTH).trimEnd()
        }
    }

    private companion object {
        const val ROLE_USER = "user"
        const val SESSION_TITLE_MAX_LENGTH = 60
    }
}

private fun TripProfileEntity.toTripProfile(): TripProfile = TripProfile(
    destination = destination,
    days = days,
    budget = budget,
    people = people,
    travelStyle = travelStyle,
    transport = transport,
    note = note
)

private fun TripProfile.toEntity(
    sessionId: Long,
    createdAt: Long
): TripProfileEntity = TripProfileEntity(
    sessionId = sessionId,
    destination = destination.trim(),
    days = days,
    budget = budget.trim(),
    people = people,
    travelStyle = travelStyle.trim(),
    transport = transport.trim(),
    note = note.trim(),
    createdAt = createdAt
)

data class StoredChatSession(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val tripProfile: TripProfile?,
    val tripPlanSnapshot: TripPlanSnapshot?,
    val budgetItems: List<BudgetItem>,
    val checklistItems: List<ChecklistItem>,
    val messages: List<StoredChatMessage>
)

data class StoredChatSessionSummary(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean
)

data class StoredChatMessage(
    val role: String,
    val content: String,
    val createdAt: Long
)

private fun TripPlanSnapshotEntity.toTripPlanSnapshot(gson: Gson): TripPlanSnapshot =
    TripPlanSnapshot(
        sessionId = sessionId,
        rawResponse = rawResponse,
        days = parsedDays(gson),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private fun TripPlanSnapshotEntity.parsedDays(gson: Gson): List<TripPlanDay> {
    val json = parsedJson?.takeIf { it.isNotBlank() } ?: return emptyList()
    return runCatching {
        gson.fromJson(json, TripPlanSnapshotPayload::class.java)?.days.orEmpty()
    }.getOrDefault(emptyList())
}

private fun TripPlanSnapshot.toEntity(gson: Gson): TripPlanSnapshotEntity =
    TripPlanSnapshotEntity(
        sessionId = sessionId,
        rawResponse = rawResponse,
        parsedJson = days.takeIf { it.isNotEmpty() }?.let { parsedDays ->
            gson.toJson(TripPlanSnapshotPayload(days = parsedDays))
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private data class TripPlanSnapshotPayload(
    val days: List<TripPlanDay>
)

private fun BudgetItemEntity.toBudgetItem(): BudgetItem = BudgetItem(
    id = id,
    sessionId = sessionId,
    category = category.toBudgetCategory(),
    title = title,
    amountVnd = amountVnd,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String.toBudgetCategory(): BudgetCategory =
    runCatching { BudgetCategory.valueOf(this) }
        .getOrDefault(BudgetCategory.INCIDENTAL)

private fun ChecklistItemEntity.toChecklistItem(): ChecklistItem = ChecklistItem(
    id = id,
    sessionId = sessionId,
    title = title,
    isChecked = isChecked,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun StoredChatSession.toTripExport(): TripExport =
    TripExport(
        title = title,
        tripProfile = tripProfile,
        tripPlanSnapshot = tripPlanSnapshot,
        budgetItems = budgetItems,
        checklistItems = checklistItems,
        fallbackAssistantText = messages.asReversed()
            .firstOrNull { it.role == "assistant" }
            ?.content
    )
