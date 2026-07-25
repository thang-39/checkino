# DECISIONS — CheckinHub

Sổ ghi quyết định kiến trúc & sản phẩm. Mỗi mục là một quyết định đã chốt, kèm lý do.
**Đọc file này trước khi làm bất cứ việc gì trong project.**

| | |
|---|---|
| Cập nhật lần cuối | 2026-07-25 |
| Trạng thái áp dụng | ⚠️ Đã chốt nhưng **chưa** phản ánh vào `PRD.md` / `PLAN.md` / `GRILL-LOG.md` — xem `docs/plan-v2-rewrite.md` |

---

## Bối cảnh — điều gì đã kích hoạt loạt quyết định này

Tài liệu v1 (2026-07-19) đặt Zalo OA/ZNS vào **lõi** của sản phẩm v1, và đặt north-star là
*"đăng ký một mình → check-in đầu tiên < 10 phút, không cần gặp người"*.

Khi kiểm chứng chi phí và điều kiện thực tế của Zalo, ba dữ kiện xuất hiện và chúng
mâu thuẫn trực tiếp với nhau.

### Dữ kiện 1 — ZNS không có free tier

Bảng giá ZNS (kiểm chứng 2026-07-25):

| Loại tin | Giá/tin |
|---|---|
| **Tin xác thực (OTP)** | **300đ** |
| Tin yêu cầu thanh toán | 300đ |
| Tin voucher | 300đ |
| Tin hành chính | 120đ |
| Loại khác | 200đ |

Phụ phí: nút CTA thứ 2 trở đi +100đ, ảnh +200đ. Trả theo tin **gửi thành công**.
Không có hạn mức miễn phí. So sánh: SMS Brandname 600–1.000đ/tin.

Từ 01/01/2026 Zalo đổi tên ZCA → **ZBS Account**, gộp các loại tin thành "ZBS Template Message".

### Dữ kiện 2 — OA xác thực bắt buộc có GPKD

Muốn gọi API gửi tin (OA message hoặc ZNS) thì phải có **OA đã xác thực** + đăng ký ứng dụng
trên `developers.zalo.me`. Xác thực OA yêu cầu **giấy phép kinh doanh** (bản gốc hoặc công chứng,
còn hiệu lực). Tạo OA thì miễn phí, gói Basic cũng miễn phí nhưng giới hạn.

Muốn tin nhắn mang tên trung tâm → **mỗi khách hàng cần OA xác thực riêng** → mỗi khách hàng
cần GPKD của chính họ. Rất nhiều trung tâm nhỏ ở VN không có GPKD, hoặc ngại làm thủ tục.

### Dữ kiện 3 — hệ quả

North-star *"< 10 phút, không cần gặp người"* là **bất khả thi** nếu Zalo nằm trong lõi v1.
Không thể vừa bắt khách đi làm GPKD + xác thực OA (mất nhiều ngày), vừa hứa onboarding
10 phút tự phục vụ.

Thêm một ràng buộc từ phía người xây: làm một mình, part-time, **không muốn phát sinh chi phí
trước khi có doanh thu**.

---

## D1 — Zalo là tính năng gói Pro, không thuộc lõi v1

**Quyết định.** Sản phẩm free tier chạy **hoàn toàn không cần Zalo**: QR check-in + dashboard
live real-time + xuất CSV/Google Sheet. Zalo OA/ZNS chỉ bật cho khách đã nâng cấp Pro và
đã có OA xác thực của riêng họ.

**Lý do.** Rào cản GPKD (Dữ kiện 2) giết mục tiêu onboarding tự phục vụ. Giữ Zalo ở lõi
đồng nghĩa từ bỏ north-star.

**Hệ quả.**
- Free tier có **chi phí biến đổi = 0đ** → nuôi bao nhiêu khách miễn phí cũng không lỗ.
- Spike Zalo **rời khỏi đường tới hạn**. Có thể bắt đầu viết code ngay mà không cần trả lời
  câu hỏi Zalo trước. Milestone Zalo lùi từ M3 xuống M4.
- Rủi ro "Zalo đổi chính sách/giá" tự động giảm mức, vì sản phẩm không còn phụ thuộc nó để sống.

---

## D2 — v1 bỏ OTP cho hội viên

**Quyết định.** Không gửi OTP khi hội viên check-in lần đầu. Thay bằng:

```
Chủ import danh sách hội viên (họ đã có sẵn Excel)
   │
Hội viên quét QR → nhập SĐT
   │
   ├─ Số CÓ trong danh sách trung tâm này → bind device token ngay, không OTP
   │     └─ ghi log; 1 số bind ở 2 máy trong thời gian ngắn → gắn cờ bất thường
   │
   └─ Số LẠ → form đăng ký học thử (lead) → vào trial pipeline của chủ
```

**Lý do.** OTP ở đây chống đúng một thứ: A nhập số của B để điểm danh hộ B. Hậu quả duy nhất
là **lệch bảng xếp hạng chuyên cần**. Không có tiền bị mất, không có dữ liệu nhạy cảm bị lộ —
màn hình chỉ hiện *"Còn 12 buổi"*. Rủi ro này đã có cơ chế xử lý: cờ bất thường + chủ trung tâm
review trước khi trao thưởng tháng.

**Phép tính chi phí — vì sao thiết kế device token quan trọng:**

| Thiết kế | Số tin OTP | Chi phí |
|---|---|---|
| **Device token** (OTP 1 lần/đời/người) — 10 trung tâm × 100 hội viên | 1.000 tin, **một lần duy nhất** | **300.000đ** tổng |
| Bắt login mỗi lần vào — cùng quy mô | 1.000 người × 12 buổi/tháng = 12.000 tin/**tháng** | **3,6 triệu/tháng** |

Con số đáng sợ chỉ xuất hiện khi thiết kế sai. Với device token, chi phí là 300k một lần cho
cả 10 trung tâm pilot — nhưng D2 đưa nó về **0đ**.

**Hệ quả.** OTP trở thành tính năng nâng cấp của gói Pro; khi đó 300đ/tin đã nằm trong
199k/tháng khách trả. Đường nâng cấp về sau: **Zalo Login (OAuth)** — không tính phí theo tin,
chỉ tốn phí xác thực OA một lần.

---

## D3 — Tách hai nhu cầu xác thực

**Quyết định.**

| Ai | Tần suất | Cơ chế | Chi phí |
|---|---|---|---|
| **Chủ / nhân viên** | ~1 lần/tháng | **Email + magic link** (Resend/Brevo free tier) | 0đ |
| **Hội viên** ở cửa | 1 lần/đời, rồi nhớ máy | **Device token** (cookie httpOnly, TTL 1 năm) + D2 | 0đ |

**Lý do.** Hai nhu cầu này trước đây bị gộp làm một. Chúng ngược nhau hoàn toàn:
đăng nhập hiếm & chịu được ma sát vs. định danh thường xuyên & cực nhạy với ma sát.

**Email cho hội viên là sai thị trường** — người tập gym ở VN không mở email, nhiều người
không có email dùng thường xuyên, và đứng ở cửa mà phải mở Gmail lấy mã thì tệ hơn nhập OTP.
Nhưng email cho **chủ/nhân viên** thì đúng và miễn phí.

**Lưu ý phân biệt:** vẫn **cần lưu** số điện thoại hội viên (trung tâm cần để liên lạc).
*Cần lưu số* ≠ *cần xác thực số*.

**Chi tiết device token.**
```
member_device (id, member_id, token_hash, user_agent, created_at, last_seen_at, revoked_at)
```
Lưu **hash** của token, không lưu bản gốc. Tối đa 3 device/member, cái cũ nhất bị đẩy ra.
Máy dùng chung → nút *"Không phải bạn? Đổi số"*. Mất mạng → hiện mã 6 số cho nhân viên nhập tay.

---

## D4 — Stack: Spring Boot 3.5 + Postgres 16 + Angular 20 PWA

**Quyết định.** Quay lại stack Java. Lật ngược quyết định "Next.js + Supabase" ghi ở
`PLAN.md` §2 (2026-07-19), vốn đã lật `GRILL-LOG.md` Q14 trước đó.

| Lớp | Công nghệ |
|---|---|
| Backend | Spring Boot 3.5 + Postgres 16 + Flyway |
| `/q/{code}` — trang hội viên | **Thymeleaf server-render** + vài chục dòng JS |
| `/staff` + `/admin` | **Angular 20 PWA** (offline queue, IndexedDB, service worker) |
| Deploy | Fly.io hoặc 1 VPS + docker compose + Caddy — **không k8s** |

**Lý do.** Lợi thế lớn nhất của Supabase là **phone OTP có sẵn**. Nhưng Supabase phone auth
chạy qua Twilio/Vonage — đắt và deliverability kém với số VN. Muốn OTP qua ZNS thì **phải tự
viết auth dù chọn stack nào** → lợi thế đó bốc hơi. Cộng thêm D2 (v1 không OTP), nó biến mất hẳn.

Phần Supabase còn lại đều có tương đương rẻ: Realtime → SSE với `SseEmitter` (~30 dòng);
RLS → Postgres thuần có sẵn, không phải tính năng riêng của Supabase; managed Postgres →
Neon/Railway/Fly.

Không có ràng buộc kỹ thuật nào bắt phải dùng Next/Supabase: tải ước tính chỉ **1–2 write/giây**
(1.000 trung tâm × 100 check-in/ngày). Đây là sản phẩm sống nhiều năm, do một người bảo trì —
ưu tiên stack người đó thành thạo.

**Vì sao `/q/{code}` không phải SPA.** Trang này chỉ hiện trạng thái thẻ + 1 nút, nhưng phải
load nhanh trên 4G nguội khi người ta đang đứng ở cửa. Nhét Angular vào đây là tự hại:
bundle nặng, cold start chậm, mà không dùng đến gì của framework.

**Exit door vẫn giữ (cho PDPL data residency).** Business logic nằm ở tầng ứng dụng, không
nằm trong DB. Migration = `pg_dump` → managed Postgres ở VN region (Viettel/VNG/FPT) →
đổi một connection string.

---

## Ba cơ chế kỹ thuật đã chốt

Ghi ở đây vì chúng dễ làm sai và hậu quả nặng.

### 1. Dedupe race-safe — đẩy xuống ràng buộc DB, không làm ở tầng app

Cách **sai** (2 request đồng thời đều thấy "chưa có" → 2 dòng):
```java
if (!repo.existsByMemberAndDate(member, today)) { repo.insert(...); }
```

Cách **đúng**:
```sql
CREATE UNIQUE INDEX uq_checkin_bucket
  ON checkin_event (member_id, scan_point_id, dedupe_bucket);

INSERT INTO checkin_event (...) VALUES (...)
ON CONFLICT (member_id, scan_point_id, dedupe_bucket) DO NOTHING
RETURNING id;   -- không có row → đã điểm danh rồi
```

`dedupe_bucket` tính theo `consume_policy` của entitlement:

| Policy | dedupe_bucket | Hiệu quả |
|---|---|---|
| `ONCE_PER_DAY` | `"2026-07-25"` (giờ Asia/Ho_Chi_Minh) | Quét lần 2 trong ngày → đụng index → bỏ qua |
| `PER_VISIT` | UUID ngẫu nhiên | Không bao giờ đụng → mỗi lần quét là 1 lượt |
| `PER_CLASS` | `"2026-07-25#slot_18h"` | 1 lượt mỗi ca học |

Insert và trừ buổi phải **cùng một transaction**; `UPDATE ... WHERE sessions_used < session_quota`
với kiểm tra affected rows = 0 → hết buổi → rollback.

### 2. Cô lập đa tenant — hai lớp, và lớp test là bắt buộc

**Lớp 1 — Postgres RLS** (không phụ thuộc Supabase):
```sql
ALTER TABLE checkin_event ENABLE ROW LEVEL SECURITY;
CREATE POLICY org_isolation ON checkin_event
  USING (org_id = current_setting('app.org_id')::uuid);
```
Đầu mỗi transaction: `SET LOCAL app.org_id = '...'`.

**Lớp 2 — test tự động, không thương lượng:** tạo org A và org B; với **mọi** endpoint, dùng
token của A cố đọc/sửa dữ liệu của B → assert 403 hoặc rỗng. Chạy trong CI.
Không có bộ test này thì lớp 1 chỉ là niềm tin. **Viết test này trước, không phải sau.**

### 3. Roster offline — idempotency key

Nhân viên tap tên → ghi vào outbox trong IndexedDB `{client_event_id: uuid, member_id, ...}` →
UI tick xanh ngay (optimistic) → có mạng thì POST cả batch. Server dedupe bằng unique index
trên `client_event_id`. Client gửi lại 10 lần vì mạng chập chờn → vẫn đúng 1 dòng.
Không cần logic conflict resolution phức tạp.

---

## Nguồn kiểm chứng

- [Bảng giá ZNS mới nhất 2026 — smsthuonghieu.com](https://www.smsthuonghieu.com/gia-zns/)
- [Chi phí vận hành tài khoản Zalo OA doanh nghiệp xác thực — oa.zalo.me](https://oa.zalo.me/home/documents/guides/chi-phi-van-hanh-tai-khoan-zalo-oa-doanh-nghiep-xac-thuc_4294439646029434342)
- [Zalo Notification Service / ZBS Template Message — oa.zalo.me](https://oa.zalo.me/home/documents/guides/zbs-template-message)
- [Chính sách xác thực tài khoản OA — oa.zalo.me](https://oa.zalo.me/home/documents/policy/xac-thuc-tai-khoan)
- [Zalo OA OpenAPI — developers.zalo.me](https://developers.zalo.me/docs/api/official-account-api-230)

---

## Còn bỏ ngỏ

1. **Google Sheets API quota** ở quy mô nhiều khách hàng — cần đo write rate thực tế, ghi theo batch.
2. **Tên thương hiệu & domain** — "CheckinHub" chỉ là placeholder.
3. **Độ chính xác GPS** tại cửa hàng thật (trong nhà) — quyết định ngưỡng bán kính cho lớp
   soft-check chống gian lận.
