# DECISIONS — CheckinHub

Sổ ghi quyết định kiến trúc & sản phẩm. Mỗi mục là một quyết định đã chốt, kèm lý do.
**Đọc file này trước khi làm bất cứ việc gì trong project.**

| | |
|---|---|
| Cập nhật lần cuối | 2026-07-25 (D4 sửa lần 2, thêm D5) |
| Trạng thái áp dụng | `PRD.md` v2.0 đồng bộ D1–D3; **còn 6 lỗ phải vá ở Bước 4** (roster/Sheet + chữ "PWA" + giới hạn offline) · `PLAN.md` / `GRILL-LOG.md` ⚠️ **chưa** — xem `docs/plan-v2-rewrite.md` |

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

## D4 — Stack: Spring Boot 3.5 + Postgres 16 + React (Vite)

**Quyết định.** Backend Java. Lật ngược quyết định "Next.js + Supabase" ghi ở
`PLAN.md` §2 (2026-07-19), vốn đã lật `GRILL-LOG.md` Q14 trước đó.

| Lớp | Công nghệ |
|---|---|
| Backend | Spring Boot 3.5 + Postgres 16 + Flyway |
| `/q/{code}` — trang hội viên | **Thymeleaf server-render** + vài chục dòng JS |
| `/staff` + `/admin` | **React + Vite**, build ra file tĩnh, Spring Boot serve. **Không Next.js** |
| `/staff` — riêng | **PWA đầy đủ**: manifest + service worker + IndexedDB (xem cơ chế 3) |
| Deploy | Fly.io hoặc 1 VPS + docker compose + Caddy — **không k8s** |

> **Sửa 2026-07-25 (lần 2 trong ngày): Angular 20 → React + Vite.**
>
> Đây là **lần lật thứ tư** cho cùng câu hỏi frontend (Q14 Angular → 19/07 Next.js+React →
> D4 Angular → nay React). Lật nhiều vì **tiêu chí quyết định chưa được chốt** — lúc thì
> "stack đang thành thạo", lúc thì "nhanh nhất". Chốt tiêu chí ở đây để dừng lật:
>
> **Tiêu chí: cái nào người xây thật sự muốn mở editor lên làm, với điều kiện không phá
> ràng buộc kỹ thuật nào.** Với dự án solo part-time sống nhiều năm, động lực là ràng buộc
> thật, không phải chuyện cảm tính.
>
> Kiểm lại: React **không** phá ràng buộc nào, vì hai lợi thế thường viện dẫn cho Angular
> ở đây đều không áp dụng. (1) *Bundle nhẹ / cold start* — trang nhạy cold start là `/q`,
> đã Thymeleaf; `/staff` và `/admin` nằm sau đăng nhập, dùng lặp lại hàng ngày, cài như PWA.
> (2) *Batteries included* — Angular CLI cho sẵn router/forms/PWA, React phải tự lắp một lần
> (~1 ngày), đổi lại ecosystem rộng hơn nhiều và **AI hỗ trợ tốt hơn rõ rệt** — yếu tố
> velocity thật khi làm một mình.
>
> Phần khó nhất của frontend (outbox + sync offline) là **code tự viết ở cả hai** — framework
> không giúp gì. Nên lựa chọn này không ảnh hưởng tới rủi ro lớn nhất.
>
> **Ràng buộc kèm theo:** React ở đây là **Vite build ra file tĩnh**, Spring Boot serve.
> Tuyệt đối không Next.js — nó kéo theo một Node runtime phải vận hành, đúng thứ D4 vừa dọn đi.

**Lý do.** Lợi thế lớn nhất của Supabase là **phone OTP có sẵn**. Nhưng Supabase phone auth
chạy qua Twilio/Vonage — đắt và deliverability kém với số VN. Muốn OTP qua ZNS thì **phải tự
viết auth dù chọn stack nào** → lợi thế đó bốc hơi. Cộng thêm D2 (v1 không OTP), nó biến mất hẳn.

Phần Supabase còn lại đều có tương đương rẻ: Realtime → SSE với `SseEmitter` (~30 dòng);
RLS → Postgres thuần có sẵn, không phải tính năng riêng của Supabase; managed Postgres →
Neon/Railway/Fly.

Không có ràng buộc kỹ thuật nào bắt phải dùng Next/Supabase: tải ước tính chỉ **1–2 write/giây**
(1.000 trung tâm × 100 check-in/ngày). Đây là sản phẩm sống nhiều năm, do một người bảo trì —
ưu tiên stack người đó thành thạo.

**Vì sao `/q/{code}` không phải SPA — quy về một con số.** Hội viên **chưa từng vào site này**
(lần đầu quét QR, cache trống), đang **đứng ở cửa** trên **4G nguội**. Đếm số lượt đi lại:

| | SPA | Thymeleaf |
|---|---|---|
| | 1. tải `index.html` rỗng | 1. tải HTML **đã có sẵn** "Còn 12 buổi" |
| | 2. tải bundle JS ← chỗ đau | |
| | 3. JS khởi động | |
| | 4. JS gọi API lấy trạng thái thẻ | |
| | 5. mới hiện chữ | |
| **Tổng** | 4–5 lượt | **1 lượt** |

Và trang đó có gì? Một ô nhập SĐT, một dòng chữ, một cái nút. Framework không mua được gì —
không state phức tạp, không routing, không real-time. Đây là trang **duy nhất** trong sản phẩm
mà người dùng chưa từng vào, và nó nằm ngay giữa vòng lặp cốt lõi.

**Chữ "PWA" chỉ áp cho `/staff`.** PWA không phải công nghệ, nó là cái nhãn cho *website +
`manifest.json` + service worker*. Ba mặt tiền cần ba mức khác nhau — trộn chúng vào một chữ
là nguồn gốc của lẫn lộn:

| Mặt tiền | manifest | service worker | Thực chất là |
|---|---|---|---|
| `/q` hội viên | ❌ | ❌ | **một trang web thường** — quét, xem, bấm, đi |
| `/staff` | ✅ | ✅ | **PWA đầy đủ** — cài icon, toàn màn hình, chạy offline |
| `/admin` | ✅ | ❌ | website + icon cho tiện bấm |

`display: "standalone"` trong manifest là thứ bỏ thanh địa chỉ đi, khiến `/staff` trông như app
tải từ store. Đổi lại việc **không qua App Store**: không xét duyệt, không chờ Apple, sửa lỗi là
deploy. Đây là lý do `GRILL-LOG` Q13 chọn web — vòng lặp cốt lõi bắt đầu bằng camera quét QR dán
trên tường, bắt cài app ở cửa là vòng lặp chết ngay tại đó.

**Exit door vẫn giữ (cho PDPL data residency).** Business logic nằm ở tầng ứng dụng, không
nằm trong DB. Migration = `pg_dump` → managed Postgres ở VN region (Viettel/VNG/FPT) →
đổi một connection string.

---

## D5 — Kiến trúc: monorepo + modular monolith, một tiến trình

**Quyết định.** Một repo. **Một tiến trình khi chạy.** Một lần deploy. Microservices **không nằm
trên bàn**, kể cả về sau, trừ khi có team.

Hai chữ hay bị gộp — chúng là hai trục độc lập: *repo* (monorepo ↔ multi-repo) và *runtime*
(monolith ↔ microservices). Quyết định này là **monorepo + monolith**.

```
manage-pwa/
├── backend/                        # Spring Boot 3.5
│   ├── src/main/java/com/checkinhub/...
│   ├── src/main/resources/
│   │   ├── templates/q/            # Thymeleaf — /q/{code}
│   │   ├── db/migration/           # Flyway
│   │   └── static/app/             # ← bundle React copy vào lúc build
│   └── pom.xml
├── frontend/                       # Vite + React → /staff + /admin
│   ├── src/
│   ├── public/manifest.json
│   └── vite.config.ts
├── docker-compose.yml              # postgres + app (+ caddy)
└── Dockerfile
```

Build: Vite → file tĩnh → copy vào `static/app/` → Maven đóng **một** jar → **một** image.

**Vì sao không microservices — lý do mạnh nhất nằm trong chính ba cơ chế dưới.**
Lập luận thường gặp: tải chỉ **1–2 write/giây**; microservices giải bài toán **tổ chức**
(nhiều team deploy độc lập), mà đây là một người — không có chi phí phối hợp nào để giải.

Nhưng lý do cứng hơn: **cơ chế 1 đòi insert check-in và trừ buổi cùng một transaction.** Tách
`checkin-service` / `entitlement-service` → `@Transactional` biến thành **saga phân tán với
compensating action** (ghi check-in xong, trừ buổi lỗi, phải gọi ngược để xoá, lệnh xoá cũng có
thể lỗi...). Đổi một dòng annotation lấy một hệ thống bù trừ, để phục vụ 2 write/giây.

**Cơ chế 2 cũng vỡ:** RLS + `SET LOCAL app.org_id` chỉ có nghĩa trong **một connection, một
transaction**. Tách service là mất lớp cô lập, phải tự canh `org_id` bằng tay ở mọi chỗ gọi —
đúng thứ RLS được chọn để khỏi phải làm.

Cả ba cơ chế đã chốt đều dựa vào **một database, một transaction**. Microservices phá cả ba.

**Nhưng giữ cửa thoát — modular monolith.** Một tiến trình, code chia theo miền nghiệp vụ:

```
com.checkinhub
├── org/            # tenant, scan_point, giờ mở cửa, GPS
├── member/         # member, member_device
├── entitlement/    # gói thẻ, consume policy
├── checkin/        # checkin_event, dedupe bucket
├── trial/          # lead pipeline (F4)
├── report/         # xếp hạng, CSV, mirror Sheet
├── notification/   # outbox, Zalo (Pro)
├── auth/           # magic link, device token
└── shared/         # config, RLS interceptor, SSE
```

Quy tắc duy nhất phải giữ: **module gọi nhau qua interface, không thò tay vào repository của
nhau.** Giữ được thì 5 năm sau cần tách sẽ tách theo đường có sẵn; không cần thì chẳng mất gì.
Đúng tinh thần exit door của D4: **chuẩn bị đường thoát, không xây sẵn con đường.**

**Hai chỗ đừng tách thành service.**

| Việc | Nghe như cần service riêng vì async | Thực tế |
|---|---|---|
| Gửi Zalo/ZNS, digest ngày | ✅ | Bảng `notification_outbox` **chính là** hàng chờ + một `@Scheduled` quét bảng. Postgres là message queue đủ tốt ở quy mô này |
| Mirror Google Sheet | ✅ | Một job định kỳ ghi theo batch, trong cùng app |

**Ai serve file React tĩnh.** Chọn **Spring Boot serve từ `static/`** — đúng một artifact để
deploy, để rollback, dev/prod đồng nhất. Cần một controller fallback trả `index.html` cho route
phía client (`/admin/members`). Caddy chỉ làm TLS. Phương án Caddy-serve-tĩnh-proxy-`/api` tốt
hơn về cache header nhưng thêm chỗ cấu hình phải đồng bộ — đổi sang sau là việc mười phút.

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

### 3. Roster offline — idempotency key **và** service worker

Nhân viên tap tên → ghi vào outbox trong IndexedDB `{client_event_id: uuid, member_id, ...}` →
UI tick xanh ngay (optimistic) → có mạng thì POST cả batch. Server dedupe bằng unique index
trên `client_event_id`. Client gửi lại 10 lần vì mạng chập chờn → vẫn đúng 1 dòng.
Không cần logic conflict resolution phức tạp.

**Vì sao `/staff` chạy được offline mà `/q` thì không** — bất đối xứng này là cả câu trả lời:

| | `/q` hội viên tự quét | `/staff` nhân viên điểm danh |
|---|---|---|
| Dữ liệu cần | Không biết trước — ai sẽ quét? | **Biết trước** — danh sách hôm nay, tải từ sáng lúc còn mạng |
| Cần server trả lời? | **Có** — tra hội viên, kiểm thẻ, trừ buổi, rồi mới hiện "còn 12 buổi" | **Không** — giáo viên đang *nhìn thấy* học viên |
| Kết quả | Phải hiện ngay cho hội viên đọc | Chỉ là *ghi nhận*, hoãn gửi được |

Cả input lẫn output của `/staff` đều ở local → offline được. `/q` thì không có cách nào.

**Quyết định 2026-07-25: làm offline "mức 2" (có service worker), bỏ "mức 1".**

| Mức | Gồm | Chịu được |
|---|---|---|
| 0 | Chỉ hàng chờ để request lỗi không mất dữ liệu | Mạng chập chờn. **Không** hứa offline |
| ~~1~~ | Outbox, **không** service worker | Mạng chết *khi trang đang mở*. **Chết khi F5** |
| **2** | Outbox + service worker + danh sách trong IndexedDB | Mở app khi đang offline từ đầu |

**Vì sao gạch mức 1.** Không có service worker thì F5 khi offline = browser đi xin `index.html`
từ mạng → không có → trang lỗi. Và **trên điện thoại F5 không cần ai bấm**: iOS Safari tự hủy
tab khi thiếu bộ nhớ (rất hay xảy ra trên máy rẻ, mà nhân viên trung tâm nhỏ dùng máy rẻ);
giáo viên chuyển sang Zalo 10 phút rồi quay lại là tab có thể đã bị hủy. Giả định *"trang vẫn
mở suốt 90 phút của lớp"* không đáng tin trên mobile.

Mức 1 tốn gần bằng mức 2 nhưng hỏng đúng lúc cần, và tệ nhất là nó **cho cảm giác đã có offline**.
Hứa mà hỏng tệ hơn không hứa.

**Chi phí mức 2 cộng thêm trên mức 1: ~2–3 ngày part-time.** Phần nặng (outbox + sync) mức 1 cũng
phải làm. Cộng thêm: `vite-plugin-pwa` sinh service worker (~½ ngày) + lưu danh sách vào IndexedDB
thay vì RAM (~½ ngày) + đường khởi-động-không-mạng (~½ ngày) + ~1 ngày vật lộn với hai nỗi đau
kinh điển (deploy rồi người dùng vẫn thấy bản cũ vì service worker cũ còn phục vụ từ cache; và
debug ma quái vì tưởng code mới mà đang chạy bản cache). Dùng `registerType: 'prompt'` để hiện
thanh *"có bản mới, tải lại"* thay vì đổi ngầm.

**Đừng lẫn hai thứ:** service worker làm app **mở được** khi offline (cache vỏ: HTML/JS/CSS);
IndexedDB outbox làm **tap không mất** (dữ liệu). Cần cả hai, nhiệm vụ khác nhau.

**Ba giới hạn đã chấp nhận — ghi ra để không ai tưởng là bug:**

1. **Lần đầu buộc phải có mạng.** Service worker chỉ được cài khi mở app lần đầu online. Nhân
   viên mới, máy mới, chưa từng mở app → xuống hầm là vô dụng. → Onboarding phải có bước
   *"mở app một lần ở chỗ có mạng trước khi vào lớp"*.
2. **Danh sách cache có thể cũ.** Cache từ 8h sáng thì hội viên thêm lúc 17h không có trong đó,
   offline không cách nào biết. Đường thoát: chủ sửa lại sau trong `/admin`. **Không giải ở v1.**
3. **iOS không tự mời cài.** Chrome/Android tự hiện thanh *"Cài ứng dụng này?"*; Safari thì người
   dùng phải bấm Share → cuộn → *"Thêm vào MH chính"*, và **không ai tự tìm ra**. → Cần màn hình
   hướng dẫn có ảnh, hiện khi phát hiện Safari trên iPhone (~½ ngày). Bỏ qua bước này thì 2–3 ngày
   làm offline trở thành vô dụng với một nửa người dùng.

Phụ: service worker chỉ chạy trên **HTTPS** (hoặc `localhost`), và chỉ quản phạm vi đường dẫn của
nó — đăng ký ở `/staff/` thì `/q` và `/admin` không bị dính vào. Tiện.

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
