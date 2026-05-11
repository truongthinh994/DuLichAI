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

### Milestone 5: TravelAI V2 - Trip planning

- [x] **TASK-011** — Foundation: test package, privacy backup, Room schema baseline
  - **Why:** Trước khi thêm nhiều bảng mới, cần sửa các rủi ro nền tảng đã phát hiện.
  - **What:** Sửa package test cũ sang `com.travelai`, chặn backup cloud mặc định cho dữ liệu chat local, bật Room schema export và chuẩn bị migration baseline.
  - **Touches:** `ExampleInstrumentedTest.kt`, `AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml`, `AppDatabase.kt`, `app/build.gradle.kts`
  - **Acceptance:**
    - [x] Instrumented test assert đúng `com.travelai`
    - [x] Room schema được export ra thư mục repo-local
    - [x] Room/local chat DB không bị cloud backup mặc định
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-012** — Trip planner form
  - **Why:** User cần nhập thông tin chuyến đi có cấu trúc thay vì chỉ chat tự do.
  - **What:** Thêm `TripPlannerScreen` cho điểm đến, số ngày, ngân sách, số người, phong cách, phương tiện, ghi chú.
  - **Touches:** `TripPlannerScreen.kt`, `TripPlannerViewModel.kt`, `NavGraph.kt`, `ChatScreen.kt`
  - **Acceptance:**
    - [x] Có route `planner`
    - [x] User nhập form và bấm tạo lịch trình
    - [x] Form validate các trường quan trọng, không crash khi bỏ trống optional
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-013** — Trip profile prompt integration
  - **Why:** DeepSeek cần nhận thông tin chuyến đi có cấu trúc để lập lịch trình sát nhu cầu.
  - **What:** Tích hợp trip profile vào prompt, tạo session từ form, lưu trip profile local, vẫn chat tiếp multi-turn.
  - **Touches:** `ChatViewModel.kt`, `ChatRepository.kt`, `ChatDao.kt`, `TripProfile.kt`, `Constants.kt`
  - **Acceptance:**
    - [x] User tạo chuyến đi từ form -> AI trả lịch trình theo đúng thông tin
    - [x] Session title ưu tiên điểm đến / số ngày từ trip profile
    - [x] Mở lại session vẫn chat tiếp được
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-014** — Structured itinerary parser and storage
  - **Why:** Lịch trình nên được hiển thị theo ngày/buổi, không chỉ là text chat.
  - **What:** Thêm parser cho `Ngày X`, `Sáng`, `Chiều`, `Tối`, lưu raw response và parsed snapshot local.
  - **Touches:** `ItineraryParser.kt`, `TripPlanSnapshot.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `AppDatabase.kt`
  - **Acceptance:**
    - [x] Parse được lịch trình 3 ngày Đà Nẵng theo ngày/buổi
    - [x] Lưu snapshot và load lại theo session
    - [x] Nếu parser không nhận diện được thì fallback raw text
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-015** — Itinerary UI
  - **Why:** UI cần làm lịch trình dễ đọc và khác chatbot thường.
  - **What:** Thêm route `itinerary/{sessionId}` và UI card/tab theo ngày, kèm fallback chat text.
  - **Touches:** `ItineraryScreen.kt`, `ItineraryViewModel.kt`, `NavGraph.kt`, `HistoryScreen.kt`, `ChatScreen.kt`
  - **Acceptance:**
    - [x] Từ chat/history mở được itinerary của session
    - [x] Hiển thị Ngày/Sáng/Chiều/Tối rõ ràng
    - [x] Session chưa có parsed itinerary vẫn hiển thị raw response
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-016** — Budget planner
  - **Why:** Ngân sách là yếu tố đặc trưng của trợ lý du lịch tự túc.
  - **What:** Thêm budget item local cho ăn uống, di chuyển, vé tham quan, khách sạn, phát sinh và tổng dự kiến.
  - **Touches:** `BudgetItem.kt`, `BudgetSection.kt`, `ChatRepository.kt`, `ItineraryScreen.kt`
  - **Acceptance:**
    - [x] Budget gắn với session
    - [x] User thêm/sửa/xóa budget item
    - [x] Tổng chi phí tự cập nhật và lưu qua restart
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-017** — Travel checklist
  - **Why:** Checklist giúp app hữu ích sau khi đã lập lịch trình.
  - **What:** Thêm checklist chuẩn bị du lịch local-only, có checkbox lưu trạng thái theo session.
  - **Touches:** `ChecklistItem.kt`, `ChecklistSection.kt`, `ChatRepository.kt`, `ItineraryScreen.kt`
  - **Acceptance:**
    - [x] Checklist gắn với session
    - [x] User thêm/xóa/tick item
    - [x] Trạng thái checkbox lưu qua restart
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-018** — Trip Library and polished export
  - **Why:** History cần trở thành nơi quản lý chuyến đi thật sự.
  - **What:** Nâng History thành Trip Library với search, rename, delete, pin/favorite, và share/export lịch trình sạch hơn.
  - **Touches:** `HistoryScreen.kt`, `HistoryViewModel.kt`, `ChatRepository.kt`, `ChatDao.kt`, `ChatScreen.kt`
  - **Acceptance:**
    - [x] Search chuyến đi theo title
    - [x] Rename/delete/pin session
    - [x] Share/export bỏ lớp chat thô và ưu tiên itinerary
    - [x] Smoke test luồng planner -> chat -> itinerary -> budget/checklist -> library pass
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-019** — Post-V2 polish và DX hardening
  - **Why:** Sau review toàn dự án (Claude + codex), còn 4 nhóm điểm yếu nhỏ cần fix trước khi dùng cá nhân. Mục tiêu: polish, KHÔNG publish Play Store.
  - **What:** Hardening OkHttp/timeouts, bound input planner, dọn test package cũ, bọc multi-write repo trong `@Transaction`, cập nhật README V2, thêm dep `material-icons-core` và polish UX (IconButton + contentDescription, overflow menu, multi-line input).
  - **Touches:** `ApiClient.kt`, `TripPlannerViewModel.kt`, `ChatDao.kt`, `ChatRepository.kt`, `ExampleInstrumentedTest.kt` (move package), `README.md`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `ChatScreen.kt`, `HistoryScreen.kt`, `MessageInput.kt`
  - **Acceptance:**
    - [x] OkHttp có connect/write/call/read timeout đầy đủ
    - [x] Planner chặn `days > 30` và `people > 50`
    - [x] Test sample không còn nằm ở package `com.midterm.myapplication6`
    - [x] Multi-write repo (saveMessage, budget/checklist add/update/delete, createTripSession) đi qua `@Transaction` wrapper trên DAO
    - [x] README có section V2 features và smoke test V2 flow
    - [x] Dep `material-icons-core` được khai báo, ChatScreen TopAppBar và HistoryScreen dùng IconButton + contentDescription, HistorySessionRow gom action vào overflow menu
    - [x] MessageInput cho phép nhập multi-line (maxLines=5)
    - [x] `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` pass

- [x] **TASK-020** — GitHub handoff docs + PDF doc index
  - **Why:** Repo cần dễ đưa lên GitHub và dễ đọc mà không phụ thuộc việc mở PDF cục bộ.
  - **What:** Thêm hướng dẫn push lên GitHub cho repo local chưa có `.git`, và tạo index liên kết giữa PDF gốc với bộ tài liệu markdown hiện có.
  - **Touches:** `README.md`, `GITHUB_SETUP.md`, `docs/PROJECT_DOCS.md`
  - **Acceptance:**
    - [x] README có hướng dẫn push repo lên GitHub theo 2 trường hợp: repo trống và repo đã có file
    - [x] Có file tài liệu riêng cho GitHub setup
    - [x] Có file index chỉ đường giữa PDF gốc và tài liệu markdown trong repo

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
