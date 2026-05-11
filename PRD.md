# PRD — TravelAI Assistant

> Phase 1 deliverable. Contract giữa bạn và AI agent: cái gì làm, cái gì không.

---

## 1. Product summary

TravelAI là app Android giúp du khách Việt tự túc hỏi đáp và lên lịch trình
du lịch bằng tiếng Việt thông qua AI (DeepSeek). App thay thế việc phải Google
từng thứ rồi ghép lại thủ công — user chỉ cần gõ "3 ngày Đà Nẵng, thích biển,
budget 3 triệu" và nhận lịch trình hoàn chỉnh.

---

## 2. User stories

1. As a du khách, I want to chat bằng tiếng Việt với AI về địa điểm du lịch,
   so that tôi không cần Google từng thứ một.
2. As a du khách, I want to nhập điểm đến + số ngày + budget,
   so that AI tự lên lịch trình hoàn chỉnh cho tôi.
3. As a du khách, I want to xem lịch trình theo từng ngày rõ ràng (sáng/chiều/tối),
   so that tôi dễ follow khi đang đi.
4. As a du khách, I want to hỏi follow-up ("thay quán ăn này bằng quán khác đi"),
   so that tôi điều chỉnh lịch trình mà không cần tạo lại từ đầu.
5. As a du khách, I want to xem lịch sử các chat / lịch trình đã tạo,
   so that tôi có thể quay lại xem chuyến đi cũ.

---

## 3. Features — phân loại MoSCoW

### MUST (walking skeleton hôm nay)
- [x] Màn hình chat — user gõ câu hỏi, AI trả lời (DeepSeek API)
- [x] System prompt tối ưu cho du lịch (AI "hiểu" context du lịch Việt Nam)
- [x] Hiển thị loading state khi đang gọi API
- [x] Error handling cơ bản (không có mạng, API lỗi)

### SHOULD (ngày 2–3)
- [ ] Lịch trình tự động — user nhập [điểm đến + ngày + budget] → AI trả
  lịch trình theo format ngày/buổi
- [ ] Lưu lịch sử chat vào local storage (Room DB)
- [ ] Màn hình danh sách các chuyến đi đã tạo

### COULD (nếu rảnh)
- [ ] Share lịch trình (copy text / share sheet)
- [ ] Dark mode
- [ ] Gợi ý câu hỏi mẫu cho user mới (onboarding chips)

### WON'T (version này) — ít nhất 5 cái

- [ ] KHÔNG có tích hợp Google Maps trong v1 — complexity cao, làm sau
- [ ] KHÔNG có location-aware / GPS — permission flow phức tạp Android 13+
- [ ] KHÔNG có đặt phòng / vé (Booking, Agoda API) — scope quá lớn
- [ ] KHÔNG có account / đăng nhập — local-only trong MVP
- [ ] KHÔNG có offline mode — cần mạng để gọi DeepSeek API
- [ ] KHÔNG có push notification
- [ ] KHÔNG publish Play Store trong MVP — test internal trước

---

## 4. Non-goals

App này KHÔNG phải là:
- KHÔNG phải booking platform (không bán vé, không đặt phòng)
- KHÔNG phải social app (không share với bạn bè trong app)
- KHÔNG phải offline map / navigation
- KHÔNG phải aggregator giá (không compare giá hotel/flight)

---

## 5. Success metrics

| Metric | Loại | Target | Cách đo |
|---|---|---|---|
| Walking skeleton chạy | định lượng | Hôm nay | App boot + chat được |
| Bản thân dùng thật | định lượng | ≥ 3 lần / 2 tuần | Đếm tay |
| Lịch trình dùng được | định tính | ≥ 70% không sửa | Self-assess |
| API response time | định lượng | < 5s p90 | Log thủ công |

---

## 6. Constraints

- **Time budget:** Hôm nay — walking skeleton. Tuần này — SHOULD features.
- **Money budget:** DeepSeek API free tier / pay-as-you-go, tối đa $5/tháng
- **Tech constraints:** Android (Kotlin + Jetpack Compose), min SDK 26 (Android 8)
- **Privacy:** Không lưu data lên server — local-only. Không track user.

---

## 7. Open questions

1. DeepSeek API có hỗ trợ streaming response không? (cho UX tốt hơn — hiện
   chữ từng từ như ChatGPT)
2. Format lịch trình tốt nhất để hiển thị trong chat bubble là gì?
   (plain text, markdown, hay custom card?)
3. Nên dùng Room DB hay DataStore cho lưu lịch sử chat?

---

## Next step

→ Phase 2: ARCHITECTURE.md
