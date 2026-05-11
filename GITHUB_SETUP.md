# GitHub Setup

Hướng dẫn này dành cho việc đưa project local `d:\Android Studio\TravelAI_2k-main` lên repo:

`https://github.com/nguyenhuunghia10t1-creator/TravelAI_2k`

## Trước khi bắt đầu

- Cài Git for Windows và mở lại terminal nếu lệnh `git` chưa chạy được.
- Đảm bảo bạn có quyền push vào repo GitHub đó.
- Không commit `local.properties` hay release keystore thật.

## Flow A: Repo GitHub đang trống

```powershell
cd "d:\Android Studio\TravelAI_2k-main"
git init
git branch -M main
git remote add origin https://github.com/nguyenhuunghia10t1-creator/TravelAI_2k.git
git add .
git commit -m "feat: initial TravelAI import"
git push -u origin main
```

## Flow B: Repo GitHub đã có README hoặc file mẫu

```powershell
cd "d:\Android Studio\TravelAI_2k-main"
git init
git branch -M main
git remote add origin https://github.com/funnyz99ak/DulichAI
git add .
git commit -m "feat: initial TravelAI import"
git pull origin main --allow-unrelated-histories
git push -u origin main
```

## Nếu gặp conflict

Các file hay conflict nhất là:

- `README.md`
- `.gitignore`

Ưu tiên giữ source code Android hiện tại, sau đó gộp lại phần tài liệu theo ý bạn muốn hiển thị trên GitHub.

## Sau khi push

Kiểm tra trên GitHub xem repo đã có các phần sau chưa:

- Source code Android trong `app/`
- `README.md`
- `IDEA.md`
- `PRD.md`
- `ARCHITECTURE.md`
- `AGENTS.md`
- `docs/PROJECT_DOCS.md`
- PDF gốc nếu bạn vẫn muốn giữ làm tài liệu tham chiếu

## Gợi ý cấu trúc hiển thị đẹp trên GitHub

1. `README.md` làm trang vào chính
2. `docs/PROJECT_DOCS.md` làm index tài liệu
3. Giữ PDF như tài liệu gốc, không dùng làm entrypoint chính
