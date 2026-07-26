# CheckinHub (tên tạm)

SaaS điểm danh QR + quản lý thẻ hội viên cho các trung tâm nhỏ ở Việt Nam
(gym, võ thuật, yoga, trung tâm ngoại ngữ, gia sư). Một người xây, part-time.

## Trạng thái

**Chưa có code.** Project hiện chỉ có tài liệu. Đợt viết lại PRD/PLAN lên v2 **đã xong** —
việc tiếp theo là khởi tạo monorepo (Bước 6 trong [`docs/plan-v2-rewrite.md`](docs/plan-v2-rewrite.md)).

## Đọc trước khi làm bất cứ việc gì

**[`DECISIONS.md`](DECISIONS.md)** — sổ quyết định kiến trúc & sản phẩm, kèm lý do và số liệu
kiểm chứng. Đây là nguồn sự thật. `PRD.md` (v2.2), `PLAN.md` (v2.1) và `GRILL-LOG.md` đã đồng bộ
**đủ D1–D10**. Khi hai bên mâu thuẫn thì `DECISIONS.md` thắng.

[`docs/plan-v2-rewrite.md`](docs/plan-v2-rewrite.md) — kế hoạch thi hành việc cập nhật đó,
kèm bảng tiến độ. Cập nhật bảng tiến độ sau mỗi bước.

## Mười quyết định đã chốt (tóm tắt — chi tiết ở `DECISIONS.md`)

- **D1** — Zalo OA/ZNS là tính năng **gói Pro**, không thuộc lõi v1. Free tier chạy hoàn toàn
  không cần Zalo. Lý do: xác thực OA bắt buộc có GPKD của khách hàng, giết mục tiêu onboarding
  tự phục vụ dưới 10 phút.
- **D2** — v1 **không gửi OTP** cho hội viên. Chủ import danh sách → hội viên nhập SĐT →
  khớp thì bind device token ngay. ZNS không có free tier (300đ/tin xác thực).
- **D3** — Tách hai nhu cầu auth: chủ/nhân viên dùng **email magic link**; hội viên dùng
  **device token** (cookie httpOnly, TTL 1 năm). Email cho hội viên là sai thị trường.
- **D4** — Stack **Spring Boot 3.5 + Postgres 16 + Angular 20**. Riêng `/q/{code}`
  server-render bằng Thymeleaf, không phải SPA. Đã lật ngược Next.js + Supabase. **Đã xét lại
  React ngày 25/07 và giữ Angular** — muốn mở lại câu hỏi này phải nêu được một ràng buộc kỹ
  thuật, không phải một cảm giác. Angular build ra file tĩnh cho Spring Boot serve, **không SSR**.
- **D5** — **Monorepo + modular monolith, một tiến trình.** Microservices không nằm trên bàn:
  nó phá cả ba cơ chế dưới, vì cả ba dựa vào một database + một transaction.

- **D6** — **Xếp hạng tháng và trial pipeline thuộc free tier.** Pro bán bằng: gỡ cap 50 hội viên,
  mirror Sheet, đa cơ sở + phân quyền, Zalo, member OTP. Lý do: xếp hạng chính là việc gig gốc
  thuê làm, và D2 chỉ đứng được khi xếp hạng tồn tại.
- **D7** — **`program` (bộ môn) là bảng riêng** + `member_program` nhiều–nhiều; `scan_point` có
  `program_id` nullable. Bộ môn là **tuỳ chọn**, bỏ qua được, để không phá north-star 10 phút.
- **D8** — **Một SĐT = một hội viên** (`UNIQUE (org_id, phone_normalized)`). Không hỗ trợ hội viên
  không có SĐT; trẻ con dùng số phụ huynh, mỗi đứa một số. Ở `/staff` cô giáo tap theo **tên**,
  không đụng SĐT.

- **D9** — **Bỏ fallback "mã 6 số" ở `/q`.** Không mạng thì `/q` không mở được (không service
  worker), nên chẳng có mã nào để hiện. Mạng hỏng → nhờ nhân viên điểm danh hộ qua `/staff` (F3).
- **D10** — **`audit_log` thuộc v1**, bảng dựng ở M1. Roster là dữ liệu định danh (D2) nên thao
  tác sửa nó phải để lại vết. Nhưng lý do loại Sheet-làm-roster **dẫn đầu bằng "không phân quyền"**,
  không phải bằng audit trail.

**Ràng buộc xuyên suốt:** free tier phải có chi phí biến đổi = **0đ**.

**Chữ "PWA" chỉ áp cho `/staff`** — `/q` là một trang web thường (không manifest, không service
worker), `/admin` chỉ cần manifest cho icon. Đừng viết "PWA" cho cả sản phẩm.

## Ba cơ chế dễ làm sai (chi tiết + code mẫu ở `DECISIONS.md`)

1. **Dedupe check-in** — đẩy xuống `UNIQUE INDEX (member_id, scan_point_id, dedupe_bucket)`
   + `ON CONFLICT DO NOTHING`. **Không** kiểm tra bằng `if (!exists)` ở tầng app.
2. **Cô lập đa tenant** — Postgres RLS + `SET LOCAL app.org_id`, **và** bộ test tự động
   cross-tenant cho mọi endpoint. Viết test này trước, không phải sau.
3. **Roster offline** — idempotency key `client_event_id` với unique index ở server, **và**
   service worker (offline "mức 2"). Không có service worker thì F5 lúc offline là app chết —
   mà trên mobile F5 tự xảy ra khi OS hủy tab.

## Quy ước

- Tài liệu viết bằng **tiếng Việt**.
- Business logic nằm ở tầng ứng dụng, không nằm trong DB — giữ đường thoát `pg_dump`
  sang Postgres VN-region cho PDPL.
