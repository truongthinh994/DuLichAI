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
