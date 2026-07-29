# Checkino

SaaS điểm danh QR + quản lý thẻ hội viên cho các trung tâm nhỏ ở Việt Nam
(gym, võ thuật, yoga, trung tâm ngoại ngữ, gia sư). Một người xây, part-time.

## Trạng thái

**Chưa có code.** Backlog đã dựng: 5 epic, 58 story ở [`docs/STORIES.yml`](docs/STORIES.yml),
render thành GitHub issues. Việc tiếp theo: `M1-S01` khởi tạo monorepo.

Muốn biết làm gì tiếp → chạy **`/next`**. Đừng tự suy ra từ tài liệu.

## Tài liệu nằm ở đâu, và cái nào thắng

| Tầng | File | Chứa gì | Thắng khi lệch |
|---|---|---|---|
| Tại sao | [`DECISIONS.md`](DECISIONS.md) | D1–D11 + Ba cơ chế + SQL mẫu | **Luôn thắng** |
| Sản phẩm làm gì | `PRD.md` (v2.3) | F1–F11, NFR, tier, metric | Hành vi sản phẩm |
| Stack & hình dáng | `PLAN.md` (v2.2) | stack, schema, M0–M4, DoD, danh sách cắt | Ý định milestone |
| **Việc phải làm** | [`docs/STORIES.yml`](docs/STORIES.yml) | acceptance criteria + thứ tự phụ thuộc | **Chia việc** |
| Trạng thái | GitHub issues | open/closed, comment, PR | Không phải nguồn |

Quy tắc chống rối — **tuân thủ, đừng lách:**

- **Chi tiết mức việc CHỈ nằm ở `docs/STORIES.yml`.** Không thêm checklist vào PRD/PLAN nữa.
- **Story TRỎ tới docs, không sao lại nội dung.** Story chỉ tự sinh ra hai thứ chưa có ở đâu:
  acceptance criteria và thứ tự phụ thuộc. Ngoại lệ duy nhất được nhắc lại: câu **cấm** ngắn.
- **Issue là bản render một chiều** từ `STORIES.yml`. Sửa body issue trên GitHub sẽ bị ghi đè.
- Thêm/sửa story → dùng skill **`write-story`**, rồi `/sync-issues`.
- Đang code mà phát hiện quyết định còn thiếu → **ghi vào `DECISIONS.md`**, không quyết ngầm
  trong code hay trong acceptance criteria.

`docs/archive/` là tài liệu đã hết vai trò (`GRILL-LOG.md`, `plan-v2-rewrite.md`) — giữ làm
lịch sử, không đọc để làm việc.

## Lệnh

| Lệnh | Làm gì |
|---|---|
| `/next` | Nên làm story nào tiếp, kèm lý do |
| `/plan <N>` | Lập plan thi hành cho issue #N, comment lên issue |
| `/work <N>` | Branch, code, test, PR |
| `/status` | Bảng tiến độ theo epic |
| `/sync-issues` | Đẩy `docs/STORIES.yml` lên GitHub issues |

GitHub: `thang-39/checkino`. Account **work** là mặc định của `gh` trên máy này — 404 ở mọi
lệnh nghĩa là cần `gh auth switch -u thang-39`. Remote dùng ssh alias `github.com-personal`.

## Mốc thời gian — đừng lặp lại con số 8 tuần

`PLAN.md § 4` nêu ~8 tuần tới hết M3. Backlog cộng lại là **43 ngày-người part-time**
(M0→M3), tức **17–21 tuần** ở 15–20h/tuần. `PLAN.md` chỉ ước lượng hai hạng mục rồi suy ra
tổng. Khi báo tiến độ thì dùng con số của backlog và quy đổi ra tuần.

## Mười một quyết định đã chốt (tóm tắt — chi tiết ở `DECISIONS.md`)

- **D1** — Zalo OA/ZNS là tính năng **gói Pro**, không thuộc lõi v1. Free tier chạy hoàn toàn
  không cần Zalo. Lý do: xác thực OA bắt buộc có GPKD của khách hàng, giết mục tiêu onboarding
  tự phục vụ dưới 10 phút.
- **D2** — v1 **không gửi OTP** cho hội viên. Chủ import danh sách → hội viên nhập SĐT →
  khớp thì bind device token ngay. ZNS không có free tier (300đ/tin xác thực).
- **D3** — Tách hai nhu cầu auth: chủ/nhân viên dùng **email magic link**; hội viên dùng
  **device token** (cookie httpOnly, TTL 1 năm). Email cho hội viên là sai thị trường.
- **D4** — Stack **Spring Boot 4.1 + Java 25 + Postgres 18 + Angular 22** (bump 29/07/2026).
  Riêng `/q/{code}` server-render bằng Thymeleaf, không phải SPA. Đã lật ngược Next.js +
  Supabase. **Đã xét lại React ngày 25/07 và giữ Angular** — muốn mở lại câu hỏi này phải nêu
  được một ràng buộc kỹ thuật, không phải một cảm giác. Angular build ra file tĩnh cho Spring
  Boot serve, **không SSR**. Hai ripple đừng quên: Boot 4.1 kéo **Testcontainers 2.0.5** (major,
  API khác 1.x) và Angular 22 đòi **Node ≥ 22.22.3** (ràng buộc lúc build, pin ở `.tool-versions`).
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
- **D11** — Tên thương hiệu: **Checkino** (hết tên tạm CheckinHub). Brand = repo = package,
  một token: `checkino`, `com.checkino`. Tiêu chí, các tên đã loại và kiểm chứng va chạm
  ở `DECISIONS.md`. Domain chưa mua — cần whois tay trước.

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
