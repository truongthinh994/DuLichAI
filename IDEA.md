# IDEA — TravelAI Assistant

> Phase 0 deliverable. Mục tiêu: 1 trang, không hơn.

---

## One-line pitch

Cho **du khách Việt tự túc** đang mất thời gian search Google và hỏi group Facebook,
**TravelAI** là **app Android trợ lý du lịch AI** giúp họ **hỏi đáp và lên lịch trình
tự động bằng tiếng Việt**, khác với Google Maps / ChatGPT web ở chỗ **giao diện
tối ưu cho du lịch, trả lời cá nhân hóa theo ngân sách và sở thích, dùng được
ngay trên điện thoại**.

---

## Why now / Why me / Why this

- **Why now:** DeepSeek API rẻ + mạnh (cost ~$0.14/1M token), đủ để build AI
  assistant chi phí thấp cho cá nhân. Jetpack Compose đã mature, vibecoding
  Android trở nên khả thi hơn bao giờ hết.
- **Why me:** [BẠN ĐIỀN — bạn hay đi du lịch? Pain point gặp phải khi plan chuyến đi?]
- **Why this approach:** Android native → dễ tích hợp GPS và offline cache sau.
  App riêng → UX tốt hơn web, không phải context-switch khi đang đi đường.

---

## Target user (cụ thể)

- **Persona chính:** Du khách Việt 22–35 tuổi, đi tự túc 1–4 người, hay dùng
  điện thoại để plan và tra cứu trong chuyến đi.
- **Tình huống dùng app:** Đang ngồi plan chuyến đi cuối tuần, không biết
  nên đi đâu trước / ăn gì / mấy ngày cho vừa.
- **Đang dùng gì hiện tại:** Google Search, hỏi bạn bè, group Facebook du lịch,
  đôi khi ChatGPT web (nhưng không tối ưu cho du lịch).

---

## Outcome mong muốn (đo được)

- **Metric 1 (định lượng):** Sau 2 tuần, bản thân dùng app ít nhất 3 lần plan
  chuyến đi thật (không phải test).
- **Metric 2 (định tính):** Lịch trình AI gợi ý dùng được ≥ 70% mà không cần
  sửa nhiều.

---

## Kill criteria

- Nếu DeepSeek API response chậm > 5 giây trung bình → UX tệ, cần đổi model
  hoặc provider.
- Nếu sau 1 tuần build bản thân không muốn dùng app → bỏ, pivot ý tưởng.
- Nếu cost API vượt $5/tháng cho 1 user thông thường → không sustainable,
  cần optimize prompt.
- Nếu không build được walking skeleton trong hôm nay → re-evaluate scope.

---

## Risks và unknowns

1. **DeepSeek API reliability:** API mới, uptime và rate limit chưa rõ ràng
   → cần fallback hoặc retry logic.
2. **Android permissions:** Location permission flow phức tạp trên Android 13+
   → không làm ở v1, để sau.
3. **Vibecoding Android:** AI training data cho Jetpack Compose ít hơn React
   → verify kỹ hơn, có thể cần nhiều AI turns hơn ước tính.

---

## Next step

→ Phase 1: PRD.md (scope MUST/SHOULD/WON'T)
