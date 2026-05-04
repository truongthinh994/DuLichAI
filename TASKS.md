# TASKS — TravelAI Assistant

> Phase 3 deliverable. Vertical slices, mỗi task ≤ 1–2h vibecoding.

---

## Task list

### Milestone 1: Walking Skeleton (Hôm nay)

- [x] **TASK-001** — App scaffold + màn hình chat rỗng + build được
  - **Why:** Có cái gì đó chạy được trước khi thêm logic.
  - **What:** Tạo Android project (Kotlin + Compose), cấu hình Hilt, màn hình
    ChatScreen rỗng với TopAppBar và ô input ở dưới.
  - **Touches:** `MainActivity.kt`, `ChatScreen.kt`, `Theme.kt`,
    `build.gradle.kts`, `AndroidManifest.xml`
  - **Acceptance:**
    - ✓ App build không lỗi (`./gradlew assembleDebug` clean)
    - ✓ Màn hình chat hiện ra với AppBar "TravelAI" và input field ở dưới
    - ✓ Chạy được trên emulator hoặc điện thoại thật
  - **Out of scope:** Chưa gọi API, chưa có logic gì
  - **Estimated AI turns:** 3–5

- [x] **TASK-002** — Kết nối DeepSeek API + hiện response trong chat
  - **Why:** Core value của app — AI phải trả lời được.
  - **What:** Setup Retrofit + DeepSeekApi interface, ChatViewModel gọi API,
    messages list hiện trong LazyColumn với chat bubble user/assistant.
  - **Touches:** `DeepSeekApi.kt`, `DeepSeekModels.kt`, `ApiClient.kt`,
    `ChatViewModel.kt`, `ChatScreen.kt`, `local.properties`
  - **Acceptance:**
    - ✓ User gõ "Gợi ý 3 ngày Đà Nẵng" → gửi → AI trả lời trong vài giây
    - ✓ Bubble user (phải) và bubble AI (trái) phân biệt rõ
    - ✓ Loading indicator hiện khi đang chờ API
    - ✓ Nếu không có mạng → hiện error message, không crash
    - ✓ API key lấy từ `local.properties`, không hardcode trong code
  - **Out of scope:** Lưu DB, lịch sử chat, streaming
  - **Depends:** TASK-001
  - **Estimated AI turns:** 5–8

### Milestone 2: Core Features

- [x] **TASK-003** — System prompt + conversation context (multi-turn)
  - **Why:** Hiện tại mỗi message là độc lập — AI không nhớ context trước.
  - **What:** Gửi toàn bộ conversation history kèm system prompt du lịch
    vào mỗi API call.
  - **Touches:** `ChatViewModel.kt`, `DeepSeekModels.kt`, `Constants.kt`
  - **Acceptance:**
    - ✓ User hỏi "3 ngày Đà Nẵng" → AI trả → user hỏi tiếp "thêm 1 ngày nữa"
      → AI hiểu context từ câu trước
    - ✓ System prompt du lịch được gửi kèm mỗi request (role: system)
    - ✓ Tổng tokens không vượt 4096 (trim nếu cần)
  - **Depends:** TASK-002
  - **Estimated AI turns:** 3–5

- [x] **TASK-004** — Room DB: lưu và load lịch sử chat
  - **Why:** User tắt app rồi mở lại cần thấy lại lịch trình đã tạo.
  - **What:** Setup Room DB, ChatSession + ChatMessage entities, DAO,
    Repository. ViewModel save message sau mỗi turn, load khi mở app.
  - **Touches:** `AppDatabase.kt`, `ChatDao.kt`, `ChatSession.kt`,
    `ChatMessage.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `AppModule.kt`
  - **Acceptance:**
    - ✓ Chat sau khi tắt app mở lại vẫn còn
    - ✓ Không crash khi DB trống (fresh install)
    - ✓ Migration không cần (version 1, clean slate)
  - **Depends:** TASK-002
  - **Estimated AI turns:** 5–8

- [x] **TASK-005** — Màn hình lịch sử chuyến đi
  - **Why:** User muốn xem lại các chuyến đã plan.
  - **What:** HistoryScreen hiện danh sách ChatSession, tap vào mở lại
    cuộc chat đó. Navigation giữa ChatScreen ↔ HistoryScreen.
  - **Touches:** `HistoryScreen.kt`, `HistoryViewModel.kt`, `NavGraph.kt`
  - **Acceptance:**
    - ✓ Từ ChatScreen có nút/icon mở HistoryScreen
    - ✓ List hiện title và ngày tạo mỗi session
    - ✓ Tap vào session → mở ChatScreen với đúng lịch sử
    - ✓ Empty state khi chưa có session nào
  - **Depends:** TASK-004
  - **Estimated AI turns:** 5–8

### Milestone 3: Polish

- [x] **TASK-006** — Empty states + Loading states đúng chỗ
  - **Touches:** `ChatScreen.kt`, `HistoryScreen.kt`
  - **Acceptance:**
    - ✓ Chat mới mở: hiện placeholder "Hỏi tôi về chuyến đi của bạn..."
    - ✓ History rỗng: hiện illustration + text gợi ý tạo chuyến đầu tiên
    - ✓ Loading: shimmer hoặc CircularProgressIndicator, không blank screen

- [x] **TASK-007** — Error boundaries + Retry
  - **Touches:** `ChatViewModel.kt`, `ChatScreen.kt`
  - **Acceptance:**
    - ✓ API timeout (>15s) → hiện "Không phản hồi, thử lại?" + Retry button
    - ✓ HTTP 4xx/5xx → hiện message lỗi cụ thể, không crash
    - ✓ No internet → detect offline, hiện banner

- [x] **TASK-008** — Share lịch trình
  - **Touches:** `ChatScreen.kt`
  - **Acceptance:**
    - ✓ Long-press vào message AI → copy text
    - ✓ Share button → mở Android share sheet với nội dung chat

### Milestone 4: Ship

- [x] **TASK-009** — Production config + API key management
  - **Acceptance:**
    - ✓ `local.properties` có trong `.gitignore`
    - ✓ `BuildConfig.DEEPSEEK_API_KEY` inject đúng cách
    - ✓ Release build signed được

- [x] **TASK-010** — Smoke test + README
  - **Acceptance:**
    - ✓ Clone repo → follow README → build thành công trong < 10 phút
    - ✓ 5 luồng chính test thủ công pass trên điện thoại thật
    - ✓ README có: setup steps, how to add API key, how to build

---

## Backlog (chưa schedule)

Feature cho version sau, KHÔNG làm trong MVP:
- Google Maps tích hợp (hiện địa điểm trên bản đồ)
- Location-aware gợi ý (dùng GPS realtime)
- Streaming response (hiện chữ từng từ)
- Booking integration (Agoda/Booking API)
- Account / sync cloud
- Widget màn hình chính
- iOS version

---

## Cancelled / deferred

| Task | Lý do |
|---|---|
| Google Maps v1 | Complexity quá cao, không đủ thời gian hôm nay |
| GPS / location-aware | Android 13+ permission flow phức tạp |
