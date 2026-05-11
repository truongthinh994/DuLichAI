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

## Đưa project này lên GitHub

Repo local hiện tại có thể chưa được khởi tạo Git. Nếu bạn muốn đưa toàn bộ project này lên repo GitHub của bạn, dùng một trong hai flow dưới đây.

### Trường hợp 1: Repo GitHub đang trống

```powershell
cd "d:\Android Studio\TravelAI_2k-main"
git init
git branch -M main
git remote add origin https://github.com/truongthinh994/DuLichAI.git
git add .
git commit -m "feat: initial TravelAI import"
git push -u origin main
```

### Trường hợp 2: Repo GitHub đã có sẵn README hoặc file khác

```powershell
cd "d:\Android Studio\TravelAI_2k-main"
git init
git branch -M main
git remote add origin https://github.com/truongthinh994/DuLichAI.git
git add .
git commit -m "feat: initial TravelAI import"
git pull origin main --allow-unrelated-histories
git push -u origin main
```

Nếu `git pull` báo conflict, giữ lại source code app hiện tại, rồi merge thủ công các file tài liệu như `README.md`.

### Không nên đẩy lên GitHub

- `local.properties`
- Keystore thật dùng để ký release
- API key DeepSeek thật

Xem thêm hướng dẫn chi tiết trong [GITHUB_SETUP.md](GITHUB_SETUP.md).

## Tài liệu dự án và PDF gốc

Repo đang có file PDF gốc [DuLichAI_Engineering_Core (1).pdf](<DuLichAI_Engineering_Core (1).pdf>) và bộ tài liệu markdown đã tách ra để dễ đọc trên GitHub:

- [IDEA.md](IDEA.md)
- [PRD.md](PRD.md)
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [AGENTS.md](AGENTS.md)
- [docs/PROJECT_DOCS.md](docs/PROJECT_DOCS.md)

Nếu mục tiêu là làm repo nhìn rõ ràng hơn trên GitHub, nên ưu tiên đọc các file `.md` này thay vì chỉ giữ PDF.

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

6. Trip Planner V2 flow
   - Bấm `Tạo chuyến` từ ChatScreen, nhập điểm đến, số ngày, ngân sách, số người, phong cách, phương tiện, ghi chú; bấm tạo.
   - Xác nhận AI trả lịch trình theo trip profile vừa nhập, session title ưu tiên điểm đến/số ngày.
   - Mở `Lịch trình`, kiểm tra parser tách Ngày/Sáng/Chiều/Tối; nếu parser không nhận diện được thì fallback raw text.
   - Trong ItineraryScreen, thêm/sửa/xóa budget item và checklist item; force close app rồi mở lại để xác nhận lưu qua restart, tổng ngân sách cập nhật đúng.
   - Vào Trip Library (`Lịch sử`), thử search theo title, rename, ghim/bỏ ghim, xóa, và Chia sẻ — nội dung share ưu tiên itinerary đã parse, không phải chat thô.

## Troubleshooting

- `JAVA_HOME is not set`: mở bằng Android Studio hoặc set `JAVA_HOME` tới JBR của Android Studio.
- Gradle/Kotlin daemon lỗi quyền trên Windows: chạy lại với Android Studio JBR và `GRADLE_USER_HOME` trong repo nếu cần.
- `sdk.dir` sai: mở Android Studio SDK Manager, kiểm tra path SDK rồi cập nhật `local.properties`.
- API trả 401/403: kiểm tra `DEEPSEEK_API_KEY` trong `local.properties`.
- API timeout: app có timeout UX 15 giây và OkHttp connect/write 15s, read 30s, call 45s; thử lại khi mạng ổn định.

## Scope hiện tại

### V1 — MVP chat
Chat AI với DeepSeek, multi-turn context, Room persistence, History screen, empty/loading/error states, retry, copy/share, debug/release build.

### V2 — Trip planning
- **Trip Planner form:** nhập điểm đến, số ngày, ngân sách, số người, phong cách, phương tiện, ghi chú để tạo session có cấu trúc.
- **Itinerary parser + UI:** tự parse response của AI thành lịch trình theo Ngày / Sáng / Chiều / Tối; có raw fallback khi parser không bắt được.
- **Budget planner:** CRUD budget item theo session (ăn uống, di chuyển, vé tham quan, khách sạn, phát sinh) + tổng dự kiến.
- **Travel checklist:** CRUD checklist item, lưu trạng thái checkbox qua restart.
- **Trip Library:** thay thế History — search theo title, rename, delete (CASCADE), pin/favorite, share/export ưu tiên itinerary đã parse.

### Chưa có
Backend / API key proxy, account, Google Maps/GPS, streaming response, booking integration, dark mode, localization (strings hiện hardcode tiếng Việt).
