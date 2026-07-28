---
description: Thi hành một issue — branch, code, test, PR
argument-hint: <số issue hoặc story id, ví dụ 12 hoặc M1-S02>
---

Thi hành: **$1**

## Bước 1 — chuẩn bị

```bash
gh issue view $1 --json number,title,body,comments,labels,milestone,state
```

- Chưa có comment plan → chạy `/plan $1` trước. Đừng code không có plan.
- Story có `needs` chưa xong → dừng, nói ra.
- Lấy `ac` từ `docs/STORIES.yml` (nguồn), không lấy từ body issue.

Branch:

```bash
gh issue develop <số> --checkout        # tạo branch có link về issue
```

Không làm trực tiếp trên `main`.

## Bước 2 — thi hành

Theo checklist trong comment plan, theo thứ tự. Sau mỗi bước lớn thì chạy test, đừng dồn
cuối mới chạy.

**Ba cơ chế — nếu issue có label `mechanism-*` thì đọc lại `DECISIONS.md § Ba cơ chế` trước
khi viết dòng đầu tiên:**

- `mechanism-1` dedupe: đẩy xuống unique index + `ON CONFLICT DO NOTHING`. Thấy mình định
  viết `if (!exists)` → sai, dừng lại.
- `mechanism-2` đa tenant: RLS + `SET LOCAL app.org_id`. **Endpoint mới không có test
  cross-tenant là chưa done** — không có ngoại lệ.
- `mechanism-3` offline: `client_event_id` + unique index ở server, **và** service worker.
  Outbox một mình là "worst of both worlds", đã bị loại.

Quy ước dự án:
- Business logic ở tầng ứng dụng, **không** trong DB — giữ đường thoát `pg_dump` sang
  Postgres region VN cho PDPL.
- Module gọi nhau qua interface, không chọc vào repository của module khác.
- Migration là immutable: sửa schema thì thêm file `V<n>__`, không sửa file cũ.
- Tài liệu viết tiếng Việt. `PRD.md` / `PLAN.md` / `GRILL-LOG.md` giữ tiếng Anh như đang có.

Chạy `mvn` với `dangerouslyDisableSandbox: true` ngay từ đầu — JVM/Maven vướng sandbox ở
nhiều mặt (Mockito inline self-attach, Testcontainers socket, git hook), lỗi hiện ra như
`Operation not permitted` chứ không phải lỗi code thật. Testcontainers thì thêm
`TESTCONTAINERS_RYUK_DISABLED=true`.

## Bước 3 — nếu phát hiện quyết định còn thiếu

Đừng quyết ngầm trong code. Ghi một mục mới vào `DECISIONS.md` (kèm lý do và cái đã loại),
rồi mới code theo nó. Nếu quyết định đó đổi scope milestone → dừng, hỏi người dùng.

## Bước 4 — xác minh trước khi nói xong

Đi qua **từng AC** trong `docs/STORIES.yml` và với mỗi cái nêu **bằng chứng**: lệnh đã chạy
và output của nó. Không có bằng chứng thì AC đó chưa xong — nói thẳng là chưa, đừng nói xong.

```bash
mvn test          # phải xanh
```

AC nào cần thiết bị thật (in poster, quét QR, airplane mode) thì nói rõ đây là việc người
dùng phải tự làm, và liệt kê ra — đừng đánh dấu xong hộ.

## Bước 5 — PR

```bash
git add -A && git commit    # message tiếng Việt, prefix feat/fix/docs/chore
gh pr create --title "..." --body "Closes #<số>

<tóm tắt>

## AC
- [x] ... — bằng chứng: ...
- [ ] ... — cần làm tay: ..."
```

Commit message kết thúc bằng:
```
Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
```
PR body kết thúc bằng:
```
🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

Sau khi merge, `Closes #N` tự đóng issue. Không cần sửa `docs/STORIES.yml` — trạng thái nằm
ở GitHub, YAML chỉ giữ định nghĩa.
