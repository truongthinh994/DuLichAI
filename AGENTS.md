# AGENTS.md — TravelAI Assistant

> File này được Claude Code / AI agent đọc tự động. Đây là knowledge persistence
> layer — mọi context AI cần đều ở đây. Update ngay khi có decision mới hoặc bug mới.

---

## 1. Project mission

TravelAI là app Android trợ lý du lịch AI cho du khách Việt tự túc. User chat
bằng tiếng Việt, AI (DeepSeek) trả lời gợi ý địa điểm và tự động lập lịch trình
theo ngày/buổi. App local-only, không có backend, không có account.

---

## 2. Tech stack (cụ thể, có version)

- **Language:** Kotlin 2.0+
- **UI:** Jetpack Compose 1.6+ (Material3)
- **Architecture:** MVVM — ViewModel + StateFlow + Repository
- **HTTP:** Retrofit 2.9 + OkHttp 4.x + Gson converter
- **Async:** Kotlin Coroutines + Flow (viewModelScope)
- **DB local:** Room 2.6+
- **DI:** Hilt 2.51+
- **AI API:** DeepSeek API (OpenAI-compatible, endpoint: `https://api.deepseek.com`)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Build system:** Gradle với Kotlin DSL (`build.gradle.kts`)
- **IDE build/run:** Android Studio (Hedgehog trở lên)
- **IDE code:** VSCode

---

## 3. Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Chạy unit tests
./gradlew test

# Chạy instrumented tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean build
./gradlew clean assembleDebug

# Cài APK lên thiết bị đang kết nối
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 4. Folder conventions

```
app/src/main/java/com/travelai/
├── data/
│   ├── api/
│   │   ├── DeepSeekApi.kt         # Retrofit interface, 1 function sendMessage()
│   │   ├── DeepSeekModels.kt      # data class Request, Response, Message, Choice
│   │   └── ApiClient.kt           # OkHttp singleton + Retrofit builder
│   ├── db/
│   │   ├── AppDatabase.kt         # Room @Database, version 1
│   │   ├── ChatDao.kt             # @Dao queries
│   │   └── entities/
│   │       ├── ChatSession.kt     # @Entity
│   │       └── ChatMessage.kt     # @Entity
│   └── repository/
│       └── ChatRepository.kt      # inject ApiClient + Dao, expose suspend fun
├── ui/
│   ├── chat/
│   │   ├── ChatScreen.kt          # @Composable, dùng ChatViewModel
│   │   ├── ChatViewModel.kt       # @HiltViewModel, StateFlow<ChatUiState>
│   │   └── components/
│   │       ├── ChatBubble.kt      # user bubble (right) / AI bubble (left)
│   │       └── MessageInput.kt    # TextField + Send button
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   ├── navigation/
│   │   └── NavGraph.kt            # NavHost, routes: "chat", "history"
│   └── theme/
│       └── Theme.kt               # MaterialTheme, colors, typography
├── di/
│   └── AppModule.kt               # @Module @InstallIn(SingletonComponent)
├── util/
│   └── Constants.kt               # SYSTEM_PROMPT, API endpoint, max tokens
└── MainActivity.kt                # setContent { NavGraph() }
```

---

## 5. Coding conventions

- **Naming:** files PascalCase, functions camelCase, constants UPPER_SNAKE_CASE.
- **Composables:** mỗi composable 1 file, không nhét nhiều screen vào 1 file.
- **ViewModel:** chỉ expose `StateFlow`, không expose mutable state ra ngoài.
- **Coroutines:** luôn dùng `viewModelScope.launch` trong ViewModel.
  Repository dùng `suspend fun`, không tự launch coroutine.
- **Imports:** dùng Android Studio auto-import. Không dùng wildcard `import x.*`.
- **Type safety:** không dùng `!!` nếu tránh được. Dùng `?.let {}` hoặc `?: return`.
- **Error handling:** không silent catch. Khi catch exception, emit error state
  vào StateFlow để UI xử lý.
- **Comments:** comment "why", không comment "what".
- **Composable size:** > 100 dòng → tách component con.

---

## 6. DO / DON'T

### DO
- ✅ Dùng `StateFlow` cho UI state trong ViewModel (không `LiveData`).
- ✅ Dùng `LazyColumn` cho danh sách message (không Column + scroll).
- ✅ Inject API key qua `BuildConfig` từ `local.properties`.
- ✅ Wrap API call trong try-catch, emit `UiState.Error` khi lỗi.
- ✅ Dùng `rememberCoroutineScope` + `LaunchedEffect` đúng chỗ trong Compose.
- ✅ Dùng Material3 components (`FilledTextField`, `ElevatedCard`, etc.).
- ✅ Preview mọi Composable với `@Preview`.
- ✅ Khi thêm dependency, justify trong commit message.
- ✅ Sau khi hoàn thành task, tự động:
   1. Đổi `[ ]` → `[x]` cho task đó trong TASKS.md
   2. Append vào SESSION_NOTE.md: ngày, task ID, files đã làm, vấn đề gặp
   3. Nếu có gotcha mới → thêm vào AGENTS.md section 9

### DON'T
- ❌ KHÔNG hardcode API key trong source code — luôn dùng `local.properties`.
- ❌ KHÔNG dùng `GlobalScope` — luôn `viewModelScope` hoặc scope được quản lý.
- ❌ KHÔNG gọi API trực tiếp từ Composable — phải qua ViewModel.
- ❌ KHÔNG dùng `runBlocking` trong production code.
- ❌ KHÔNG dùng `findViewById` — app dùng 100% Compose, không có XML layout.
- ❌ KHÔNG commit `local.properties` hoặc `keystore` — đã có trong `.gitignore`.
- ❌ KHÔNG dùng deprecated `remember { mutableStateOf() }` pattern sai chỗ —
  đọc Compose lifecycle trước.
- ❌ KHÔNG thêm Google Maps / location feature mà không hỏi user trước.

---

## 7. Domain glossary

- **Session** — 1 cuộc chat về 1 chuyến đi (vd: "3 ngày Đà Nẵng"). KHÔNG phải
  auth session.
- **Message** — 1 tin nhắn trong session, có role "user" hoặc "assistant".
- **Lịch trình** — output AI format ngày/buổi. Là Message, không phải entity riêng.
- **System prompt** — instruction gửi kèm mỗi API call để AI "biết" mình là
  trợ lý du lịch. Xem `Constants.kt`.
- **Conversation history** — toàn bộ messages trong session, gửi lên DeepSeek
  để duy trì context multi-turn.

---

## 8. Architecture decisions (chốt rồi, không discuss lại)

- **AD-001:** Dùng DeepSeek API thay OpenAI — chi phí thấp hơn 10x, endpoint
  OpenAI-compatible nên code không đổi nếu cần swap.
- **AD-002:** Local-only, không có backend — giảm complexity, không cần auth,
  data private của user.
- **AD-003:** Không có streaming response trong v1 — đơn giản hơn,
  đủ cho MVP. Thêm sau nếu UX cần.
- **AD-004:** Hilt cho DI — standard Android, AI training data nhiều, ít bug.
- **AD-005:** Không làm Google Maps và GPS trong v1 — scope creep, làm sau.

---

## 9. Gotchas (cập nhật khi gặp bug mới)

- ⚠️ **Hilt + Compose Navigation:** `hiltViewModel()` phải import từ
  `androidx.hilt.navigation.compose`, không phải `hilt-android`. Thiếu dependency
  này sẽ compile error khó hiểu.
- ⚠️ **Room + Coroutines:** DAO suspend fun tự động chạy trên IO dispatcher.
  KHÔNG wrap thêm `withContext(Dispatchers.IO)` — sẽ deadlock.
- ⚠️ **OkHttp timeout:** DeepSeek có thể chậm > 10s cho response dài. Set
  `readTimeout(30, TimeUnit.SECONDS)` trong OkHttpClient.
- ⚠️ **Compose recomposition:** tránh tạo object mới trong Composable body
  (vd: `listOf()` inline) — sẽ trigger recompose liên tục. Dùng `remember {}`.
- ⚠️ **local.properties:** file này KHÔNG được commit. Người clone repo mới
  phải tự tạo và thêm `DEEPSEEK_API_KEY=sk-...`.

---

## 10. DeepSeek API — quick reference

```kotlin
// Endpoint
POST https://api.deepseek.com/chat/completions

// Auth header
Authorization: Bearer $DEEPSEEK_API_KEY

// Request body mẫu
{
  "model": "deepseek-chat",
  "messages": [
    {"role": "system", "content": "Bạn là trợ lý du lịch..."},
    {"role": "user",   "content": "3 ngày Đà Nẵng budget 3 triệu"},
    {"role": "assistant", "content": "..."},  // lịch sử chat
    {"role": "user",   "content": "Thêm 1 ngày nữa"}
  ],
  "temperature": 0.7,
  "max_tokens": 2048,
  "stream": false
}

// Response — lấy content ở:
response.choices[0].message.content
```

---

## 11. When in doubt

1. Check `ARCHITECTURE.md` — có decision trước chưa.
2. Check "Gotchas" section — đã gặp bug tương tự chưa.
3. Hỏi user TRƯỚC khi code, không sau.
4. Default: ít code hơn, ít abstraction hơn, ít dependency hơn.

---

## 12. Out of scope cho version hiện tại

KHÔNG đề xuất / tự ý implement:
- Google Maps tích hợp
- GPS / location-aware suggestions
- Backend server / Firebase
- User account / authentication
- Booking API (Agoda, Airbnb)
- Streaming response (API stream: true)
- iOS / cross-platform
- Push notification
- In-app purchases
- Sau mỗi task hoàn thành → append vào SESSION_NOTE.md ngay.

---

## 13. How to update file này

- Fix bug AI hay tạo lại → thêm vào "Gotchas" ngay.
- Chốt decision mới → thêm vào "Architecture decisions" với ID `AD-XXX`.
- Thêm tech mới → update "Tech stack" + lý do.
- Thay convention → update section 5 + commit rõ.

## 14. Quy trình mỗi phiên làm việc

### Khi bắt đầu session mới:
1. Đọc SESSION_NOTE.md để biết đã làm gì
2. Đọc TASKS.md để xác định task tiếp theo (task [ ] đầu tiên)
3. Đọc AGENTS.md section 9 (Gotchas) để tránh bug cũ

### Khi kết thúc task:
1. Tick [x] trong TASKS.md
2. Append SESSION_NOTE.md
3. Update AGENTS.md nếu có decision/gotcha mới
4. Commit: `git add . && git commit -m "feat: mô tả (TASK-XXX)"`

### Khi bắt đầu task mới trong cùng session:
- Đọc lại task description + acceptance criteria trước khi code
