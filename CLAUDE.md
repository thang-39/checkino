# CheckinHub (tên tạm)

SaaS điểm danh QR + quản lý thẻ hội viên cho các trung tâm nhỏ ở Việt Nam
(gym, võ thuật, yoga, trung tâm ngoại ngữ, gia sư). Một người xây, part-time.

## Trạng thái

**Chưa có code.** Project hiện chỉ có tài liệu. Đang ở giai đoạn viết lại PRD/PLAN lên v2.

## Đọc trước khi làm bất cứ việc gì

**[`DECISIONS.md`](DECISIONS.md)** — sổ quyết định kiến trúc & sản phẩm, kèm lý do và số liệu
kiểm chứng. Đây là nguồn sự thật. `PRD.md` đã được đồng bộ (v2.0), nhưng `PLAN.md` **chưa được
cập nhật** theo các quyết định này; khi hai bên mâu thuẫn thì `DECISIONS.md` thắng.

[`docs/plan-v2-rewrite.md`](docs/plan-v2-rewrite.md) — kế hoạch thi hành việc cập nhật đó,
kèm bảng tiến độ. Cập nhật bảng tiến độ sau mỗi bước.

## Năm quyết định đã chốt (tóm tắt — chi tiết ở `DECISIONS.md`)

- **D1** — Zalo OA/ZNS là tính năng **gói Pro**, không thuộc lõi v1. Free tier chạy hoàn toàn
  không cần Zalo. Lý do: xác thực OA bắt buộc có GPKD của khách hàng, giết mục tiêu onboarding
  tự phục vụ dưới 10 phút.
- **D2** — v1 **không gửi OTP** cho hội viên. Chủ import danh sách → hội viên nhập SĐT →
  khớp thì bind device token ngay. ZNS không có free tier (300đ/tin xác thực).
- **D3** — Tách hai nhu cầu auth: chủ/nhân viên dùng **email magic link**; hội viên dùng
  **device token** (cookie httpOnly, TTL 1 năm). Email cho hội viên là sai thị trường.
- **D4** — Stack **Spring Boot 3.5 + Postgres 16 + React (Vite)**. Riêng `/q/{code}`
  server-render bằng Thymeleaf, không phải SPA. Đã lật ngược Next.js + Supabase, rồi lật
  Angular → React (25/07). **Không Next.js** — React ở đây là Vite build ra file tĩnh.
- **D5** — **Monorepo + modular monolith, một tiến trình.** Microservices không nằm trên bàn:
  nó phá cả ba cơ chế dưới, vì cả ba dựa vào một database + một transaction.

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
