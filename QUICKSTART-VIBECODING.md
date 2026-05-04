# VIBECODING QUICK START — TravelAI

> Copy prompt bên dưới, paste vào Claude Code để bắt đầu TASK-001.

---

## Prompt P-006 cho TASK-001 (paste vào Claude Code ngay)

```
Đọc các file sau trong project:
- AGENTS.md
- PRD.md (section 3 — MUST features)
- TASKS.md (chỉ TASK-001)
- ARCHITECTURE.md (section 1, 4, 6)

KHÔNG viết code chưa. Trả lời:

1. Hiểu của bạn về TASK-001 (1 đoạn ngắn, paraphrase).
2. Files sẽ tạo (list cụ thể theo folder structure trong AGENTS.md).
3. Approach kỹ thuật (3–5 bullet).
4. Risks / edge case bạn thấy với Kotlin + Jetpack Compose + Hilt setup.
5. Acceptance criteria — bạn sẽ verify từng cái thế nào?

Tôi sẽ confirm trước khi bạn code.
```

---

## Sau khi confirm plan — Prompt P-007 (Execute)

```
Plan ổn. Tiến hành TASK-001.

Khi xong:
- Show diff / danh sách file đã tạo (summary, không full code).
- Note bất kỳ deviation nào khỏi plan đã agreed.
- Liệt kê commands tôi cần chạy để verify:
  ./gradlew assembleDebug
  [adb install hoặc run từ Android Studio]

KHÔNG tự ý:
- Thêm Google Maps
- Thêm Room DB (đó là TASK-004)
- Thêm bất kỳ feature nào ngoài scaffold + màn hình chat rỗng

Nếu cần làm gì ngoài plan → STOP và hỏi tôi.
```

---

## Checklist trước khi bắt đầu vibecoding

- [ ] `local.properties` đã tạo với `DEEPSEEK_API_KEY=sk-...`
- [ ] Android Studio mở và emulator/thiết bị kết nối
- [ ] VSCode mở folder project
- [ ] Claude Code active trong VSCode
- [ ] AGENTS.md đã paste vào đầu session (hoặc Claude Code đọc tự động)
- [ ] Git đã init: `git init && git add . && git commit -m "chore: initial docs"`

---

## Thứ tự task hôm nay

```
TASK-001 → [verify acceptance] → commit
TASK-002 → [verify acceptance] → commit
TASK-003 → [verify acceptance] → commit (nếu còn thời gian)
```

KHÔNG sang task kế khi task trước chưa tick hết acceptance criteria.

Sau khi xong, append vào SESSION_NOTE.md:
- Ngày, TASK ID
- Đã làm gì (2-3 dòng)
- Files đã tạo/sửa
- Vấn đề gặp (nếu có)
- Decision mới (nếu có) → cũng copy vào AGENTS.md "Gotchas"
