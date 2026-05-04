# TravelAI Assistant

TravelAI là app Android trợ lý du lịch AI cho du khách Việt tự túc. Người dùng chat bằng tiếng Việt, app gọi DeepSeek để gợi ý địa điểm và lập lịch trình theo ngày/buổi. App chạy local-only, không có backend, không có account, không dùng Maps/GPS trong MVP.

## Tech stack

- Kotlin 2.0+
- Jetpack Compose + Material3
- MVVM: ViewModel + StateFlow + Repository
- Hilt dependency injection
- Retrofit + OkHttp + Gson cho DeepSeek API
- Room local database cho lịch sử chat
- Min SDK 26, target SDK 34

## Yêu cầu môi trường

- Android Studio Hedgehog hoặc mới hơn
- Android SDK đã cài platform/build tools phù hợp
- JDK/JBR đi kèm Android Studio
- Thiết bị Android thật hoặc emulator
- DeepSeek API key

Trên Windows, các lệnh bên dưới dùng PowerShell tại thư mục root của repo.

## Setup từ clone mới

1. Clone repo và mở bằng Android Studio.
2. Tạo file `local.properties` ở root project nếu chưa có.
3. Thêm Android SDK path và DeepSeek API key:

```properties
sdk.dir=C\:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
DEEPSEEK_API_KEY=<your_deepseek_api_key>
```

Nếu Android Studio đã mở project, IDE thường tự tạo dòng `sdk.dir`. Khi đó chỉ cần thêm `DEEPSEEK_API_KEY`.

Không commit `local.properties`. File này chứa secret và đã nằm trong `.gitignore`.

## Build debug APK

```powershell
.\gradlew.bat clean assembleDebug
```

APK debug nằm ở:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Cài lên thiết bị đang kết nối:

```powershell
adb install app/build/outputs/apk/debug/app-debug.apk
```

Cũng có thể bấm Run trực tiếp trong Android Studio.

## Build release APK

Release APK signed cần keystore local và signing properties trong `local.properties`. Keystore không được commit.

Ví dụ các key cần có:

```properties
RELEASE_STORE_FILE=release/travelai-release.jks
RELEASE_STORE_PASSWORD=<store_password>
RELEASE_KEY_ALIAS=travelai
RELEASE_KEY_PASSWORD=<key_password>
```

Build release:

```powershell
.\gradlew.bat assembleRelease
```

APK release nằm ở:

```text
app/build/outputs/apk/release/app-release.apk
```

Verify chữ ký nếu cần:

```powershell
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Cách dùng API key

API key chỉ đi qua `local.properties` và được inject vào `BuildConfig.DEEPSEEK_API_KEY` khi build. Không hardcode key trong Kotlin source.

Nếu thiếu hoặc để trống key, app sẽ hiện lỗi trong UI khi gửi chat thay vì crash.

## Manual smoke test checklist

Phần này chạy trên điện thoại thật sau khi cài APK.

1. Chat AI cơ bản
   - Mở app.
   - Nhập `Gợi ý 3 ngày Đà Nẵng`.
   - Xác nhận user bubble nằm bên phải, loading hiện khi chờ API, AI bubble nằm bên trái sau khi có response.

2. Multi-turn context
   - Sau response đầu tiên, nhập `thêm 1 ngày nữa`.
   - Xác nhận AI hiểu đang nói tiếp về lịch trình Đà Nẵng trước đó.

3. Persistence và History
   - Sau khi có chat, force close app rồi mở lại.
   - Xác nhận chat gần nhất vẫn còn.
   - Bấm `Lịch sử`, thấy session có title/ngày tạo, tap vào session mở đúng nội dung chat.

4. Offline/error/retry
   - Tắt mạng rồi gửi một message mới.
   - Xác nhận app hiện banner không có internet, không crash.
   - Bật mạng lại; với lỗi timeout/API nếu có, bấm `Thử lại` và xác nhận không tạo duplicate user bubble.

5. Copy/share lịch trình
   - Long-press vào message AI, paste sang app khác để xác nhận copy đúng nội dung.
   - Bấm `Chia sẻ`, xác nhận Android share sheet mở với toàn bộ nội dung chat hiện tại.

## Troubleshooting

- `JAVA_HOME is not set`: mở bằng Android Studio hoặc set `JAVA_HOME` tới JBR của Android Studio.
- Gradle/Kotlin daemon lỗi quyền trên Windows: chạy lại với Android Studio JBR và `GRADLE_USER_HOME` trong repo nếu cần.
- `sdk.dir` sai: mở Android Studio SDK Manager, kiểm tra path SDK rồi cập nhật `local.properties`.
- API trả 401/403: kiểm tra `DEEPSEEK_API_KEY` trong `local.properties`.
- API timeout: app có timeout UX 15 giây và OkHttp read timeout 30 giây; thử lại khi mạng ổn định.

## Scope hiện tại

MVP hiện có chat AI, multi-turn context, Room persistence, History screen, empty/loading/error states, retry, copy/share, debug/release build. Chưa có backend, account, Maps/GPS, streaming response hoặc booking integration.
