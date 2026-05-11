# Hướng dẫn Đóng góp (Contributing Guidelines)

Cảm ơn bạn đã quan tâm và đóng góp vào dự án TravelAI. Để đảm bảo quá trình làm việc nhóm mượt mà và hạn chế conflict, vui lòng tuân thủ các quy tắc dưới đây.

## 1. Branching Workflow (Git Flow-like)

Dự án áp dụng mô hình nhánh như sau:
- **`main`**: Nhánh production, chứa code đã release ổn định. TUYỆT ĐỐI KHÔNG commit trực tiếp vào nhánh này.
- **`develop`**: Nhánh tích hợp chính cho môi trường phát triển. Các tính năng mới sẽ được ghép (merge) vào đây.
- **`feature/*`**: Nhánh để phát triển tính năng riêng biệt. **Luôn luôn tạo từ `develop`**. (Ví dụ: `feature/login`, `feature/chat-ai`)
- **`hotfix/*`**: Nhánh để sửa lỗi khẩn cấp. **Luôn tạo từ `main`**. Sau khi sửa xong phải merge vào cả `main` và `develop`. (Ví dụ: `hotfix/crash-login`)

## 2. Quy tắc Commit (Conventional Commits)

Để lịch sử Git rõ ràng và dễ tự động hóa, vui lòng sử dụng một trong các tiền tố sau cho commit message:
- **`feat:`** Thêm tính năng mới.
- **`fix:`** Sửa một lỗi/bug.
- **`refactor:`** Tái cấu trúc code (không thay đổi tính năng hay sửa lỗi).
- **`chore:`** Thay đổi về cấu hình, build tool, CI/CD.
- **`docs:`** Cập nhật tài liệu (README, CONTRIBUTING, comments...).

Ví dụ: `feat: Thêm tính năng đăng nhập bằng Google`

## 3. Quy trình làm việc (Workflow Bắt buộc)

1. Cập nhật nhánh develop: `git checkout develop && git pull origin develop`
2. Tạo nhánh tính năng mới: `git checkout -b feature/ten-tinh-nang`
3. Code và commit (nhớ test cẩn thận). Không commit API keys hoặc file rác.
4. Format code gọn gàng trước khi commit.
5. Push nhánh lên GitHub: `git push origin feature/ten-tinh-nang`
6. Tạo Pull Request (PR) từ nhánh feature vào nhánh `develop`.
7. Trải qua CI (GitHub Actions) để kiểm tra build và lint. Sau đó code review và merge.

## 4. Kiểm tra trước khi Push

- Chạy lệnh build: `./gradlew assembleDebug`
- Chạy lệnh lint: `./gradlew lintDebug`
- Đảm bảo app chạy ổn định và đồng bộ logic với các phần khác.
