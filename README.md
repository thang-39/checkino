# Checkino

SaaS điểm danh QR + quản lý thẻ hội viên cho trung tâm nhỏ ở Việt Nam
(gym, võ thuật, yoga, trung tâm ngoại ngữ, gia sư). Một người xây, part-time.

**Trạng thái:** chưa có code. Backlog đã dựng — 5 epic, 58 story.

---

## Bắt đầu từ đâu

```
/next        →  nên làm story nào tiếp, kèm lý do
/plan 12     →  lập plan thi hành cho issue #12, comment lên issue
/work 12     →  branch, code, test, PR
/status      →  bảng tiến độ theo epic
/sync-issues →  đẩy docs/STORIES.yml lên GitHub issues
```

Không cần nhớ file nào ở đâu. Gõ `/next`.

---

## Tài liệu nằm ở đâu, và cái nào thắng

Năm tầng. Mỗi tầng có một việc, không tầng nào chép nội dung của tầng khác.

| Tầng | File | Chứa gì | Thắng khi lệch |
|---|---|---|---|
| **Tại sao** | [`DECISIONS.md`](DECISIONS.md) | D1–D11 + Ba cơ chế dễ làm sai + SQL mẫu | **Luôn thắng** |
| **Sản phẩm làm gì** | [`PRD.md`](PRD.md) | F1–F11, NFR, tier, success metric | Hành vi sản phẩm |
| **Stack & hình dáng** | [`PLAN.md`](PLAN.md) | stack, schema, milestone M0–M4, DoD, danh sách cắt | Ý định milestone |
| **Việc phải làm** | [`docs/STORIES.yml`](docs/STORIES.yml) | epic/story: acceptance criteria, thứ tự phụ thuộc | **Chia việc** |
| **Trạng thái** | [GitHub issues](../../issues) | open/closed, comment, PR | Không phải nguồn |

Quy tắc giữ cho nó không rối:

- **Chi tiết mức việc chỉ nằm ở `docs/STORIES.yml`.** Không thêm checklist vào PRD/PLAN nữa.
- **Story TRỎ tới docs, không sao lại.** Story chỉ tự sinh ra hai thứ chưa có ở đâu:
  acceptance criteria và thứ tự phụ thuộc.
- **Issue là bản render một chiều** từ `STORIES.yml`. Sửa body issue trên GitHub sẽ bị ghi đè.
- Thêm/sửa story → dùng skill `write-story` (`.claude/skills/write-story/`), rồi `/sync-issues`.
- Phát hiện quyết định còn thiếu khi đang code → **ghi vào `DECISIONS.md`**, không quyết ngầm.

[`docs/archive/`](docs/archive) là tài liệu đã hết vai trò, giữ lại làm lịch sử. Không đọc để
làm việc: `GRILL-LOG.md` (tự vấn adversarial trước khi có PRD) và `plan-v2-rewrite.md`
(kế hoạch viết lại PRD/PLAN lên v2, đã đóng).

---

## Ba chỗ dễ làm sai nhất

Chi tiết + code mẫu ở [`DECISIONS.md § Ba cơ chế kỹ thuật đã chốt`](DECISIONS.md).

1. **Dedupe check-in** — đẩy xuống `UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)`
   + `ON CONFLICT DO NOTHING`. **Không** `if (!exists)` ở tầng app.
2. **Cô lập đa tenant** — Postgres RLS + `SET LOCAL app.org_id`, **và** bộ test cross-tenant
   tự động cho mọi endpoint. Viết test trước, không phải sau. *Endpoint mới không có test
   cross-tenant là chưa done.*
3. **Roster offline** — `client_event_id` + unique index ở server, **và** service worker.
   Thiếu service worker thì F5 lúc offline là app chết — mà trên mobile F5 tự xảy ra.

---

## Mốc thời gian — con số thật

`PLAN.md § 4` nêu ~8 tuần tới hết M3. Backlog cộng lại: **43 ngày-người part-time** cho
M0→M3, tức **17–21 tuần** ở 15–20h/tuần. Chênh lệch này là thật, không phải lỗi ước lượng
từng story — `PLAN.md` chỉ ước lượng hai hạng mục rồi suy ra tổng.

Hai lựa chọn: nhận mốc ~4–5 tháng tới hết M3, hoặc dùng danh sách cắt ở `PLAN.md § 6`
(① Sheet mirror ② GPS layer) — nhưng cắt cả hai chỉ giảm ~3 ngày, nên chủ yếu là phải nhận mốc.

## Ràng buộc xuyên suốt

- Free tier có **chi phí biến đổi = 0đ**. Không có ngoại lệ.
- Zalo không bao giờ nằm trong luồng lõi — nó là tính năng Pro, chặn bởi GPKD của khách (D1).
- Chữ **"PWA" chỉ áp cho `/staff`**. `/q` là trang web thường (không manifest, không service
  worker), `/admin` chỉ cần manifest cho icon.
- Business logic ở tầng ứng dụng, không trong DB — giữ đường thoát `pg_dump` sang Postgres
  region VN cho PDPL.
