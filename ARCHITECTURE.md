# ARCHITECTURE — TravelAI Assistant

> Phase 2 deliverable. Quyết định trước khi viết code.

---

## 1. Tech stack

| Layer | Choice | Version | Lý do |
|---|---|---|---|
| Language | Kotlin | 2.0+ | Modern Android standard, null-safe, coroutines |
| UI | Jetpack Compose | 1.6+ | Declarative, ít boilerplate, AI viết tốt hơn XML |
| Architecture | MVVM + Clean (light) | — | Standard Android, tách UI khỏi logic |
| HTTP Client | Retrofit 2 + OkHttp | 2.9 / 4.x | Type-safe API call, dễ mock test |
| JSON | Gson / Moshi | — | Serialize DeepSeek response |
| Async | Kotlin Coroutines + Flow | 1.7+ | Native Android async, tích hợp tốt Compose |
| Local DB | Room | 2.6+ | Lưu lịch sử chat (SHOULD feature) |
| DI | Hilt | 2.51+ | Dependency injection chuẩn Android |
| AI API | DeepSeek API | v1 | Rẻ, mạnh, OpenAI-compatible endpoint |
| Maps (sau) | Google Maps SDK | — | Để v2, không làm hôm nay |
| IDE build | Android Studio | Hedgehog+ | Build + emulator |
| IDE code | VSCode | latest | Vibecoding với Claude Code |
| Min SDK | Android 8.0 (API 26) | — | Cover ~95% thiết bị Android VN |
| Target SDK | Android 14 (API 34) | — | Play Store requirement |

---

## 2. System diagram

```
┌─────────────────────────────────┐
│         Android App             │
│                                 │
│  ┌──────────┐  ┌─────────────┐  │
│  │  Compose │  │  ViewModel  │  │
│  │   UI     │◄─┤  (StateFlow)│  │
│  └──────────┘  └──────┬──────┘  │
│                       │         │
│               ┌───────▼──────┐  │
│               │  Repository  │  │
│               └──┬───────┬───┘  │
│                  │       │      │
│         ┌────────▼─┐  ┌──▼────┐ │
│         │  Room DB │  │Retro- │ │
│         │ (local)  │  │fit API│ │
│         └──────────┘  └───┬───┘ │
└──────────────────────────┼──────┘
                            │ HTTPS
                    ┌───────▼────────┐
                    │  DeepSeek API  │
                    │ (OpenAI-compat)│
                    └────────────────┘
```

---

## 3. Frontend / State / Data split

1. **State sống ở đâu?**
   - UI state (loading, error, messages list) → ViewModel StateFlow
   - Lịch sử chat → Room DB (local, không sync)
   - API key → BuildConfig / local.properties (KHÔNG commit)

2. **Cái gì cần private?**
   - DeepSeek API key → BuildConfig, inject lúc build, không hardcode
   - System prompt → constants trong code (không cần hide)

3. **Cái gì cần realtime?**
   - Streaming response DeepSeek (nếu API support) → Flow + collect
   - Không cần WebSocket, không cần server push

4. **Cái gì cần persist qua reload?**
   - Lịch sử chat → Room DB
   - Settings (nếu có) → DataStore
   - Không sync lên server trong v1

---

## 4. Data model

```
// Room entities

ChatSession
  - id: Long (PK, autoGenerate)
  - title: String          // "3 ngày Đà Nẵng"
  - createdAt: Long        // timestamp
  - updatedAt: Long

ChatMessage
  - id: Long (PK, autoGenerate)
  - sessionId: Long (FK → ChatSession.id)
  - role: String           // "user" | "assistant"
  - content: String        // nội dung message
  - timestamp: Long

// API models (không persist)

DeepSeekRequest
  - model: String          // "deepseek-chat"
  - messages: List<Message>
  - temperature: Float
  - max_tokens: Int
  - stream: Boolean

Message
  - role: String
  - content: String

DeepSeekResponse
  - choices: List<Choice>
  - usage: Usage

Choice
  - message: Message
  - finish_reason: String
```

---

## 5. API surface

DeepSeek dùng OpenAI-compatible endpoint:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `https://api.deepseek.com/chat/completions` | Gửi chat message, nhận AI reply |

**System prompt mặc định:**
```
Bạn là trợ lý du lịch thông minh cho du khách Việt Nam. 
Bạn biết rõ các địa điểm du lịch Việt Nam và quốc tế.
Khi được hỏi về lịch trình, hãy trả lời theo format:
Ngày X: [Sáng] ... [Chiều] ... [Tối] ...
Luôn hỏi rõ budget và số người nếu user chưa cung cấp.
Trả lời bằng tiếng Việt, ngắn gọn, thực tế.
```

---

## 6. Folder structure (target)

```
app/
├── src/main/
│   ├── java/com/travelai/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── DeepSeekApi.kt       # Retrofit interface
│   │   │   │   ├── DeepSeekModels.kt    # Request/Response data classes
│   │   │   │   └── ApiClient.kt         # OkHttp + Retrofit setup
│   │   │   ├── db/
│   │   │   │   ├── AppDatabase.kt       # Room database
│   │   │   │   ├── ChatDao.kt           # Queries
│   │   │   │   └── entities/            # Room entities
│   │   │   └── repository/
│   │   │       └── ChatRepository.kt    # Single source of truth
│   │   ├── ui/
│   │   │   ├── chat/
│   │   │   │   ├── ChatScreen.kt        # Compose UI
│   │   │   │   ├── ChatViewModel.kt     # State + logic
│   │   │   │   └── components/          # ChatBubble, InputBar...
│   │   │   ├── history/
│   │   │   │   └── HistoryScreen.kt     # Danh sách chuyến đi
│   │   │   └── theme/
│   │   │       └── Theme.kt             # Material3 theme
│   │   ├── di/
│   │   │   └── AppModule.kt             # Hilt modules
│   │   └── MainActivity.kt
│   └── res/
├── build.gradle.kts
└── local.properties                     # DEEPSEEK_API_KEY (không commit)
```

---

## 7. External services

| Service | Purpose | Cost | Failure mode |
|---|---|---|---|
| DeepSeek API | AI chat completion | ~$0.14/1M tokens | App hiện error, retry button |
| Google Maps SDK | Maps (v2) | Free tier | Không làm v1 |

---

## 8. Assumptions nguy hiểm

1. **Assume:** DeepSeek API ổn định và < 5s response → **If wrong:** phải thêm
   timeout config + UX fallback, hoặc đổi sang Gemini API
2. **Assume:** Jetpack Compose AI code generation đủ chất lượng từ vibecoding →
   **If wrong:** tốn nhiều AI turns hơn dự kiến, cần verify kỹ hơn
3. **Assume:** Min SDK 26 là đủ cho target user VN → **If wrong:** một số user
   dùng Android 7 sẽ không cài được

---

## 9. Decisions log

| Date | Decision | Why | Alternative |
|---|---|---|---|
| 2026-05-04 | Dùng DeepSeek thay OpenAI | Chi phí thấp hơn 10x, OpenAI-compatible | OpenAI GPT-4o |
| 2026-05-04 | Không làm Google Maps v1 | Complexity cao, scope creep | Tích hợp ngay |
| 2026-05-04 | Local-only (no backend) | Đơn giản nhất, không cần server | Firebase |
| 2026-05-04 | Hilt cho DI | Standard Android, AI viết đúng pattern | Koin |

---

## Next step

→ Phase 3: TASKS.md
