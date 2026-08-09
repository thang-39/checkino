# DECISIONS — Checkino

Sổ ghi quyết định kiến trúc & sản phẩm. Mỗi mục là một quyết định đã chốt, kèm lý do.
**Đọc file này trước khi làm bất cứ việc gì trong project.**

| | |
|---|---|
| Cập nhật lần cuối | 2026-08-05 (D13: `/admin` trên màn rộng là một cột căn giữa, chốt sau khi dựng xong `/admin` nền tảng) |
| Trạng thái áp dụng | ✅ **Tài liệu đã đồng bộ hết D1–D13, không còn mục lệch.** `PRD.md` **v2.3** · `PLAN.md` **v2.2** · `GRILL-LOG.md` có ghi chú superseded (Q6, Q13, Q14) + Q16, giữ nguyên phần lịch sử. Kết quả rà lại năm mục kiểm tra: [`docs/archive/plan-v2-rewrite.md`](docs/archive/plan-v2-rewrite.md) mục "Kết quả Bước 5". Việc tiếp theo là **viết code** (Bước 6 trong file đó) |

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
Máy dùng chung → nút *"Không phải bạn? Đổi số"*. ~~Mất mạng → hiện mã 6 số cho nhân viên nhập tay.~~
→ **Superseded bởi [D9](#d9--bỏ-fallback-mã-6-số-ở-q):** mất mạng thì `/q` không mở được nên không
có mã nào để hiện; đường thoát là nhân viên điểm danh hộ qua `/staff` *(F3)*.

---

## D4 — Stack: Spring Boot 4.1 + Postgres 18 + Angular 22

**Quyết định.** Quay lại stack Java. Lật ngược quyết định "Next.js + Supabase" ghi ở
`PLAN.md` §2 (2026-07-19), vốn đã lật `GRILL-LOG.md` Q14 trước đó.

| Lớp | Công nghệ |
|---|---|
| Backend | Spring Boot 4.1 + Java 25 LTS + Postgres 18 + Flyway |
| `/q/{code}` — trang hội viên | **Thymeleaf server-render** + vài chục dòng JS |
| `/staff` + `/admin` | **Angular 22**, build ra file tĩnh, Spring Boot serve |
| `/staff` — riêng | **PWA đầy đủ**: `@angular/pwa` + service worker + IndexedDB (xem cơ chế 3) |
| Deploy | Fly.io hoặc 1 VPS + docker compose + Caddy — **không k8s** |

> **Bump version — 2026-07-29.** Chốt trước khi viết dòng code đầu tiên (`M1-S01`), nên
> không tốn gì ngoài sửa tài liệu. Số lấy từ Maven Central / npm registry, không từ trí nhớ.
>
> | | Cũ | Mới | Xác minh bằng |
> |---|---|---|---|
> | Spring Boot | 3.5 | **4.1.0** | `<release>` trong `maven-metadata.xml` |
> | Java | (chưa ghi) | **25 LTS** | `temurin-25.0.4+7.0.LTS`; baseline Boot 4.1 chỉ đòi 17 |
> | Postgres | 16 | **18** | `postgres:18-alpine` |
> | Angular | 20 | **22.0.9** | `dist-tags.latest`; 20.3.32 nay là `v20-lts` |
>
> **Ripple kéo theo từ Boot 4.1.0** (đọc từ `spring-boot-dependencies-4.1.0.pom`):
> Spring Framework **7**, Hibernate **7.4.1**, Flyway **12.4.0**, Testcontainers **2.0.5**,
> driver Postgres 42.7.11.
>
> ⚠️ **Testcontainers 2.0.5 là bump major từ 1.x — API khác.** Đây là ripple đáng để ý nhất,
> vì cơ chế 2 (bộ test cô lập cross-tenant, `M1-S04`) và test dedupe đồng thời ở M2 đều dựng
> trên nó. Phần lớn snippet `@Container` / `@ServiceConnection` trên mạng còn là 1.x — đừng
> copy.
>
> ⚠️ **Boot 4 modularise autoconfiguration — mỗi tích hợp cần starter riêng.** Phát hiện lúc
> làm `M1-S01`, và là cái bẫy im lặng nhất trong cả bump này:
>
> | Cách viết cũ (Boot 3) | Boot 4 |
> |---|---|
> | `org.flywaydb:flyway-core` | `spring-boot-starter-flyway` |
> | `@WebMvcTest` từ `spring-boot-starter-test` | thêm `spring-boot-starter-webmvc-test` |
> | `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
> | `org.testcontainers:postgresql` | `org.testcontainers:testcontainers-postgresql` |
> | `org.testcontainers:junit-jupiter` | `org.testcontainers:testcontainers-junit-jupiter` |
>
> Cái Flyway là nguy hiểm nhất: chỉ có `flyway-core` trần thì Flyway **không được wire và
> migration im lặng không chạy** — app vẫn boot xanh, không có lỗi nào để thấy. `M1-S01` có
> một test chốt việc Flyway được wire, chính vì lý do này. Slice test sau (`@DataJpaTest` →
> `spring-boot-starter-data-jpa-test`) cũng theo cùng khuôn.
>
> ⚠️ **Postgres 18 đổi quy ước data directory.** Volume phải mount ở `/var/lib/postgresql`,
> **không** phải `/var/lib/postgresql/data` như 16/17 — mount kiểu cũ làm container thoát ngay
> với exit 1. Lý do: image đặt dữ liệu vào thư mục con theo major version để
> `pg_upgrade --link` chạy được.
>
> ⚠️ **Angular 22 đòi Node `^22.22.3 || ^24.15.0 || >=26`.** Đây là ràng buộc **lúc build**,
> không phải runtime: `PLAN.md § 2.1` vẫn cấm Node runtime ở production, bump này không lật
> điều đó. Repo pin bằng `.tool-versions` ở root.
>
> Lập luận "giữ Angular thay vì React" dưới đây **không phụ thuộc version** và vẫn đứng —
> `@angular/pwa@22.0.9` vẫn tồn tại, nên lý do #3 nguyên vẹn.

> **Đã xét lại React và giữ nguyên Angular — 2026-07-25.**
>
> Câu hỏi frontend đã lật ba lần (Q14 Angular → 19/07 Next.js+React → D4 Angular), nên lần
> này xét kỹ rồi ghi lại kết quả để **không mở lại lần thứ tư**.
>
> Đề xuất được xem xét: thay Angular bằng React + Vite, lý do *"cảm giác React nhanh hơn"*.
> **Kết luận: giữ Angular.** Cái "nhanh hơn" của React là thật nhưng nằm ở ecosystem và trợ lực
> AI, không phải ở bản thân framework — còn hai lợi thế kỹ thuật thường được viện dẫn thì
> **không áp dụng trong bài toán này**:
>
> | Lợi thế của React | Có áp dụng ở đây? |
> |---|---|
> | Bundle nhẹ, cold start nhanh | **Không.** Trang nhạy cold start là `/q`, đã Thymeleaf. `/staff` và `/admin` nằm sau đăng nhập, dùng lặp lại hàng ngày, cài như PWA — bundle không mua được gì |
> | Đơn giản hơn, ít bề mặt tư duy | **Một phần.** Nhưng đổi lại phải tự lắp router / data fetching / forms / PWA plugin; Angular CLI cho sẵn |
> | Ecosystem rộng, AI hỗ trợ tốt hơn | **Có** — đây là lợi thế thật duy nhất, và là cái đã bị đánh đổi |
>
> Ba lý do quyết định giữ:
> 1. **Angular là stack đi làm của người xây** *(Q14)* — năng suất, quy ước, tooling đã có sẵn.
>    Đây là sản phẩm sống nhiều năm do một người bảo trì, không phải hackathon.
> 2. **Phần khó nhất của frontend không phụ thuộc framework.** Offline outbox + sync là code tự
>    viết ở cả hai. React không giảm được một dòng nào của rủi ro lớn nhất.
> 3. **`/staff` cần PWA, và Angular cho sẵn.** `ng add @angular/pwa` sinh manifest + service
>    worker + `ngsw-config.json` khai báo caching bằng JSON. Bên React phải tự lắp
>    `vite-plugin-pwa`. Đúng cái phần nặng nhất thì Angular đỡ việc hơn.
>
> **Tiêu chí chốt để dừng lật:** *stack đang thành thạo thắng, trừ khi có ràng buộc kỹ thuật
> cụ thể bắt phải đổi.* Đã kiểm ngày 25/07: **không có ràng buộc nào.** Lần sau muốn mở lại
> câu hỏi này thì phải nêu được một ràng buộc kỹ thuật, không phải một cảm giác.
>
> **Và dù chọn gì cũng không Next.js** — nó kéo theo một Node runtime phải vận hành, đúng thứ
> D4 vừa dọn đi. Angular/React ở đây đều là build ra file tĩnh cho Spring Boot serve.

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
checkino/
├── backend/                        # Spring Boot 4.1 + Java 25
│   ├── src/main/java/com/checkino/...
│   ├── src/main/resources/
│   │   ├── templates/q/            # Thymeleaf — /q/{code}
│   │   ├── db/migration/           # Flyway
│   │   └── static/app/             # ← bundle Angular copy vào lúc build
│   └── pom.xml
├── frontend/                       # Angular 22 → /staff + /admin
│   ├── src/
│   ├── ngsw-config.json            # cấu hình service worker (PWA cho /staff)
│   └── angular.json
├── docker-compose.yml              # postgres + app (+ caddy)
└── Dockerfile
```

Build: `ng build` → file tĩnh → copy vào `static/app/` → Maven đóng **một** jar → **một** image.
Có thể để `frontend-maven-plugin` chạy `ng build` trong vòng đời Maven, hoặc một script gọi tay —
với một người thì script đơn giản hơn, không cần plugin.

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
com.checkino
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

**Ai serve file Angular tĩnh.** Chọn **Spring Boot serve từ `static/`** — đúng một artifact để
deploy, để rollback, dev/prod đồng nhất. Cần một controller fallback trả `index.html` cho route
phía client (`/admin/members`). Caddy chỉ làm TLS. Phương án Caddy-serve-tĩnh-proxy-`/api` tốt
hơn về cache header nhưng thêm chỗ cấu hình phải đồng bộ — đổi sang sau là việc mười phút.

---

## D6 — Xếp hạng và trial pipeline thuộc free tier

**Quyết định.** Bảng xếp hạng chuyên cần tháng *(F7)* và danh sách học thử *(F4)* nằm ở **gói
free**. Gói Pro bán bằng bốn thứ khác: gỡ cap 50 hội viên, mirror Google Sheet, đa cơ sở +
phân quyền, và Zalo OA/ZNS.

Sửa lại bảng gói ở `PRD.md` §7, vốn đang xếp *rankings* và *trial pipeline* vào Pro trong khi
§4 F4/F7 mô tả chúng như tính năng lõi v1 — ba chỗ trong cùng một tài liệu nói ba kiểu.

**Lý do.**

1. **Xếp hạng chính là việc gig gốc thuê người làm** *(PRD §1)*. Free tier không có nó thì không
   phải "phiên bản rút gọn của sản phẩm", mà là một sản phẩm khác.
2. **D2 đứng được là nhờ xếp hạng tồn tại.** Lập luận bỏ OTP dựa trên câu *"hậu quả duy nhất của
   gian lận là lệch bảng xếp hạng"*. Bỏ xếp hạng khỏi free tier thì câu đó rỗng nghĩa ở đúng nơi
   phần lớn người dùng ở.
3. **F4 gánh đường vào roster phổ biến nhất** — một người mới lẻ tự đến *(bảng "bốn đường vào
   roster", PRD §4)*. Khoá nó sau tường trả tiền thì roster free chỉ lớn được bằng import file.
   Và F4 còn là chỗ hứng **cú quét thử của chính chủ** trước khi import *(F1)* — khoá nó là hỏng
   onboarding, tức hỏng north-star.

**Cap 50 hội viên đã là ranh giới kiếm tiền rồi**, không cần cắt thêm tính năng. Ranh giới đó
sạch: nó tăng theo giá trị khách nhận được, không chặn khách nhỏ dùng thử.

**Hệ quả.** Danh sách cắt khi trễ tiến độ ở `PLAN.md` §6 phải sửa: *"cắt F7 rankings"* nay là
cắt một tính năng free mà gig gốc đòi — không còn là món để cắt. Thứ tự cắt còn lại:
mirror Sheet → lớp GPS của F9. Và lưu ý cắt lớp GPS thì phí luôn spike GPS ở M0, vốn là mục duy
nhất còn lại của milestone đó.

---

## D7 — `program` (bộ môn) là bảng riêng, quan hệ nhiều–nhiều với hội viên

**Quyết định.** Tách bộ môn ra khỏi `scan_point`:

```sql
program        (id, org_id, name, active)
member_program (member_id, program_id)        -- nhiều–nhiều
scan_point     (..., program_id NULL)         -- NULL = QR dùng chung cho cả cơ sở
```

`scan_point` giữ nguyên nghĩa **"một mã QR ở một chỗ"**, thêm một tham chiếu bộ môn tuỳ chọn.

**Lý do.** `PLAN.md` §3.1 đang chú thích `scan_point` là *"location/program"* — gộp hai chiều
độc lập làm một. PRD lại dùng bộ môn như một chiều riêng ở bốn chỗ. Ba chỗ gãy:

| Chỗ gãy | Vì sao gộp thì hỏng |
|---|---|
| **Roster hôm nay của cô giáo** *(F3)* — chỗ đau thật | *"Lọc danh sách theo bộ môn"*, nhưng `member` không có liên kết nào tới bộ môn. Cô giáo lớp Yoga mở app ra thấy cả 200 hội viên của trung tâm, phải tự tìm 15 đứa của mình. Mà **lớp trẻ con là ca dùng chính của F3** |
| **Thẻ theo bộ môn** *(F5)* | PRD viết `scope: program(s) \| whole org`. Không có bảng thật để trỏ tới thì "thẻ Boxing không dùng được cho lớp Yoga" không kiểm được |
| **Xếp hạng theo bộ môn** *(F7)* | Thành xếp hạng theo QR. Trung tâm 3 cơ sở × 3 bộ môn = 9 mã QR; hỏi *"ai chăm nhất môn Boxing toàn hệ thống"* thì không trả lời được |

**Bộ môn là tuỳ chọn — đây là điều kiện để không phá north-star.** Wizard F1 giữ nguyên bốn bước
`org → điểm quét → roster → QR`; tạo bộ môn là bước **bỏ qua được**. Bỏ qua thì mọi thẻ và mọi
xếp hạng nằm ở phạm vi cả org, đúng như hiện tại. Trung tâm một bộ môn không phải học thêm khái
niệm nào; trung tâm nhiều bộ môn bật lên khi cần.

Cột "bộ môn" trong file import cũng tuỳ chọn. Tên bộ môn chưa tồn tại thì **màn hình preview của
F1 phải liệt kê ra** (*"sẽ tạo mới 2 bộ môn: Boxing, Yoga"*) — cùng tinh thần "xem trước rồi mới
ghi" của D2/F1, không tự tạo ngầm.

**Chi phí:** một bảng, một bảng nối, một màn hình CRUD nhỏ trong `/admin`, một bộ lọc ở `/staff`.
Không phải một hệ thống.

---

## D8 — Một số điện thoại = một hội viên

**Quyết định.** `UNIQUE (org_id, phone_normalized)` trên `member`. Không có hội viên nào không có
số điện thoại. Trẻ con dùng số phụ huynh, và **mỗi đứa con cần một số riêng**.

**Lý do.** SĐT đã là khoá định danh từ D2 — khoá của import upsert *(F1)*, của tra cứu ở `/q`
*(F2)*, và là thứ F11 phải sửa được vì sai một chữ số là hội viên vĩnh viễn không vào được. Cho
phép một số trỏ tới nhiều người thì cả ba chỗ đó đều phải sinh thêm nhánh: import không biết
đang cập nhật ai, `/q` không biết trả về ai.

**Giới hạn đã chấp nhận — ghi ra để sau không ai báo là bug:**

1. **Phụ huynh hai con phải có hai số.** Số thứ hai thường có sẵn (số của người còn lại trong gia
   đình). Ca này ít xảy ra, và chủ trung tâm xử lý được ngay ở F11 mà không cần chờ tính năng.
2. **Không hỗ trợ hội viên không có SĐT.** Bỏ cụm *"no-phone members"* khỏi `PRD.md` F3 — nó
   mâu thuẫn trực tiếp với D2. Lưu ý điều này **không** cản lớp trẻ con: ở `/staff` cô giáo tap
   theo **tên**, không hề đụng tới số điện thoại. SĐT chỉ cần lúc import và lúc tự quét ở `/q`.

**Hệ quả cần vá.** Preview của F1 phải báo trùng SĐT **trong file** như một lỗi và bỏ qua dòng đó
*(PRD F1 đã viết)*; F11 sửa SĐT phải kiểm trùng và từ chối nếu số đã thuộc về người khác.

**Đường thoát nếu về sau thấy đau** — ghi lại để khỏi nghĩ lại từ đầu: đổi khoá upsert thành
**(SĐT + tên chuẩn hoá)**, và thêm ở `/q` một bước *"Bạn là ai?"* khi một số khớp nhiều người.
`/staff` không phải đổi gì. Chỉ làm khi có khách hàng thật kêu, không làm trước.

---

## D9 — Bỏ fallback "mã 6 số" ở `/q`

**Quyết định.** Xoá câu *"Network failure fallback: the page shows a 6-digit code for staff to key
in manually"* khỏi `PRD.md` F2. Thay bằng: **mạng hỏng thì nhờ nhân viên điểm danh hộ qua
`/staff`** *(F3)* — đường đó chạy được cả khi offline.

**Lý do.** Ba chỗ hở, cả ba là hệ quả của quyết định đã chốt ở nơi khác:

1. **Không mạng thì `/q` không mở được, nên không có mã nào để hiện.** `/q` không có service
   worker *(D4)* → không cache gì → mất mạng là trang lỗi. Cái fallback chỉ đỡ được đúng một khe
   hẹp: trang đã tải xong, mạng mới chết, hội viên chưa kịp bấm. Trường hợp phổ biến hơn — sóng
   yếu ngay từ đầu, quét QR mà trang không mở — nó không đỡ được gì.
2. **Không có đường kiểm mã.** Nhân viên gõ mã vào `/staff`, mà `/staff` lúc đó cũng có thể đang
   offline (đó là cả lý do nó có service worker). Vậy mã phải kiểm được **không cần server**, tức
   phải suy ra từ danh sách đã cache. Mã suy ra từ hội viên thì nó chỉ là cái tên viết bằng số.
3. **`/staff` đã làm đúng việc này, ít thao tác hơn.** Nhân viên gõ tên hoặc SĐT vào ô tìm, tap
   một cái — offline vẫn chạy, đã có trong F3, đã nằm ở M2. Mã 6 số bắt hội viên đọc sáu chữ số
   cho nhân viên gõ lại: nhiều bước hơn, dễ nghe nhầm hơn, và cần thêm bảng lưu mã + quy tắc kiểm
   offline + ô nhập mã.

**Nói thẳng chỗ đau còn lại:** `/q` mất mạng thì **trang không mở được**, và không có cơ chế nào
trong tầm v1 cứu được điều đó. Đường thoát duy nhất là con người — nhân viên với `/staff`.

---

## D10 — `audit_log` thuộc v1, không phải "grow later"

**Quyết định.** Chuyển `audit_log` từ nhóm *Grow later* *(`PLAN.md` §3.1)* vào **schema lõi**.
Bảng dựng ở **M1** cùng schema nền; mỗi tính năng có thao tác sửa dữ liệu thì ghi log ngay khi
làm tính năng đó — F1 import *(M1)*, F11 *(M2)*.

Ghi tối thiểu: `(id, org_id, actor_staff_user_id, action, entity_type, entity_id, summary,
created_at)`. Trường `summary` giữ dạng người đọc được (*"import: thêm 12, cập nhật 3"*) để chủ
tra được mà không cần công cụ.

**Lý do.**

1. **Lập luận loại Sheet-làm-roster đang dựa vào nó.** `PRD.md` §4 out of scope viết: không để
   roster trong *"một file ai có link cũng sửa được và không có audit trail"*. Câu đó ngầm khẳng
   định app **có** nhật ký. Với `audit_log` ở "grow later" thì v1 cũng không có, và lý do tự sụp
   — trong khi **kết luận vẫn đúng**. Đưa `audit_log` vào v1 làm lý do đó thành thật.
2. **Roster là dữ liệu định danh từ D2.** Sửa roster = đổi ai được vào cửa. Thao tác có sức nặng
   như vậy mà không để lại vết là thiếu sót, không phải tối giản.
3. **Trả lời được khiếu nại.** *"Sao thẻ tôi bị trừ buổi"*, *"ai cho tôi nghỉ"*, *"ai đổi số của
   tôi"* — chủ có chỗ tra thay vì đoán. Với `/admin` nhiều người dùng chung (F8: Owner / Manager /
   Staff) thì đây là thứ sớm muộn cũng cần.

**Chi phí:** một bảng + một lượt ghi ở mỗi thao tác sửa, ~nửa đến một ngày.

**Dù vậy, vẫn viết lại lý do out-of-scope cho mạnh hơn** — đừng để nó đứng một chân trên
audit trail, vì Google Sheet cũng có version history (yếu, nhưng khác không). Ba lý do đúng ngay
ở v1 và mạnh hơn hẳn: **(a) không phân quyền** — ai có link là sửa được, còn app có vai trò *(F8)*
và RLS *(cơ chế 2)*, mà sửa được roster nghĩa là **tự thêm số của mình vào = check-in miễn phí**;
**(b) không có ràng buộc dữ liệu** — Sheet không có `UNIQUE (org_id, phone_normalized)` *(D8)*,
không có màn hình preview trước khi ghi *(F1)*; **(c) xoá không phục hồi được** — đúng cái cửa mà
quy tắc *"import không bao giờ xoá"* đã đóng lại. Audit trail là lý do thứ tư, không phải lý do
thứ nhất.

---

## D11 — Tên thương hiệu: Checkino

**Quyết định.** Brand + repo + package dùng chung một tên: **Checkino**. Thay placeholder
*CheckinHub* và thư mục `manage-pwa`. Một token duy nhất ở mọi chỗ kỹ thuật: repo `checkino`,
package `com.checkino`, artifactId/image Docker/database `checkino`. Chốt 2026-07-26 — **trước
Bước 6**, vì sau bước đó tên nằm trong package Java, artifactId Maven, tên image và tên database:
đổi bây giờ là sửa ~12 dòng tài liệu, đổi sau là refactor thật.

**Tiêu chí** (suy từ PRD §1, §5 + một bổ sung 26/07):

1. Chủ trung tâm nghe tên trong một comment FB phải **gõ lại đúng**. Kênh tăng trưởng duy nhất
   là post trong group FB — "nghe→gõ đúng" quý hơn "dễ bảo hộ thương hiệu". (Đã vặn lại một ý:
   không tên tiếng Anh nào khớp được cụm tìm kiếm "app điểm danh" — cụm đó thuộc về title
   landing page và caption bài post, không thuộc về brand.)
2. Không gym-only, không school-only — đa vertical là luận điểm sản phẩm *(PRD §5)*.
3. Không đặt tên theo công nghệ (không "PWA", không "app", không "hub").
4. Gọi được ít nhất việc số 1: **điểm danh** (việc số 2 là thẻ hội viên).
5. Đứng được như tên *nền tảng*, không chỉ tên chức năng — hướng mở rộng đã định: trang public
   cho người ngoài xem lớp/chương trình nào đang mở, xem thông tin chung, rồi tham gia.

**Vì sao Checkino.**

- "Check-in" là từ tiếng Anh đã Việt hoá hoàn toàn — nghe một lần hiểu ngay app làm gì
  *(tiêu chí 4)*, gõ đúng vì ngày nào cũng gõ nó trên FB *(tiêu chí 1)*.
- Trung tính vertical: "check-in buổi tập / buổi học / buổi gia sư" đều tự nhiên *(tiêu chí 2)*.
- Một token sạch: `com.checkino` hợp lệ, không hyphen, không lẫn sang từ khác.
- Hậu tố "-o" cùng văn phạm với các app Việt đã quen tai (Zalo, Momo, Sendo) — đứng được như
  tên nền tảng *(tiêu chí 5)*.
- Kiểm chứng search 2026-07-26: **không thấy sản phẩm/công ty nào tên Checkino.** Gần như mọi
  tên "hiển nhiên" khác đều đã có chủ đúng ngành liền kề (bảng dưới).

**Điểm yếu đã chấp nhận.** Vần "-ino" nghe thoáng giống *casino* — chấp nhận, đổi lấy độ sạch.
**ACheckin** (Appota — chấm công nhân sự, acheckin.vn) cùng gốc "checkin" ở VN nhưng khác phân
khúc (HRM nội bộ vs hội viên trung tâm) và khác cấu trúc tên; chung gốc là chấp nhận được, vì
chính gốc đó làm tên tự giải thích.

**Các tên đã loại** (đều kiểm bằng search 2026-07-26):

| Tên | Vì sao loại |
|---|---|
| CheckinHub (placeholder cũ) | "hub" là từ công nghệ — phạm tiêu chí 3; là tên giữ chỗ ngay từ đầu |
| **Onlist** (á quân) | Sạch trên internet; ẩn dụ "list" phủ cả ba việc (có tên trong list hôm nay = điểm danh, list hội viên = thẻ, list lớp public = mở rộng). Thua vì nghĩa điểm danh gián tiếp hơn "checkin" một nhịp, và nhìn hơi gần chữ "online" |
| CheckinClub | CHECK IN CLUB (Trainerize) đã ở trên Google Play đúng ngành fitness; dài 11 ký tự; "club" lệch với gia sư 1-1 |
| Tickin | tickin.app là phần mềm điểm danh đang hoạt động — trùng thẳng category; nghe→gõ vỡ (tick/tik, đứng cạnh Tiki) |
| GoClass · Joinly | Gọi tính năng mở rộng chứ không gọi điểm danh — phạm tiêu chí 4; cả hai đều đã có sản phẩm cùng tên (goclass.com; Joinly — phần mềm quản lý hội viên CLB) |
| ComeIn | "May I come in?" đẹp cảm xúc, nhưng Comeen (workplace platform Pháp) gần trùng âm; cụm quá generic để search ra mình |
| Fullhouse | Full House Active đã làm đúng hướng mở rộng (nền tảng book lớp fitness/nghệ thuật); thêm FullHouse Software (property mgmt, từ 1978) |
| TapIn | Nhiều sản phẩm TapIn check-in/điểm danh đang sống — trùng thẳng category |
| Traino | App fitness cùng tên đang sống; "train" lệch fitness, không gọi được điểm danh |
| CheckinViet | Đụng văn hoá "Check in Vietnam" du lịch — search sẽ ngập nội dung du lịch |
| Diemdanh | Yêu cầu là tên tiếng Anh; `diemdanh.vn` nếu trống vẫn đáng mua làm redirect/SEO |

**Domain — chưa xác nhận** (không kiểm được whois từ môi trường làm việc): Thang tự kiểm
`checkino.vn` / `checkino.com` / `checkino.app` trước khi mua. Tài liệu này **không** khẳng định
domain nào còn trống.

---

## D12 — Hướng thiết kế và hệ màu

**Chốt 2026-08-02, sau khi dựng xong bề mặt `/q` (`designs/q.dc.html`).**

Hướng thiết kế: **Bản 2 · Bảng điều khiển** (`docs/design/ban-2-operational.html`). Chữ Archivo,
khối bo tròn lớn, màu bão hoà dùng tiết chế trên nền than.

Luật thi hành đầy đủ nằm ở [`docs/design/prompts/00-he-thong.md`](docs/design/prompts/00-he-thong.md)
— § A1 bốn tầng hộp, § A1b ba slot đầu màn, § A2 màu, § A3 chữ. Mục này chỉ ghi **quyết định và
lý do**, không chép lại đặc tả.

### Bảy token, mỗi màu một nghĩa

`--c-ink` `#131413` · `--c-coral` `#F4573F` · `--c-rust` `#8E2C1B` · `--c-purple` `#6F66F0` ·
`--c-yellow` `#F3C24A` · `--c-sage` `#C4D4C1` · `--c-sky` `#A9E5F1` (+ `--c-dim` `#9AA096` cho
chữ phụ).

> **Cập nhật (D15):** nghĩa chi tiết của từng token giờ do
> [`docs/DESIGN.md § 2`](docs/DESIGN.md) giữ. Ba chỗ `00-he-thong.md § A2` để mang hai nghĩa đã
> chốt ở đó: **vàng** chỉ còn "cần xử lý, còn kịp" (bỏ "thành tích"); **sage** chỉ còn "xong
> xuôi"; **tím** = "con số về người (sĩ số + sức chứa)". Xem D15.

**`--c-rust` là token thêm mới.** Bảng màu ban đầu không có màu nào mang nghĩa "bị chặn", nên
vàng phải gánh cả *"sắp hết hạn"* lẫn *"đã hết hạn"* — hậu quả là hai màn khác hẳn nhau về
nghiệp vụ lại trông na ná. Rust là san hô nung tối, cùng gia đình nên không phá bảng màu, và đủ
tối để không vi phạm luật "màu bão hoà không bao giờ tràn kín màn hình".

> **Vàng = vẫn vào được, nhưng phải xử lý sớm. Rust = hôm nay không vào được.
> San hô không bao giờ là lỗi — nó là nhịp của hiện tại.**

Kèm theo: **nền màn chỉ có ba giá trị** — than (mặc định) · sage (xong xuôi) · rust (bị chặn).
Rust **chỉ** làm nền màn, không bao giờ làm khối.

### Ba slot đầu màn

Header bar = **nơi chốn** (logo + tên tổ chức + pill cơ sở), đứng yên tuyệt đối qua mọi màn.
Nhãn nhỏ = **tên người**. Hero 36px = **câu trạng thái**.

Lý do: trước khi chốt, tên tổ chức xuất hiện ở ba vị trí khác nhau tuỳ màn, và slot nhãn nhỏ khi
thì mang tên tổ chức khi thì mang tên hội viên. Một slot mang hai nghĩa là nguồn gốc của cảm giác
"đẹp nhưng không đồng nhất" — nó không sửa được bằng cách chỉnh lề.

### Đã loại

- **Phương án 1A cho màn nhập SĐT** (nền than + khối san hô chứa ô nhập). Giữ 1B: nửa dưới sáng
  dễ đọc ngoài nắng, và tấm sáng chiếm phần dưới thì ô nhập nằm đúng tầm ngón cái.
- **Gradient bắc cầu nhiều họ màu** (thẻ hội viên từng là vàng→san hô→đỏ). Gradient chỉ được đi
  trong một họ; bắc cầu là trộn hai nghĩa vào một vật và vật đó hết đọc được. Thẻ hội viên mang
  **hạn mức** → họ tím, `#9E98F6 → #6F66F0`.
- **Xanh băng làm nút.** Nó chỉ mang nghĩa "đã chọn / đã tick". Nút chính: nền sáng → nút than;
  nền than hoặc rust → nút kem.
- **Dropdown chọn cơ sở ở `/q`.** Hội viên quét QR của một `scan_point` cụ thể — cơ sở đã biết
  trước. Đa cơ sở là gói Pro ([D6](#d6--xếp-hạng-và-trial-pipeline-thuộc-free-tier)).
- **Gạch ngang số buổi trên thẻ hết hạn.** Chip chữ `HẾT HẠN · 28/07` đã đủ. Đây là mặt còn lại
  của luật "màu không bao giờ là tín hiệu duy nhất": khi **chữ** đã nói rồi thì tín hiệu thứ hai
  chỉ làm rối. Kiểm bằng cách chụp màn hình ở chế độ đen trắng — đọc được là đạt.

### Chưa làm

`design/tokens.css`, `docs/DESIGN.md`, `docs/design/styleguide.html` và skill `/design-screen`
viết **sau khi đủ bốn bản dựng** và sau khi gom bốn báo cáo `*-A8.md`. Đã có ba bản dựng
(`/q`, `/staff`, `/admin` nền tảng) và hai báo cáo (`staff-A8.md`, `admin-A8.md` — `q-A8.md` chưa
từng được viết dù `01-q-hoi-vien.md:7` có hứa). Viết bây giờ gần như chắc chắn phải viết lại.

### Hai luật treo từ lượt `/staff` — **đã chốt sau lượt `/admin`**

Hai luật này cố ý hoãn ở `staff-A8.md:50` để "quyết một lần cho cả hệ". Lượt `03a` đã dựng thử cả
hai cách rồi chốt (chi tiết + bằng chứng ở `docs/design/admin-A8.md § 0a` và `§ 0b`):

- **Hero không đổi theo bộ lọc.** Con số đổi vì **dữ liệu** thì lên hero; con số đổi vì người dùng
  **vừa bấm bộ lọc** thì sống ở dòng kết quả cạnh bộ lọc, kèm đủ mẫu số. Lý do loại cách cũ: hero
  36px nhảy mà thứ duy nhất giải thích là eyebrow 10.5px `opacity:.62` — thành phần mờ nhất đỡ
  nghĩa cho thành phần to nhất. Đã viết vào `00-he-thong.md § A1b`.
  **Ripple:** `designs/staff.dc.html` vẫn làm theo cách cũ, cần một lượt sửa riêng.
- **Nhãn nhỏ rỗng chỉ có một nghĩa — "chưa biết là ai" — và nghĩa đó chỉ tồn tại ở `/q`.** Ở
  `/staff` và `/admin` eyebrow luôn có chữ, nên nghĩa thứ hai ("không có tập nội dung để mô tả")
  không cần tồn tại. `min-height` cố định vẫn giữ, nhưng vì lý do bố cục.

Kèm theo, `00-he-thong.md § A6` được sửa: bản dựng `.dc.html` **miễn** luật khai token trên `:root`.
Cả ba bản dựng đều vi phạm luật cũ vì định dạng này viết style nội tuyến — sửa luật cho khớp thực
tế thay vì sửa ba file đã đóng. Token sống ở `design/tokens.css` (`admin-A8.md § 2.1`).

---

## D13 — `/admin` trên màn rộng là **một cột căn giữa**, không bố cục lại

**Chốt 2026-08-05, sau khi dựng xong `/admin` nền tảng (`designs/admin.dc.html`, 15 màn).**

### Bối cảnh — vì sao câu hỏi này chưa từng được hỏi

Ba lượt dựng đầu đều mặc định điện thoại: `03a-admin-nen-tang.md:18` ghi chủ trung tâm "xem phần
mềm này **trên điện thoại**, giữa hai ca", `:23` ra lệnh "đừng thiết kế cho màn hình máy tính rồi
thu nhỏ", và `00-he-thong.md § A4` chỉ nói "mọi màn phải đọc được ở **360px**". Khung 390×844 trong
các bản dựng là **mặt bàn để xem**, không phải một bố cục desktop.

Nhưng cho tới trước quyết định này, cả `DECISIONS.md`, `PRD.md` lẫn `docs/STORIES.yml` **không có
một chữ nào** về desktop, responsive hay breakpoint. Nghĩa là hình dáng trên laptop đang là một
giả định không ai ký, và lúc code `/admin` bằng Angular thì mỗi story sẽ tự quyết một kiểu.

### Quyết định

Ở bề rộng **≥ 720px**, `/admin` **không bố cục lại**: một cột `max-width: 440px` căn giữa trên nền
than `#0B0C0B`, nội dung y hệt màn điện thoại. Không breakpoint nào khác, không bố cục hai cột,
không bảng ngang. Áp cho `/staff` luôn — cùng lý do, và `/staff` còn ít lý do lên laptop hơn.

`/q` không cần luật này: nó là trang Thymeleaf một màn, quét từ điện thoại.

### Lý do

1. **North-star là 10 phút trên điện thoại** (`03a:20`, `PRD.md:20`). Bố cục desktop không rút ngắn
   được 10 phút đó — nó chỉ thêm một hệ luật thứ hai phải giữ đồng bộ.
2. **Hệ thiết kế xây quanh 360–390px.** Bốn tầng hộp, ba slot đầu màn, thanh nav nổi, vùng chạm
   52px — tất cả đều là luật của một cột hẹp. Bố cục hai cột không kế thừa được cái nào trong đó
   mà không phải viết lại luật.
3. **Một người xây, part-time.** Backlog M0→M3 đã là 43 ngày-người. Hai bố cục là hai bộ lỗi hiển
   thị, hai lượt kiểm bằng mắt, hai chỗ để lệch.

### Rủi ro đang nhận, nói thẳng

Hai việc trong `/admin` thật sự là **việc của laptop**, và cả hai vẫn phải làm trong cột 440px:

- **Nhập danh sách từ Excel** (`M1-S09`, wizard bước 4). File `.xlsx` thường nằm trên máy tính, và
  chọn file trên điện thoại là bước dễ bỏ cuộc nhất của cả wizard.
- **Tải và in poster QR A4** (wizard bước 5). Máy in gần như luôn nối với máy tính.

Chấp nhận cho v1 vì cả hai vẫn **làm được** trong cột hẹp — bản dựng đã có luồng dán-từ-Excel làm
đường thoát cho việc thứ nhất, và PDF tải trên điện thoại rồi gửi sang máy in được.

**Điều kiện mở lại:** nếu pilot (`M4-S13`) báo về rằng chủ trung tâm bỏ dở ở bước 4 hoặc bước 5 vì
đang ngồi máy tính, thì mở lại bằng một **lượt dựng 03c** cho đúng hai màn đó — không sửa ngầm
trong lúc code, không thêm breakpoint lẻ tẻ vào từng component.

### Cái này **không** phải

Đây không phải quyết định về PWA. `/admin` vẫn chỉ có **manifest cho icon**, không service worker —
xem bảng ở mục `Chữ "PWA" chỉ áp cho /staff` bên dưới. Một cột căn giữa là chuyện bố cục, không đổi
gì về cài đặt hay offline.

Luật thi hành nằm ở `docs/design/prompts/00-he-thong.md § A4`.

---

## D14 — Giao diện **song ngữ vi/en**, mặc định tiếng Việt

**Chốt 2026-08-07, sau khi cả bốn bản dựng UI đã xong và viết đủ bốn báo cáo `*-A8.md`.**

### Bối cảnh — quyết định này sinh ra từ một chỗ tự quyết của bản dựng

Cả ba bản dựng đều đã mang sẵn một lớp dịch hai ngôn ngữ và nút đổi EN/VI: `q.dc.html` (05/08, mặc
định `en`), `staff.dc.html` (mặc định `vi`), và `admin.dc.html` (thêm 07/08). **Không prompt nào
yêu cầu bản tiếng Anh** — `00-he-thong.md:164` chỉ dùng tiếng Anh làm mốc "chữ Việt dài hơn ~15%,
đừng thiết kế vừa khít chữ Anh rồi vỡ khi dịch", tức tiền đề là thiết kế bằng tiếng Việt. Lớp song
ngữ là **quyết định tự phát của Claude Design**, ghi lại ở `q-A8.md § 4.1`.

Ba mặc định lệch nhau (q=en, staff=vi, admin mới thêm) nghĩa là nó đang là một giả định không ai ký.
Quyết định này ký nó — chọn giữ, không gỡ — và làm ba mặc định nhất quán.

### Quyết định

Giao diện **song ngữ Việt/Anh ở cả ba bề mặt** (`/q`, `/staff`, `/admin`). **Mặc định tiếng Việt**
ở cả ba; người dùng đổi sang tiếng Anh được, và lựa chọn được **nhớ lại**:

- `/admin`, `/staff` (Angular SPA): lưu ở `localStorage`.
- `/q` (Thymeleaf server-render): cookie ngôn ngữ (khác cookie device token của D3), đọc lúc render.
  Không có cookie → mặc định `vi`. Không suy từ `Accept-Language` — chủ dự án chọn "mặc định vi,
  người dùng đổi", không "theo trình duyệt".

Chỉ **hai** ngôn ngữ, không dựng khung i18n nhiều-locale (ICU, số nhiều, RTL…). Copy sống thành hai
bộ khoá song song, như `DICT` trong các bản dựng.

### Lý do

**Đặt nền cho mở rộng thị trường sau** — không phải một nhu cầu khách hàng v1 đã xác nhận. Đây là
một **cược có chủ ý về hướng đi**, nói thẳng để không nhầm với nhu cầu đã kiểm chứng:

1. Lớp dịch **đã tồn tại** trong cả ba bản dựng. Gỡ ra cũng là việc, và sẽ phải làm lại nếu cược
   đúng. Giữ thì rẻ hơn miễn là kỷ luật hai-bộ-khoá được giữ từ đầu.
2. Kiến trúc rẻ nhất để "để ngỏ" cửa quốc tế: tách chuỗi ra khỏi template ngay từ M2 tốn ít hơn
   nhiều so với đi bóc chuỗi cứng khắp codebase sau này.
3. Không đội chi phí biến đổi (ràng buộc xuyên suốt): dịch là chuỗi tĩnh, không gọi dịch vụ ngoài.

### Chi phí và rủi ro — nói thẳng, vì nó căng với D13 lý do #3

**Một người xây, part-time.** Song ngữ nghĩa là **mọi chuỗi hiển thị phải duy trì hai bản** và giữ
đồng bộ mãi mãi — đúng loại "hai bộ để lệch" mà D13 đã từ chối cho bố cục desktop. Chấp nhận ở đây
vì chi phí là **tuyến tính theo số chuỗi** (dịch một lần, sửa khi đổi copy), không phải hai hệ luật
render như bố cục; và vì cược để-ngỏ-thị-trường được cho là đáng. Nếu tới M4 mà không có tín hiệu
thị trường nào ngoài VN, **được phép rút gọn về vi-only** — bỏ bộ khoá `en`, giữ khung tách-chuỗi.

Ai dịch: **chủ dự án tự dịch** bản en (không thuê), vì đằng nào cũng cần đọc để đúng giọng sản phẩm.

### Ripple sang các việc chưa làm

- `design/tokens.css` không đổi (token là màu/chữ, không phải nội dung).
- `docs/DESIGN.md` + `styleguide.html` phải kiểm nhãn/nút ở **bản dài hơn trong hai ngôn ngữ** (thường
  là tiếng Việt, theo `00 § A3`), và ghi rõ toggle EN/VI trong bản dựng là **thật**, không phải
  scaffold — khác với bảng nhảy trạng thái và khung máy iOS.
- Các story M2+ đụng copy phải tách chuỗi ra khỏi template ngay, không gõ cứng tiếng Việt.

Chưa lộ story mới ngay; khi tới M2 render các màn Angular thì gắn `QUYẾT ĐỊNH CẦN CHỐT` nếu cần chốt
cơ chế lưu ngôn ngữ ở tầng code.

---

## D15 — `docs/DESIGN.md` là nguồn sự thật của hệ thiết kế; prompt build-time thành lịch sử

**Chốt 2026-08-07, sau khi viết đủ bốn báo cáo `*-A8.md` và gom chúng vào `docs/DESIGN.md`.**

### Bối cảnh — một cam kết build-time hết hạn

Trong lúc dựng bốn lượt UI, luật chung sống ở `docs/design/prompts/00-he-thong.md` với cam kết
"nguồn sự thật **duy nhất** của luật chung, không chép sang file nào khác — sửa một chỗ là cả
bốn lượt đổi theo". Cam kết đó đúng **lúc dựng**: bốn phiên Claude Design chạy độc lập, cần một
file luật không được phân mảnh.

Nhưng `00` là **prompt build-time**. `README.md § "Sau khi có bốn bản dựng"` đã định sẵn: thứ
giữ lại cho code sản phẩm là `design/tokens.css` + **`docs/DESIGN.md`** (luật bốn tầng, nghĩa
màu, đặc tả từng màn) + `styleguide.html` + skill `/design-screen`. Bốn lượt dựng nay đã xong.

### Quyết định

**`docs/DESIGN.md` là nguồn sự thật của hệ thiết kế cho giai đoạn code sản phẩm.** Nó hấp thụ
toàn bộ luật chung của `00-he-thong.md` (bốn tầng hộp, ba slot đầu màn, token màu, thang chữ,
kích thước) cộng đặc tả từng màn ba bề mặt. Khi hình dáng/nghĩa UI lệch nhau, DESIGN.md thắng.

`00-he-thong.md` và các prompt `01/02/03*` chuyển thành **lịch sử build-time đã đóng vai trò** —
mỗi file thêm một header ngắn trỏ về DESIGN.md. Giữ nội dung làm dấu vết, không đọc để làm việc.

### Bốn chỗ nhập nhằng đã chốt khi hấp thụ luật chung

Bốn báo cáo A8 dồn lại các chỗ `00`/bản dựng tự nới nghĩa hoặc để treo. DESIGN.md chốt (chi tiết
+ ripple ở [`docs/DESIGN.md § 2` và `§ 5`](docs/DESIGN.md)):

1. **Vàng một nghĩa** — "cần xử lý, còn kịp". Bỏ "thành tích". Quán quân xếp hạng dùng **icon
   huy chương + số hạng** trên nền than, không dùng khối vàng.
2. **Sage một nghĩa** — "xong xuôi". Cái "đang diễn ra / là hôm nay" thuộc **coral**. Cột "hôm
   nay" của biểu đồ trang chủ `/admin` phải đổi sage → coral.
3. **Tím = "con số về người"** — phủ cả sĩ số trong ngày ("24 người đã tới") lẫn con số có trần
   (cap 50, số buổi). Bản dựng giữ nguyên.
4. **Header avatar = logo/monogram của trung tâm** cho cả ba bề mặt (header = nơi chốn). Bỏ
   emoji 🏋️ (đọc nhầm thành bộ môn) ở `/q` và `/staff`.

### Ripple

- `q.dc.html` (cột không có; avatar 🏋️), `staff.dc.html` (avatar 🏋️; hero còn đổi theo bộ lọc),
  `admin.dc.html` (cột hôm nay sage; quán quân khối vàng) cần một **lượt sửa bản dựng riêng** —
  không sửa trong lượt viết DESIGN.md.
- `design/tokens.css`, `styleguide.html`, skill `/design-screen` viết sau, dựa vào DESIGN.md.
- `D12` được ghi chú: nghĩa màu chi tiết giờ do DESIGN.md giữ.

### Cái này **không** phải

Không đảo bất kỳ quyết định thiết kế nào của D12–D14 — DESIGN.md thi hành chúng. Đây chỉ là dời
**nơi giữ** luật chung từ prompt build-time sang spec sản phẩm, đúng như `README.md` đã định.

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
phải làm. Cộng thêm: `ng add @angular/pwa` sinh manifest + `ngsw-worker.js` + `ngsw-config.json`
(~½ ngày) + lưu danh sách vào IndexedDB thay vì RAM (~½ ngày) + đường khởi-động-không-mạng
(~½ ngày) + ~1 ngày vật lộn với hai nỗi đau kinh điển (deploy rồi người dùng vẫn thấy bản cũ vì
service worker cũ còn phục vụ từ cache; và debug ma quái vì tưởng code mới mà đang chạy bản cache).

Của Angular thì caching khai báo bằng JSON, không phải viết tay: `assetGroups` với
`installMode: prefetch` cho vỏ app, `dataGroups` cho API. Và **đừng để nó tự đổi bản ngầm** —
dùng service `SwUpdate` (observable `versionUpdates`) để hiện thanh *"có bản mới, tải lại"*,
vì đổi ngầm giữa lúc giáo viên đang điểm danh là mất dữ liệu trong RAM.

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
2. ~~**Tên thương hiệu & domain** — "CheckinHub" chỉ là placeholder.~~ → **Đã chốt: Checkino**
   ([D11](#d11--tên-thương-hiệu-checkino)). Chỉ còn phần domain: chờ whois + mua tay
   (`checkino.vn` / `.com` / `.app`).
3. **Độ chính xác GPS** tại cửa hàng thật (trong nhà) — quyết định ngưỡng bán kính cho lớp
   soft-check chống gian lận.
4. **Phân quyền staff theo bộ môn** — có nên khoá một staff chỉ thấy/điểm danh đúng một (vài)
   bộ môn không. **Chưa quyết, hoãn có chủ đích.** Trục phân quyền mà spec đã chọn là *theo cơ
   sở* (`F8`: Owner / Manager per location / Staff), và cả cụm "đa cơ sở + phân quyền" là tính
   năng **Pro**, sequenced về M4 — ở M1 chỉ có "roles skeleton" (một cột `role`), nên schema
   cũng chưa có liên kết `staff_user ↔ location`, không riêng gì `program`.
   - **Ca "một staff một lớp" v1 xử lý bằng bộ lọc, không bằng quyền:** `/staff` lọc roster
     theo bộ môn (`F3`, đọc `member_program` — [D7](#d7--program-bộ-môn-là-bảng-riêng-quan-hệ-nhiều-nhiều-với-hội-viên)).
     Là tiện lợi, **không** phải rào quyền — staff đổi bộ lọc vẫn xem lớp khác. Với trung tâm
     nhỏ ≤50 hội viên, mức đe doạ thấp; RLS ([cơ chế 2](#ba-cơ-chế-kỹ-thuật-đã-chốt)) là để chặn
     rò **chéo org**, không phải trong nội bộ org.
   - **Nếu về sau quyết là cần cứng:** thêm bảng nối `staff_program (staff_user_id, program_id)`
     — rẻ, y hệt `member_program`. Chi phí thật là **tầng thực thi** (mỗi query `/staff` lọc theo
     program được phép) + UI gán quyền ở `/admin` — đúng loại việc của cụm roles Pro (M4). Build
     thì build cùng cả cụm, đừng nhỏ giọt một bảng lẻ vào M1.
   - Tín hiệu để mở lại: pilot ([M4-S13](docs/STORIES.yml)) có khách kêu cần khoá cứng.
