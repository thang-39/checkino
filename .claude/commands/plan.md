---
description: Lập plan thi hành cho một issue, comment lên issue
argument-hint: <số issue hoặc story id, ví dụ 12 hoặc M1-S02>
allowed-tools: Bash(gh issue view:*), Bash(gh issue comment:*), Bash(ruby:*), Bash(rg:*), Read, Write, Edit
---

Lập plan thi hành cho: **$1**

## Bước 1 — đọc issue và story

```bash
gh issue view $1 --json number,title,body,labels,milestone,state
```

Nếu `$1` là story id (dạng `M1-S02`) thì tra số issue trong `docs/STORIES.yml` trước.

Lấy từ `docs/STORIES.yml` (đây là nguồn, không phải body issue): `ac`, `refs`, `needs`,
`estimate`, `labels`.

## Bước 2 — đọc `refs` THẬT

Với mỗi ref dạng `"<file> § <mục>"`: mở file, đọc đúng mục đó. Dùng
`rg --color=never --no-heading -n '<tên mục>' <file>` để định vị rồi Read với offset.

**Không lập plan từ trí nhớ hay từ CLAUDE.md.** CLAUDE.md chỉ là bản tóm tắt; `DECISIONS.md`
mới là nguồn và nó thắng mọi tranh chấp.

Nếu ref nhắc tới một trong Ba cơ chế → đọc nguyên mục đó trong `DECISIONS.md § Ba cơ chế`,
kể cả đoạn code mẫu. Ba cơ chế này là ba chỗ dễ làm sai nhất của dự án.

## Bước 3 — kiểm tra story còn đúng không

Trước khi lập plan, kiểm ba thứ:

- `needs` đã xong hết chưa? Chưa xong → nói ra, đừng lập plan cho story bị chặn.
- Có AC nào chứa `QUYẾT ĐỊNH CẦN CHỐT:` không? Có → **quyết định đó phải nằm trong plan như
  bước đầu tiên**, và plan phải có bước ghi kết quả vào `DECISIONS.md`.
- AC còn khớp với docs không? Docs đã đổi từ lúc viết story → sửa story trong
  `docs/STORIES.yml` trước (dùng skill `write-story`), rồi mới lập plan.

## Bước 4 — viết plan

Plan là **checklist thi hành**, không phải bài luận. Mỗi mục:

- Một hành động cụ thể: file nào, tạo hay sửa, làm gì.
- Ánh xạ được về ít nhất một AC. AC nào không có bước nào phủ → thiếu bước.

Cấu trúc:

```markdown
## Plan — [M1-S02] Migration V1

**Ràng buộc bắt buộc** (từ refs, đọc rồi mới viết)
- D8: UNIQUE (org_id, phone_normalized) — không phải unique toàn cục
- D10: audit_log dựng ở M1, không phải grow-later

**Bước**
- [ ] 1. `backend/src/main/resources/db/migration/V1__core_schema.sql` — bảng org, staff_user, ...
- [ ] 2. ... (AC #3)
- [ ] 3. Test: `mvn test -Dtest=SchemaMigrationTest` (AC #6)

**Cách kiểm** — lệnh chạy thật, không phải "kiểm tra bằng mắt"
```bash
docker compose up -d && mvn flyway:migrate && mvn test
```

**Bẫy** — chép từ AC dạng chữ HOA, một dòng mỗi cái
```

Nếu plan dài quá ~10 bước: story quá to. Nói ra và đề xuất tách trong `docs/STORIES.yml`
thay vì cố lập plan cho nó.

## Bước 5 — comment lên issue

```bash
gh issue comment <số> --body-file <file plan>
```

Rồi báo: `Xong. Làm: /work <số>`
