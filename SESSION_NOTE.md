# 2026-05-04 — TASK-001 TravelAI scaffold

- Implemented Android Compose walking skeleton for TravelAI with package `com.travelai`.
- Added minimal Hilt setup: `TravelAiApplication`, `@AndroidEntryPoint` `MainActivity`, Hilt Gradle plugin/dependency via KSP for Kotlin 2.0.
- Added empty `ChatScreen` with Material3 `TopAppBar("TravelAI")`, bottom `MessageInput`, `WindowCompat.setDecorFitsSystemWindows(window, false)`, and `imePadding()`.
- Set `minSdk = 26`, `targetSdk = 34`, namespace/applicationId `com.travelai`.
- Verified `clean assembleDebug` passed. Scope check found no DeepSeek/Retrofit/Room/API key/Maps/GPS/backend/streaming code in `app/src/main`.

# 2026-05-04 — TASK-002 DeepSeek chat

- Ticked TASK-001 and TASK-002 in `TASKS.md`.
- Added DeepSeek API wiring with Retrofit, OkHttp, Gson converter, Hilt provider, and `BuildConfig.DEEPSEEK_API_KEY` loaded from `local.properties`.
- Added `ChatRepository`, `ChatViewModel` with `StateFlow`, user/assistant chat messages, loading state, and API/network error state.
- Updated Compose chat UI to use `hiltViewModel()`, `collectAsStateWithLifecycle()`, `LazyColumn`, user/assistant bubbles, stateless `MessageInput`, and loading/error rendering.
- Files created: `DeepSeekApi.kt`, `DeepSeekModels.kt`, `ApiClient.kt`, `ChatRepository.kt`, `AppModule.kt`, `ChatViewModel.kt`, `ChatBubble.kt`.
- Files edited: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, `ChatScreen.kt`, `MessageInput.kt`, `TASKS.md`.
- Verified `clean assembleDebug` passed and `app/build/outputs/apk/debug/app-debug.apk` exists.
- Static scope check found no hardcoded `sk-...` key, Room, Maps/GPS, Firebase, streaming `true`, `GlobalScope`, `runBlocking`, or `findViewById` in app source/config.
- Manual emulator/device smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 — TASK-003 System prompt + conversation context

- Added `Constants.kt` with `SYSTEM_PROMPT`, DeepSeek model/max token constants, and approximate context character budget.
- Updated DeepSeek request defaults to use constants and keep `stream = false`.
- Changed `ChatRepository.sendMessage()` to accept a prepared `List<DeepSeekMessage>` payload instead of creating a single-message request.
- Updated `ChatViewModel` to send `system` prompt plus in-memory conversation history on every request, mapping user/assistant roles and trimming oldest history when the approximate context budget is exceeded.
- Files created: `Constants.kt`.
- Files edited: `DeepSeekModels.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `TASKS.md`.
- Verified Gradle `clean assembleDebug` completed successfully; the wrapper client timed out after 3 minutes, but the Gradle daemon log reported `BUILD SUCCESSFUL in 3m 45s` and `app-debug.apk` exists.
- Static scope check confirmed `SYSTEM_PROMPT`, role `system`, role `user`, role `assistant`, constants usage, `stream = false`, no Room, no Maps/GPS, no Firebase/backend, no hardcoded `sk-...`, no `GlobalScope`, no `runBlocking`, and no `findViewById`.
- Manual multi-turn smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 — TASK-004 Room chat persistence

- Added Room 2.6.1 dependencies with KSP compiler.
- Added Room DB layer: `AppDatabase`, `ChatDao`, `ChatSessionEntity`, and `ChatMessageEntity` with a session-to-message foreign key and `travelai.db`.
- Updated Hilt `AppModule` to provide `AppDatabase` and `ChatDao`.
- Updated `ChatRepository` with latest-session load, session creation, and message persistence while keeping the existing DeepSeek send API.
- Updated `ChatViewModel` to load the latest session on startup, create a session for the first user message, save user messages before API calls, and save assistant responses after successful API calls.
- Files created: `AppDatabase.kt`, `ChatDao.kt`, `ChatSession.kt`, `ChatMessage.kt`.
- Files edited: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AppModule.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `TASKS.md`.
- Initial sandbox build failed because Gradle wrapper/dependency download was blocked by network sandbox permissions.
- Verified `clean assembleDebug` outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`; daemon log reported `BUILD SUCCESSFUL in 5m 40s`, 43 tasks executed, and `app-debug.apk` exists.
- Static checks confirmed Room annotations/providers/dependencies and no `withContext(Dispatchers.IO)`, `GlobalScope`, `runBlocking`, Maps/GPS, Firebase/backend, streaming `true`, or `findViewById`.
- Manual persistence smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 — TASK-005 Trip history screen

- Added Compose Navigation with routes `chat`, `chat?sessionId={sessionId}`, and `history`.
- Added `HistoryScreen` and `HistoryViewModel` to list stored trip sessions with title and created date, plus an empty state when no sessions exist.
- Updated `ChatScreen` with a `Lịch sử` action that opens History.
- Updated `MainActivity` to render `NavGraph()` inside `TravelAITheme`.
- Extended `ChatDao` and `ChatRepository` with session list and load-by-id APIs without changing Room schema/version.
- Updated `ChatViewModel` to read `sessionId` from `SavedStateHandle`, load the selected session when present, and keep saving new messages to the selected session.
- Files created: `NavGraph.kt`, `HistoryScreen.kt`, `HistoryViewModel.kt`.
- Files edited: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `MainActivity.kt`, `ChatScreen.kt`, `ChatViewModel.kt`, `ChatDao.kt`, `ChatRepository.kt`, `TASKS.md`.
- Initial sandbox build failed because Gradle wrapper/dependency download was blocked by network sandbox permissions.
- Verified `clean assembleDebug` outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`; command exited successfully and `app-debug.apk` exists.
- Static checks confirmed Navigation/History markers and no Room schema version change, migration, `withContext(Dispatchers.IO)`, `GlobalScope`, `runBlocking`, Maps/GPS, Firebase/backend, streaming `true`, or `findViewById`.
- Manual History smoke test was not run because `adb devices` showed no attached devices.
- Gradle daemon was stopped after verification; `.gradle-home/` cleanup was left in place because recursive delete was blocked by tool policy.

# 2026-05-04 - TASK-006 Empty states + loading states

- Added Chat empty placeholder text `Hoi toi ve chuyen di cua ban...` in UI source as Vietnamese text with accents.
- Added assistant-side loading indicator for pending DeepSeek responses so Chat is not blank while waiting.
- Added History loading state with `CircularProgressIndicator` and loading text.
- Added History empty state with lightweight Compose illustration, title, and first-trip guidance text.
- Added empty/loading previews for Chat and loading preview for History.
- Files edited: `ChatScreen.kt`, `HistoryScreen.kt`, `TASKS.md`.
- Initial sandbox build failed because Gradle/Android plugin could not write to the sandbox Android settings directory.
- Verified `clean assembleDebug` outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`; command exited successfully and `app-debug.apk` exists.
- Static checks confirmed required empty/loading strings and `CircularProgressIndicator`, with no Maps/GPS, Firebase/backend, streaming `true`, `GlobalScope`, `runBlocking`, `findViewById`, or new dependency.
- Manual UI smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 - TASK-007 Error boundaries + Retry

- Added chat-level offline detection with `ConnectivityManager`/`NetworkCapabilities` and `ACCESS_NETWORK_STATE`.
- Added app-level 15s timeout with `withTimeout(15_000)` around DeepSeek response calls while keeping OkHttp `readTimeout(30s)`.
- Added retry state with `PendingRetry` and `retryLastMessage()` so timeout retry calls DeepSeek again without duplicating the saved user message.
- Added HTTP-specific error messages for 401, 403, 429, and 5xx.
- Updated Chat UI with offline banner, retry-capable error card, and retry/offline previews.
- Files edited: `ChatViewModel.kt`, `ChatScreen.kt`, `AndroidManifest.xml`, `TASKS.md`.
- Verified `clean assembleDebug` with Android Studio JBR and repo-local `GRADLE_USER_HOME`; build succeeded and `app-debug.apk` exists.
- Static checks confirmed `withTimeout`, retry markers, network-state permission, offline/timeout texts, and unchanged OkHttp `readTimeout(30, TimeUnit.SECONDS)`.
- Static scope check found no Maps/GPS, Firebase/backend, streaming `true`, `GlobalScope`, `runBlocking`, `findViewById`, `withContext(Dispatchers.IO)`, or new dependency.
- Manual offline/timeout/retry smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 - TASK-008 Share lịch trình

- Added Chat AppBar `Chia sẻ` action that opens Android share sheet with the current chat messages formatted by role.
- Added long-press copy for assistant messages only, using Compose clipboard and a short copy confirmation toast.
- Updated `ChatBubble` with optional long-press handling via `combinedClickable` while preserving existing user/assistant bubble layout.
- Files edited: `ChatScreen.kt`, `ChatBubble.kt`, `TASKS.md`.
- Initial sandbox build failed because Android Gradle plugin could not write to the sandbox Android settings directory.
- Verified `clean assembleDebug` outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`; command exited successfully and `app-debug.apk` exists.
- Static checks confirmed `LocalClipboardManager`, `AnnotatedString`, `combinedClickable`, `ACTION_SEND`, `Intent.createChooser`, and `Chia sẻ`.
- Static scope check found no Maps/GPS, Firebase/backend, streaming `true`, `GlobalScope`, `runBlocking`, `findViewById`, `withContext(Dispatchers.IO)`, or new dependency.
- Manual copy/share smoke test was not run because `adb devices` showed no attached devices.

# 2026-05-04 - TASK-009 Production config + API key management

- Cleaned `.gitignore` to keep `local.properties` ignored and add local signing artifact ignores: `*.jks`, `*.keystore`, `/release/`.
- Added release signing config in `app/build.gradle.kts`, reading `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` from `local.properties`.
- Kept `BuildConfig.DEEPSEEK_API_KEY` injected from `local.properties`; no API key value is hardcoded or logged.
- Created local ignored keystore artifact `release/travelai-release.jks` and added signing property keys to `local.properties` without printing secret values.
- Files edited: `.gitignore`, `app/build.gradle.kts`, `TASKS.md`.
- Local ignored artifacts/properties updated: `release/travelai-release.jks`, `local.properties`.
- Initial sandbox debug build failed because the sandbox could not access the Android SDK/Kotlin daemon paths under `C:\Users\nguye\AppData\Local`; reran Gradle outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`.
- Verified `clean assembleDebug` passed.
- Verified `clean assembleRelease` passed and `app/build/outputs/apk/release/app-release.apk` exists.
- Verified release APK signature with `apksigner verify --print-certs`; signer certificate subject is `CN=TravelAI, OU=TravelAI, O=TravelAI, L=Da Nang, ST=Da Nang, C=VN`.
- Static checks confirmed `local.properties` and `release/travelai-release.jks` are ignored, signing config markers exist, and no hardcoded `sk-...`, Maps/GPS, Firebase/backend, streaming `true`, `GlobalScope`, `runBlocking`, `findViewById`, or `withContext(Dispatchers.IO)` were added.

# 2026-05-04 - TASK-010 README + smoke test handoff

- Created `README.md` with project overview, tech stack, prerequisites, setup from clone, `local.properties` API key instructions, debug/release build commands, install command, release signing note, troubleshooting, and a 5-flow manual smoke checklist.
- Files created: `README.md`.
- Files edited: `TASKS.md`.
- Verified README contains setup/API/build/manual-test markers: `local.properties`, `DEEPSEEK_API_KEY`, `assembleDebug`, `assembleRelease`, and the 5 manual smoke flows.
- Static scope check found no hardcoded `sk-...`, Maps/GPS, Firebase/backend, streaming `true`, `GlobalScope`, `runBlocking`, `findViewById`, or `withContext(Dispatchers.IO)` in README/app/Gradle scope.
- Initial sandbox build failed because the sandbox could not access Kotlin daemon temp files and Android SDK `android.jar`; reran Gradle outside the sandbox with Android Studio JBR and repo-local `GRADLE_USER_HOME`.
- Verified `clean assembleDebug` passed and `app/build/outputs/apk/debug/app-debug.apk` exists.
- Manual phone smoke test was not run by Codex; README now includes the checklist for the user to run on a real device.

# 2026-05-08 - TASK-011 Foundation privacy and Room schema baseline

- Added TravelAI V2 tasks `TASK-011` through `TASK-018` to `TASKS.md`.
- Fixed the stale instrumented test package/assertion from `com.midterm.myapplication6` to `com.travelai`.
- Disabled app backup by default and added explicit backup/data-extraction exclusions for database/shared preferences.
- Enabled Room schema export and generated `app/schemas/com.travelai.data.db.AppDatabase/1.json`.
- Updated `AGENTS.md` with AD-006: local Room/chat data is not backed up to cloud by default.
- Files edited: `TASKS.md`, `AGENTS.md`, `app/build.gradle.kts`, `AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml`, `AppDatabase.kt`, `ExampleInstrumentedTest.kt`.
- Files created: `app/schemas/com.travelai.data.db.AppDatabase/1.json`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-012 Trip planner form

- Added `planner` route as the app start destination with navigation to Chat and History.
- Added `TripPlannerScreen` and `TripPlannerViewModel` for destination, day count, budget, people count, travel style, transport, and notes.
- Added validation for required destination, positive day count, and positive people count; optional fields can stay blank.
- Planner submit now opens Chat with a structured prompt prefilled, but it does not auto-call DeepSeek or save a trip profile yet; that remains `TASK-013`.
- Added a Chat AppBar action to return to the planner.
- Added unit coverage for planner validation and prompt creation.
- Files edited: `TASKS.md`, `ChatScreen.kt`, `ChatViewModel.kt`, `NavGraph.kt`.
- Files created: `TripPlannerScreen.kt`, `TripPlannerViewModel.kt`, `TripPlannerViewModelTest.kt`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.
- Gradle emitted the existing `LocalClipboardManager` deprecation warning in `ChatScreen.kt`; lint still passed.

# 2026-05-08 - TASK-013 Trip profile prompt integration

- Added `TripProfile` domain model with initial prompt/session title/profile context helpers.
- Added `TripProfileEntity`, Room database version 2, migration `MIGRATION_1_2`, and exported schema `app/schemas/com.travelai.data.db.AppDatabase/2.json`.
- Extended `ChatDao` and `ChatRepository` so a planner-created session stores and reloads its trip profile locally.
- Updated planner flow so form submission creates a real chat session first, then opens Chat with `sessionId` and `autoStart=true`.
- Updated `ChatViewModel` to auto-send the first trip prompt for a new profile session and include profile context in later DeepSeek calls for multi-turn chat.
- Replaced the old prompt-draft unit test with `TripProfileTest` covering title, initial prompt, and profile context formatting.
- Files edited: `TASKS.md`, `AGENTS.md`, `AppDatabase.kt`, `ChatDao.kt`, `ChatRepository.kt`, `AppModule.kt`, `ChatViewModel.kt`, `NavGraph.kt`, `TripPlannerScreen.kt`, `TripPlannerViewModel.kt`.
- Files created: `TripProfile.kt`, `TripProfileEntity.kt`, `TripProfileTest.kt`, `app/schemas/com.travelai.data.db.AppDatabase/2.json`.
- Files removed: `TripPlannerViewModelTest.kt`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-014 Structured itinerary parser and storage

- Added `TripPlanSnapshot` domain model for raw assistant response plus parsed itinerary days/periods.
- Added `ItineraryParser` for `Ngày X`, `Sáng`, `Chiều`, and `Tối`, including fallback behavior when output cannot be parsed.
- Added `TripPlanSnapshotEntity`, Room database version 3, migration `MIGRATION_2_3`, and exported schema `app/schemas/com.travelai.data.db.AppDatabase/3.json`.
- Extended `ChatDao` and `ChatRepository` to save/load itinerary snapshots per session while preserving raw response text.
- Updated `ChatViewModel` to save a snapshot after assistant responses; the initial planner response keeps raw fallback even if parser fails, later non-itinerary chat does not overwrite an existing parsed snapshot.
- Added `ItineraryParserTest` covering a 3-day Da Nang itinerary, multiline period bodies, and raw-text fallback.
- Files edited: `TASKS.md`, `AGENTS.md`, `AppDatabase.kt`, `ChatDao.kt`, `ChatRepository.kt`, `AppModule.kt`, `ChatViewModel.kt`.
- Files created: `TripPlanSnapshot.kt`, `ItineraryParser.kt`, `TripPlanSnapshotEntity.kt`, `ItineraryParserTest.kt`, `app/schemas/com.travelai.data.db.AppDatabase/3.json`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-015 Itinerary UI

- Added `itinerary/{sessionId}` route with `ItineraryScreen` and `ItineraryViewModel`.
- Itinerary UI loads the current session snapshot and shows parsed tabs by day with `Sáng`, `Chiều`, and `Tối` cards.
- Added raw-response fallback so sessions without parsed itinerary still show the original assistant itinerary text or latest assistant message.
- Added navigation into itinerary from Chat and History; itinerary can also return to the related chat session.
- Updated `ChatUiState` to expose the current `sessionId` after session load/create so Chat can open the correct itinerary.
- Files edited: `TASKS.md`, `AGENTS.md`, `SESSION_NOTE.md`, `ChatScreen.kt`, `ChatViewModel.kt`, `HistoryScreen.kt`, `NavGraph.kt`.
- Files created: `ItineraryScreen.kt`, `ItineraryViewModel.kt`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-016 Budget planner

- Added `BudgetItem` domain model with budget categories, VND amount parsing, formatting, and total helpers.
- Added `BudgetItemEntity`, Room database version 4, migration `MIGRATION_3_4`, and exported schema `app/schemas/com.travelai.data.db.AppDatabase/4.json`.
- Extended `ChatDao` and `ChatRepository` with budget CRUD scoped by `sessionId`, plus budget loading through `StoredChatSession`.
- Added `BudgetSection` inside `ItineraryScreen` so users can add, edit, delete, and review trip budget items with an auto-updating total.
- Updated `ItineraryViewModel` with budget form state, validation, save/edit/delete actions, and reload from Room so data persists after app restart.
- Added `BudgetItemTest` for amount parsing, total calculation, and VND display suffix.
- Files edited: `TASKS.md`, `AGENTS.md`, `SESSION_NOTE.md`, `AppDatabase.kt`, `ChatDao.kt`, `ChatRepository.kt`, `AppModule.kt`, `ItineraryScreen.kt`, `ItineraryViewModel.kt`.
- Files created: `BudgetItem.kt`, `BudgetItemEntity.kt`, `BudgetSection.kt`, `BudgetItemTest.kt`, `app/schemas/com.travelai.data.db.AppDatabase/4.json`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-017 Travel checklist

- Added `ChecklistItem` domain model with completed-count helper.
- Added `ChecklistItemEntity`, Room database version 5, migration `MIGRATION_4_5`, and exported schema `app/schemas/com.travelai.data.db.AppDatabase/5.json`.
- Extended `ChatDao` and `ChatRepository` with checklist CRUD scoped by `sessionId`, plus checklist loading through `StoredChatSession`.
- Added `ChecklistSection` inside `ItineraryScreen` so users can add, delete, and tick travel preparation items next to the itinerary/budget tools.
- Updated `ItineraryViewModel` with checklist draft state, validation, add/toggle/delete actions, and Room reload so checkbox state persists after app restart.
- Updated `AGENTS.md` folder map to reflect Room version 5 and the new checklist files.
- Added `ChecklistItemTest` for completed checklist count behavior.
- Files edited: `TASKS.md`, `AGENTS.md`, `SESSION_NOTE.md`, `AppDatabase.kt`, `ChatDao.kt`, `ChatRepository.kt`, `AppModule.kt`, `ItineraryScreen.kt`, `ItineraryViewModel.kt`.
- Files created: `ChecklistItem.kt`, `ChecklistItemEntity.kt`, `ChecklistSection.kt`, `ChecklistItemTest.kt`, `app/schemas/com.travelai.data.db.AppDatabase/5.json`.
- Initial sandbox Gradle build failed because the sandbox could not access `C:\Users\nguye\AppData\Local\Android\Sdk\platforms\android-36\android.jar` and Kotlin daemon temp files; reran Gradle outside the sandbox with Android Studio JBR and repo-local Gradle/Android homes.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` passed using Android Studio JBR.

# 2026-05-08 - TASK-018 Trip Library and polished export

- Upgraded History into Trip Library with title search, pinned sessions first, rename dialog, delete confirmation, and session export action.
- Added `isPinned` to `ChatSessionEntity`, Room database version 6, migration `MIGRATION_5_6`, and exported schema `app/schemas/com.travelai.data.db.AppDatabase/6.json`.
- Extended `ChatDao` and `ChatRepository` with rename/delete/pin session APIs and clean trip export loading through `StoredChatSession`.
- Added `TripExport` formatter so sharing prioritizes parsed itinerary, then raw itinerary/fallback assistant text, and includes trip profile, budget total/items, and checklist without `Bạn:` / `TravelAI:` chat-role wrappers.
- Updated Chat share to use the same clean export path instead of raw bubble concatenation.
- Added `TripShare.kt` Android share helper and `TripExportTest` coverage for parsed itinerary export and raw fallback.
- Updated `AGENTS.md` folder map/glossary to reflect Room version 6, Trip Library, trip export, and the Windows lint-cache lock gotcha.
- Files edited: `TASKS.md`, `AGENTS.md`, `SESSION_NOTE.md`, `AppDatabase.kt`, `ChatDao.kt`, `ChatSession.kt`, `ChatRepository.kt`, `AppModule.kt`, `ChatScreen.kt`, `ChatViewModel.kt`, `HistoryScreen.kt`, `HistoryViewModel.kt`.
- Files created: `TripExport.kt`, `TripShare.kt`, `TripExportTest.kt`, `app/schemas/com.travelai.data.db.AppDatabase/6.json`.
- Verification passed with Android Studio JBR: `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug`.
- Smoke note: `adb` from Android SDK showed no attached devices, so the planner -> chat -> itinerary -> budget/checklist -> library flow was verified by navigation/source path plus build/test/lint, not on a physical device.
- Issue encountered: `:app:lintDebug` initially failed because another Gradle daemon held `app/build/intermediates/lint-cache`; stopping both global and repo-local Gradle daemons cleared the lock.

# 2026-05-08 - TASK-019 Post-V2 polish và DX hardening

- Reviewed dự án hậu V2 (Claude + codex) và chốt 4 nhóm fix: quick wins, data integrity, developer DX, UX polish. Không bao gồm backend proxy API key, R8/proguard, hay nâng `targetSdk` (đẩy sang V3 backlog).
- OkHttp giờ có `connectTimeout(15s)`, `writeTimeout(15s)`, `callTimeout(45s)`, giữ `readTimeout(30s)` để cứng hóa khi DNS/handshake treo.
- TripPlanner `validationError()` thêm `MAX_DAYS = 30` và `MAX_PEOPLE = 50` để chặn input bất hợp lý gửi prompt khổng lồ tới DeepSeek.
- Xóa test sample lệch ở `com.midterm.myapplication6` (cả unit test 2+2 và instrumented test) và tạo lại `app/src/androidTest/java/com/travelai/ExampleInstrumentedTest.kt` để khớp `applicationId`.
- Đổi `ChatDao` từ `interface` → `abstract class` và thêm 8 hàm `@Transaction` wrapper bundle insert/update/delete với `updateSessionUpdatedAt`. `ChatRepository` gọi các hàm này thay vì 2 DAO call rời rạc, đảm bảo `chat_sessions.updatedAt` không bao giờ lệch dữ liệu con khi app bị kill giữa chừng.
- README cập nhật scope: tách V1 (MVP chat) và V2 (planner, itinerary parser, budget, checklist, Trip Library), thêm flow smoke test V2, sửa note OkHttp timeout cho khớp config mới.
- Thêm dependency `androidx.compose.material:material-icons-core` (Compose BOM pin version) vào `gradle/libs.versions.toml` + `app/build.gradle.kts`. Compose BOM chỉ manage version, KHÔNG kéo theo artifact icons — phải khai báo riêng nếu muốn dùng `Icons.Filled.*`.
- ChatScreen TopAppBar đổi 4 TextButton ("Chia sẻ" / "Tạo chuyến" / "Lịch trình" / "Lịch sử") thành 4 IconButton (`Share`, `Add`, `AutoMirrored.List`, `Menu`) kèm `contentDescription` cho TalkBack.
- HistoryScreen back button đổi sang `Icons.AutoMirrored.Filled.ArrowBack` IconButton; HistorySessionRow gom 4 action phụ ("Chia sẻ", "Ghim/Bỏ ghim", "Đổi tên", "Xóa") vào overflow `DropdownMenu` mở từ icon `MoreVert`.
- MessageInput bỏ `singleLine = true`, dùng `maxLines = 5` (auto-grow rồi scroll); bỏ `ImeAction.Send` + keyboard action gửi vì multi-line cần Enter để xuống dòng. User vẫn dùng nút "Gửi" bên cạnh.
- Files edited: `TASKS.md`, `AGENTS.md`, `SESSION_NOTE.md`, `README.md`, `ApiClient.kt`, `TripPlannerViewModel.kt`, `ChatDao.kt`, `ChatRepository.kt`, `ChatScreen.kt`, `HistoryScreen.kt`, `MessageInput.kt`, `gradle/libs.versions.toml`, `app/build.gradle.kts`.
- Files removed: `app/src/test/java/com/midterm/myapplication6/ExampleUnitTest.kt`, `app/src/androidTest/java/com/midterm/myapplication6/ExampleInstrumentedTest.kt`.
- Files created: `app/src/androidTest/java/com/travelai/ExampleInstrumentedTest.kt`.
- Verified `:app:assembleDebug`, `:app:testDebugUnitTest`, và `:app:lintDebug` pass với Android Studio JBR; có pre-existing warning `LocalClipboardManager` deprecated trong `ChatScreen.kt`, không liên quan đợt polish này.
- Manual smoke test trên thiết bị thật chưa chạy vì `adb devices` không có thiết bị nối; checklist trong plan để user chạy thủ công khi có máy.

# 2026-05-11 - TASK-020 GitHub handoff docs + PDF doc index

- Added GitHub publishing guidance for this local project, including separate flows for an empty remote repo and a remote repo that already contains starter files.
- Added `GITHUB_SETUP.md` with step-by-step commands and conflict guidance for pushing `TravelAI_2k-main` to `https://github.com/nguyenhuunghia10t1-creator/TravelAI_2k`.
- Added `docs/PROJECT_DOCS.md` to map the original PDF to the markdown documentation already present in the repo so GitHub readers can navigate without opening the PDF first.
- Updated `README.md` with GitHub handoff and documentation-index sections.
- Files edited: `README.md`, `TASKS.md`, `SESSION_NOTE.md`.
- Files created: `GITHUB_SETUP.md`, `docs/PROJECT_DOCS.md`.
- Verification: static review only; Git is not available in the current shell and this workspace does not yet contain a `.git` directory, so no init/commit/push command was executed from Codex.

# 2026-05-11 - GitHub branch handoff to truongthinh994/DuLichAI

- Updated `README.md` and `GITHUB_SETUP.md` so the documented GitHub remote now points to `https://github.com/truongthinh994/DuLichAI.git`.
- Intentionally left local Gradle/cache folders (`android/`, `caches/`, `daemon/`, `kotlin-profile/`, `native/`, `wrapper/`) untracked so they are not included in the publish branch.
- Next step from this workspace is to create a dedicated publish branch and push that branch to the target GitHub repo without touching remote `main`.

# 2026-05-11 - Thiết lập Kiến trúc Git Branching & CI/CD Workflow
- Đã sửa lỗi cấu hình `compileSdk { version = release(36) }` thành `compileSdk = 34` trong `app/build.gradle.kts` để có thể build thành công.
- Tự động hóa CI/CD với GitHub Actions (`.github/workflows/ci.yml`) để chạy lint và assembleDebug.
- Bổ sung `PULL_REQUEST_TEMPLATE.md` và `CONTRIBUTING.md` hướng dẫn chi tiết quy tắc commit.
- Cập nhật `.gitignore` để bỏ qua thư mục cache của Gradle user home (`caches/`, `daemon/`, `wrapper/`, v.v.).
- Khởi tạo kiến trúc Git Branching (Git Flow): `main`, `develop`, các nhánh tính năng (`feature/login`, `feature/chat-ai`, `feature/maps`, `feature/offline-roomdb`) và nhánh sửa lỗi (`hotfix/crash-login`, `hotfix/api-timeout`).
- Tiến hành checkout, push tất cả nhánh lên remote repo (`origin`).
- Đã kiểm tra build với Android Studio JDK 17 (JBR) qua lệnh `assembleDebug` do Local JDK 25 gặp lỗi tương thích với Gradle.
