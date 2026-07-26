# Kế hoạch — viết lại PRD + PLAN lên v2

Tài liệu thi hành. Quyết định nền nằm ở [`../DECISIONS.md`](../DECISIONS.md) — đọc file đó trước.

| | |
|---|---|
| Tạo | 2026-07-25 |
| Trạng thái | Bước 0–4 ✅ xong · Bước 5 ⬜ chưa làm (vá 14 mục lệch + D6–D10 vào PRD/PLAN) |

---

## Tiến độ

| Bước | Việc | Trạng thái |
|---|---|---|
| 0 | `git init`, snapshot v1, tạo `DECISIONS.md` + `CLAUDE.md` + file này | ✅ 2026-07-25 |
| 1 | Viết lại `PRD.md` → v2.0 | ✅ 2026-07-25 |
| 1.5 | Vá `PRD.md` → v2.1 — 6 lỗ roster-as-identity (F11, import upsert, hướng ghi Sheet, giới hạn offline) | ✅ 2026-07-25 |
| 2 | Viết lại `PLAN.md` → v2.0 | ✅ 2026-07-25 |
| 3 | Thêm ghi chú superseded vào `GRILL-LOG.md` (Q6, Q13, Q14) + thêm Q16 | ✅ 2026-07-25 |
| 4 | Đối chiếu chéo PRD ↔ PLAN — tìm ra 14 mục lệch, chốt D6/D7/D8 | ✅ 2026-07-26 |
| 5 | Vá 14 mục lệch + D6–D10 vào `PRD.md` (→ v2.2) và `PLAN.md` (→ v2.1) | ⬜ |

---

## Bước 1 — `PRD.md` lên v2.0

| Mục | Thay đổi |
|---|---|
| Header | Status → `Draft v2.0 — 2026-07-25, sau khi kiểm chứng chi phí Zalo/ZNS` |
| **§4 F2** | Bỏ OTP khỏi luồng v1. Luồng mới: chủ import danh sách hội viên → hội viên quét QR → nhập SĐT → khớp danh sách thì bind device token ngay → hiện trạng thái thẻ → check-in. Số lạ → chuyển sang F4. Ghi rõ OTP là nâng cấp gói Pro. *(D2, D3)* |
| **§4 F6** | Đổi tiêu đề thành *"Notifications (gói Pro)"*. Nêu rõ: **dashboard live real-time là kênh duy nhất của free tier**. Zalo OA/ZNS chỉ bật khi khách có OA xác thực. Đưa bảng giá ZNS thật vào. Giữ nguyên lập trường chỉ dùng API chính thức. *(D1)* |
| **§4** — thêm **F10** | Auth chủ/nhân viên: email + magic link. Tách bạch với định danh hội viên. *(D3)* |
| **§4** — out of scope | Thêm: OTP SMS/ZNS cho hội viên (v1); Zalo như tính năng bắt buộc |
| **§6** | Thêm NFR: xác thực danh tính hội viên ở v1 là "mềm" — bù bằng cờ bất thường + review thủ công. Nêu rõ ngưỡng rủi ro chấp nhận |
| **§7** | Free tier có **chi phí biến đổi = 0đ** → nuôi bao nhiêu khách free cũng không lỗ. Zalo/ZNS thuộc Pro. Nêu rào cản GPKD như một bước trong hành trình nâng cấp |
| **§9** | Hạ mức rủi ro Zalo (không còn trên đường tới hạn). Thêm rủi ro mới: gian lận điểm danh khi không có OTP |
| **§10** | **Đóng câu hỏi #1** — đã có câu trả lời (OA cần GPKD, ZNS 300đ/tin, không free tier). Chuyển thành phần "Đã kiểm chứng", trỏ sang `DECISIONS.md`. Giữ #2 (Sheets quota) và #3 (tên thương hiệu) |

## Bước 2 — `PLAN.md` lên v2.0

| Mục | Thay đổi |
|---|---|
| **§1** | Giữ kết luận web-không-native *(Q13)*, nhưng **sửa cách dùng chữ "PWA"** — nó chỉ áp cho `/staff` (manifest + service worker); `/q` là trang web thường; `/admin` chỉ cần manifest. Bổ sung: `/q/{code}` **không phải SPA** — 1 lượt request thay vì 4–5. *(D4)* |
| **§2 Stack** | Viết lại toàn bộ theo D4 + D5: Spring Boot 3.5 + Postgres 16 + Flyway + **Angular 20** + Thymeleaf. **Không Next.js, không SSR** — `ng build` ra file tĩnh, Spring Boot serve, cần controller fallback trả `index.html` cho route phía client. Nêu lý do lật Next.js+Supabase, **và ghi rằng React đã được xét lại ngày 25/07 rồi loại** — kèm tiêu chí đã chốt để dừng lật (stack thành thạo thắng, trừ khi có ràng buộc kỹ thuật cụ thể). Thêm mục **kiến trúc**: monorepo + modular monolith, một tiến trình, vì sao không microservices. Giữ exit door: logic ở tầng app, `pg_dump` sang Postgres VN-region cho PDPL. Deploy Fly.io hoặc VPS + docker compose + Caddy, **không k8s** |
| **§3 Domain model** | Bổ sung bảng: `member_device`, `staff_user` (email auth), `notification_outbox`. Ghi rõ 3 cơ chế kỹ thuật trong `DECISIONS.md`. Thêm layout package theo miền nghiệp vụ *(D5)* |
| **§4 Milestones** | Sắp xếp lại — xem bảng dưới |
| **§6 Risk register** | Bỏ rủi ro "Supabase/PDPL". Thêm: gian lận khi không OTP; **offline mức 2 của `/staff` là phần frontend nặng nhất** (~2–3 ngày cộng thêm, chưa kể ~½ ngày màn hình hướng dẫn cài trên iOS — bỏ bước đó thì offline vô dụng với người dùng iPhone) |
| **§7 Definition of done** | Bỏ "nhận digest Zalo" khỏi tiêu chí v1 — nó đã là tính năng Pro |

**Milestone mới** — Zalo không còn chặn đường:

| M | Nội dung | Exit criteria |
|---|---|---|
| **M0** (~2 ngày) | Chốt design partner #0. Test độ chính xác GPS tại cửa hàng thật. **Bỏ Zalo spike khỏi đường tới hạn** | Có design partner |
| **M1** | Spring Boot + Postgres + Flyway; RLS + **bộ test cô lập đa tenant**; email magic link cho chủ/nhân viên *(F10)*; F1 tạo scan point + xuất QR PDF; F1/F2 **import = upsert theo SĐT, không bao giờ xoá, có màn hình preview** ("thêm 12, cập nhật 3, giữ nguyên 185") + bind device token + trang `/q` Thymeleaf; dashboard live qua SSE | Demo end-to-end trên điện thoại thật tại cửa thật; import lại file cũ **không** làm mất ai |
| **M2** | F5 entitlement + consume policy; dedupe race-safe (unique index + Testcontainers test); F9 giờ mở cửa + GPS mềm + cờ bất thường; **F11 màn hình quản lý hội viên** (gán/gia hạn thẻ, convert lead, sửa SĐT, thu hồi device token, cho nghỉ); F3 roster + outbox IndexedDB + idempotency key — **chưa service worker**; F4 trial pipeline | Toàn bộ workflow của gig FB gốc chạy được, **không cần Zalo**. F11 đứng trước F5/F4 về mặt dùng được: không có nó thì gán thẻ và convert lead không có chỗ bấm |
| **M3** | **Offline mức 2 cho `/staff`** *(tách khỏi F3)*: `ng add @angular/pwa` + `ngsw-config.json` + cache danh sách vào IndexedDB + `SwUpdate` (không đổi bản ngầm) + **màn hình hướng dẫn cài trên iOS**. Rồi F7 xếp hạng tháng, thẻ sắp hết hạn, mirror Google Sheet một chiều (protected range + dòng cảnh báo, ghi theo batch), CSV | ① Bật chế độ máy bay → F5 trang → vẫn mở app và điểm danh được, có mạng lại thì sync đủ ② Design partner #0 dùng thật |
| **M4** | Billing (VietQR + xác nhận tay); trang PDPL; **rồi mới** Zalo OA/ZNS như tính năng Pro; onboard 5–10 pilot | Có khách trả tiền đầu tiên |

## Bước 3 — `GRILL-LOG.md`, chỉ thêm ghi chú

Giữ nguyên giá trị lịch sử, làm đúng cách Q14 đã được xử lý:

- **Q6** — thêm `> Superseded 2026-07-25:` Zalo rời khỏi lõi v1, lý do GPKD + không free tier
- **Q14** — thêm ghi chú lật lại về Spring Boot, kèm lý do (Supabase auth advantage bốc hơi).
  **Và ghi chú thứ hai:** ngày 25/07 đã xét lại React và **giữ Angular** — verdict gốc của Q14
  đứng vững, kèm tiêu chí đã chốt để dừng lật (stack thành thạo thắng, trừ khi có ràng buộc
  kỹ thuật cụ thể; muốn mở lại phải nêu ràng buộc, không phải cảm giác)
- **Q13** — thêm ghi chú: kết luận web-không-native **vẫn đúng**, nhưng chữ "PWA" trong câu trả lời
  bị dùng cho cả sản phẩm trong khi thực tế chỉ `/staff` cần manifest + service worker
- Thêm **Q16** mới: *"Bạn lấy tiền đâu trả OTP khi chưa có doanh thu?"* — ghi lại phép tính
  (device token: 1.000 hội viên = 300k một lần, so với login mỗi lần: 3,6tr/tháng) và kết luận D2

## Bước 4 — kiểm tra tính nhất quán

Không có code nên không chạy test được. Kiểm tra bằng tính nhất quán tài liệu.

> **6 lỗ nội dung đã đóng ở Bước 1.5** *(xem mục dưới)* — Bước 4 nay chỉ còn năm mục rà nhất quán
> sau đây, không phải sửa nội dung PRD nữa.

1. **Rà mạch lạc** — `rg --color=never --no-heading -i "supabase|next\.js|OTP" *.md` → mọi kết quả
   còn lại phải nằm trong ngữ cảnh "đã bị thay thế" hoặc "tính năng Pro", không còn ở lõi v1
2. **Kiểm chứng north-star** — đọc lại luồng F1→F2 trong PRD, xác nhận không còn bước nào cần
   GPKD, cần OA, hoặc cần trả tiền → mục tiêu "< 10 phút không cần gặp người" thật sự đạt được
3. **Kiểm chứng chi phí** — liệt kê mọi chi phí biến đổi của một khách free tier, tổng phải bằng **0đ**
4. **Đối chiếu chéo** — mọi feature **F1–F11** (F11 thêm ở v2.1) trong PRD phải xuất hiện ở đúng một
   milestone trong PLAN, không sót không trùng. Thêm: offline mức 2 đã tách khỏi F3 nên phải thấy nó
   ở M3, không lẫn trong M2
5. `git log` hiển thị đủ các bước, `git show <commit-snapshot>:PRD.md` xem lại được bản v1

### Kết quả chạy năm mục — 2026-07-26

| # | Mục | Kết quả |
|---|---|---|
| 1 | `rg "supabase\|next\.js\|OTP"` | ✅ mọi kết quả nằm trong ngữ cảnh "đã bị thay thế" hoặc "gói Pro" — trừ `PLAN.md:371`, xem lệch **E** |
| 2 | North-star F1→F2 | ✅ không còn bước nào cần GPKD, OA, hay trả tiền |
| 3 | Chi phí biến đổi free tier | ⚠️ 0đ ở mọi khoản trừ email magic link — xem lệch **J** |
| 4 | F1–F11 có mặt trong PLAN | ✅ đủ 11/11, không sót; có trùng **cố ý** nhưng PLAN khẳng định sai — xem lệch **F**. Offline mức 2 nằm ở M3, không lẫn vào M2 ✅ |
| 5 | `git log` + `git show 4d227dc:PRD.md` | ✅ 8 commit, bản v1 mở lại được |

### 14 mục lệch tìm được — chưa vá, để Bước 5

Ba mục nặng nhất đã được chốt thành quyết định trong `DECISIONS.md`: **D6** (A), **D7** (C),
**D8** (D). Mười một mục còn lại là sửa câu chữ, không cần quyết định thêm.

| # | Mục lệch | Ở đâu | Chốt |
|---|---|---|---|
| **A** | Xếp hạng + trial pipeline: §7 xếp vào Pro, §4 mô tả như lõi v1, PLAN không gate gì | PRD nội bộ + PLAN | **D6** — cả hai vào free |
| **C** | `program` (bộ môn) dùng như một chiều độc lập ở PRD (F1/F3/F5/F7) nhưng PLAN §3.1 gộp vào `scan_point` | PRD ↔ PLAN | **D7** — bảng riêng + `member_program` |
| **D** | F3 nói phục vụ *"no-phone members"* nhưng SĐT là khoá định danh | PRD nội bộ | **D8** — 1 SĐT = 1 người, bỏ cụm đó |
| **E** | Member OTP: PRD ghi out of scope v1, PLAN xếp vào M4 | PRD ↔ PLAN | Sửa PRD: OTP là **Pro trong v1**, không phải out of scope. Out of scope chỉ giữ "OTP ở free tier" |
| **J** | *"Unlimited free orgs never produces a loss"* đúng về đơn giá, sai về **hạn mức** free tier của Resend/Brevo (~3.000 email/tháng, 100/ngày) | PRD §7 | Sửa thành "0đ tới ngưỡng ~N org" + thêm mục vào §10.2 Còn bỏ ngỏ: **đo ngưỡng email** |
| **B** | Exit criteria M2 = *"toàn bộ workflow gig gốc chạy"*, nhưng gig đòi xếp hạng tháng — nằm ở M3 | PLAN | Sửa exit M2 thành "toàn bộ workflow **trừ báo cáo tháng**" |
| **F** | Câu *"Every PRD feature appears in exactly one milestone"* sai với chính bảng dưới nó (F3 ở M2+M3, F6 và F8 ở M1+M4) | PLAN §4 + file này, mục 4 | Việc tách là đúng; sửa câu thành "mỗi feature có một milestone chủ, phần Pro tách sang M4" |
| **G** | §7 nói *"Pro bật ngay trừ Zalo"*, nhưng bảng Pro có member OTP — cũng đi qua ZNS, cũng cần GPKD | PRD nội bộ | Sửa: **hai** thứ bị chặn sau GPKD |
| **H** | Danh sách cắt khi trễ: *Sheet mirror → GPS → rankings* — cắt đúng hai trong ba thứ bán Pro | PLAN §6 ↔ PRD §7 | Theo **D6**, rankings nay là tính năng free mà gig đòi → bỏ khỏi danh sách cắt |
| **I** | Fallback *"mã 6 số cho nhân viên nhập tay"* có ở PRD F2, không có ở milestone nào, không có bảng nào | PRD → PLAN | **D9** — bỏ; thay bằng nhờ nhân viên tap ở `/staff` |
| **K** | Tuần chồng nhau (M1 W1–3, M2 W3–5, M3 W5–7) và M4 nhồi billing + PDPL + Zalo + OTP + onboard 5–10 pilot vào ~1 tuần | PLAN §4 | Nhiều khả năng "~8 tuần" thực ra là tới hết M3 — nói rõ ra |
| **L** | Lý do loại Sheet-làm-roster là *"no audit trail"* (hàm ý app có), nhưng `audit_log` nằm ở "Grow later" | PRD §4 ↔ PLAN §3.1 | **D10** — kéo `audit_log` vào v1 (bảng ở M1), **và** viết lại lý do cho mạnh hơn |
| **M** | Cảnh báo hết hạn nằm ở cả F5 (M2) và F7 (M3) | PRD nội bộ | Nói rõ: cảnh báo lúc check-in ở M2, danh sách ở M3 |
| **N** | DoD đòi *"zero contact with the founder"* nhưng gồm mirror Sheet — Pro — mà Pro thanh toán VietQR **có admin xác nhận tay** | PLAN §7 | Tách DoD thành phần free (không contact) và phần Pro |

### Lỗ đã phát hiện — ✅ đã vá ở Bước 1.5 (2026-07-25)

Tìm ra khi rà câu hỏi *"chủ sửa Google Sheet thì backend có sync không?"* (25/07). Gốc chung:
**D2 biến roster từ dữ liệu báo cáo thành dữ liệu định danh**, nhưng F1/F5/F7 vẫn viết với giọng
của thời roster chỉ để xem.

Giữ nguyên phần mô tả dưới đây làm dấu vết lý do — **đã vá hết ở `PRD.md` v2.1**, không còn
thuộc phạm vi Bước 4.

1. ✅ *(→ PRD v2.1 §4 **F11**, feature riêng, không gộp vào F5)* **Thiếu màn hình quản lý hội viên
   trong PRD.** F1 chỉ nói "import", F5 nói gán thẻ nhưng không
   nói quản lý hội viên ở đâu. Bốn việc bắt buộc cần nó: gán/gia hạn thẻ *(F5)*, chuyển lead thành
   hội viên *(F4)*, sửa SĐT nhập sai (SĐT là khoá định danh — sai thì hội viên vĩnh viễn không vào
   được), thu hồi device token khi hội viên đổi máy *(F2)*. Đây là lỗ độc lập với chuyện Sheet.
2. ✅ *(→ PRD v2.1 §4 F1)* **F1 phải nói rõ import là việc làm nhiều lần, không phải một lần** — **upsert theo số điện
   thoại**. Hai ràng buộc đã chốt: (a) **import không bao giờ xoá** — file thiếu ai thì giữ nguyên
   người đó, vì tải nhầm file cũ sẽ xoá sạch roster và cả trung tâm không check-in được; cho nghỉ
   phải làm tay trong app; (b) **preview trước khi apply** — *"thêm 12, cập nhật 3, giữ nguyên 185"*
   → chủ xác nhận mới ghi.
3. ✅ *(→ PRD v2.1 §4 F7, mục "write-direction contract")* **F7 phải ghi hành vi khi chủ sửa Sheet** — hiện chỉ ghi "never the reverse" mà không nói chuyện
   gì xảy ra. Chốt: tab do app ghi bị **protected range** + một dòng cảnh báo ở đầu tab
   (*"Tab này do app ghi. Sửa tay sẽ bị ghi đè."*). Chủ **đọc ở Sheet, sửa ở app**. Không có tích
   hợp Sheets API ở chiều vào, không OAuth thêm, không sync ngầm.
4. ✅ *(→ PRD v2.1 §4 out of scope)* **Ghi vào out of scope:** sync hai chiều với Google Sheet; quản lý roster bằng cách gõ trực tiếp
   vào Sheet. Lý do: không đặt dữ liệu xác thực trong một file ai có link cũng sửa được và không có
   audit trail.
5. ✅ *(→ PRD v2.1 §4 F2 tiêu đề + khối "Terminology, fixed here once"; F3 nay là mặt tiền PWA duy nhất)* **`PRD.md` F2 viết `(PWA, no install)` — tự mâu thuẫn.** "PWA không cài" chính là *một website*.
   Chữ PWA ở đó không mang thông tin gì. Gốc: tài liệu dùng "PWA" cho **hai nghĩa** — (a) *"web chứ
   không phải native"* (quyết định Q13, áp cho cả sản phẩm) và (b) *"app cài được, chạy offline"*
   (yêu cầu kỹ thuật, **chỉ `/staff`**). Sửa F2 thành *"trang web, không cài"*, và chỉ dùng chữ PWA
   ở F3. Bảng ba mức theo mặt tiền ở `DECISIONS.md` D4.
6. ✅ *(→ PRD v2.1 §6, khối "Accepted offline limits — by design, not bugs")* **`PRD.md` §6 phải ghi ba giới hạn offline đã chấp nhận** *(cơ chế 3 trong `DECISIONS.md`)*:
   lần đầu buộc có mạng để cài service worker; danh sách cache có thể cũ; iOS không tự mời cài nên
   cần màn hình hướng dẫn. Cả ba là giới hạn có chủ đích, không phải bug — không ghi ra thì sau này
   sẽ bị báo là lỗi.

Bốn đường vào roster sau khi vá — không chồng chéo, và F4 gánh phần lớn *(đã đưa vào PRD v2.1
ngay sau F11)*:

| Tình huống | Đường | Chủ phải gõ |
|---|---|---|
| Ngày đầu, đã có sẵn Excel | Import file *(F1)* | không |
| Đầu khoá, thêm cả lớp | Import lại, upsert theo SĐT *(F1)* | không |
| Một người mới lẻ tự đến | **F4** — họ tự điền form học thử → chủ convert | một cú bấm *(F11)* |
| Sửa tên / đổi số / cho nghỉ | Màn hình quản lý hội viên *(**F11**)* | vài ô |

---

## Bước 5 — vá PRD + PLAN theo D6–D8 và 14 mục lệch

Không có quyết định nào còn treo; đây là việc thi hành.

**`PRD.md` → v2.2**

| Mục | Thay đổi | Nguồn |
|---|---|---|
| §7 bảng gói | Xếp hạng + trial pipeline chuyển sang **Free**. Pro còn: gỡ cap 50 hội viên, mirror Sheet, đa cơ sở + phân quyền, Zalo, member OTP | **D6** |
| §7 upgrade journey | *"Bật ngay trừ Zalo"* → **trừ Zalo và member OTP**, cả hai chặn sau GPKD | **G** |
| §7 chi phí 0đ | Ghi rõ hạn mức free tier của nhà cung cấp email; đổi *"unlimited free orgs"* thành "0đ tới ngưỡng ~N org" | **J** |
| §4 F1 | Thêm bước tạo bộ môn **tuỳ chọn, bỏ qua được**; cột bộ môn trong file import tuỳ chọn; preview liệt kê bộ môn sẽ tạo mới | **D7** |
| §4 F3 | Bỏ cụm *"no-phone members"*. Thêm một câu: ở `/staff` cô giáo tap theo **tên**, không đụng SĐT — nên lớp trẻ con không bị ảnh hưởng | **D8** |
| §4 F5 | Nói rõ cảnh báo hết hạn **lúc check-in** thuộc F5; **danh sách** thẻ sắp hết hạn thuộc F7 | **M** |
| §4 F2 | **Bỏ** fallback mã 6 số; thay bằng một câu chỉ sang F3 (nhân viên điểm danh hộ, chạy được offline) | **D9** |
| §4 out of scope | Bỏ *"SMS/ZNS OTP for members"* khỏi danh sách; nó là **tính năng Pro trong v1**, không phải out of scope. Viết lại lý do loại Sheet-làm-roster: dẫn đầu bằng **không phân quyền** → sửa roster = tự thêm mình vào; rồi **không có ràng buộc dữ liệu** (D8, preview F1); rồi **xoá không phục hồi**; audit trail là lý do thứ tư | **E**, **D10** |
| §6 | Thêm giới hạn đã chấp nhận thứ tư: một SĐT một người, phụ huynh hai con cần hai số | **D8** |
| §10.2 | Thêm câu hỏi mở: **ngưỡng email free tier** ở quy mô nhiều org free | **J** |
| §11 | Changelog v2.2 | |

**`PLAN.md` → v2.1**

| Mục | Thay đổi | Nguồn |
|---|---|---|
| §3.1 | Thêm `program`, `member_program`; `scan_point` thêm `program_id NULL`; `member` thêm `UNIQUE (org_id, phone_normalized)`; chuyển `audit_log` từ *Grow later* lên **Core — ship first** | **D7**, **D8**, **D10** |
| §3.3 | Bộ môn thuộc package nào — `org/` (cùng `scan_point`) hay `member/`. Nghiêng về `org/`. `audit_log` vào `shared/` | **D7**, **D10** |
| §4 M1 | Thêm bảng `program` + bước tạo bộ môn tuỳ chọn trong wizard; thêm bảng `audit_log` + F1 import ghi log lượt import | **D7**, **D10** |
| §4 M2 | Thêm bộ lọc bộ môn cho roster `/staff`; F11 ghi log mọi thao tác sửa (gán/gia hạn thẻ, sửa SĐT, thu hồi token, cho nghỉ); sửa exit criteria — *"trừ báo cáo tháng"* | **D7**, **D10**, **B** |
| §4 bảng map | Sửa câu *"exactly one milestone"*; ghi rõ F3/F6/F8 tách phần Pro sang M4 | **F** |
| §4 M4 | Member OTP giữ nguyên ở M4, nhưng ghi là **Pro trong v1** cho khớp PRD | **E** |
| §4 tuần | Nói rõ "~8 tuần" tính tới hết M3; M4 không nằm trong con số đó | **K** |
| §6 | Bỏ *rankings* khỏi danh sách cắt; ghi chú cắt lớp GPS thì phí spike GPS ở M0 | **D6**, **H** |
| §7 DoD | Tách phần free (thật sự không cần contact) khỏi phần Pro (VietQR xác nhận tay) | **N** |

Sau khi vá xong, chạy lại **năm mục kiểm tra** ở Bước 4 một lượt nữa.

---

## Cách prompt ở session mới

Mỗi session một bước. `CLAUDE.md` tự nạp nên không cần trỏ `DECISIONS.md` thủ công, nhưng
nhắc lại cho chắc cũng không hại gì.

**Bước 1:**
```
Đọc DECISIONS.md và docs/plan-v2-rewrite.md.
Làm Bước 1: viết lại PRD.md lên v2.0.
Không đụng PLAN.md và GRILL-LOG.md. Commit riêng.
```

**Bước 2:**
```
Đọc DECISIONS.md và docs/plan-v2-rewrite.md.
Làm Bước 2: viết lại PLAN.md lên v2.0.
Chú ý sắp xếp lại milestone — Zalo lùi xuống M4, M0 rút còn ~2 ngày.
PRD đã lên v2.1: có thêm F11 (quản lý hội viên) ở M2, và offline mức 2
đã tách khỏi F3 xuống đầu M3. Bám bảng milestone ở Bước 2.
```

**Bước 3:**
```
Đọc DECISIONS.md và docs/plan-v2-rewrite.md.
Làm Bước 3: thêm ghi chú superseded vào Q6, Q14 của GRILL-LOG.md và thêm Q16.
Giữ nguyên phần còn lại — đây là tài liệu lịch sử.
```

**Bước 4:**
```
Làm Bước 4 trong docs/plan-v2-rewrite.md: đối chiếu PRD.md và PLAN.md,
báo mọi chỗ mâu thuẫn. Đừng tự sửa, hỏi tôi trước.
```

**Bước 5:**
```
Đọc DECISIONS.md (chú ý D6–D10) và docs/plan-v2-rewrite.md.
Làm Bước 5: vá PRD.md lên v2.2 và PLAN.md lên v2.1 theo hai bảng ở mục Bước 5.
Không còn quyết định nào treo — cứ vá thẳng.
Chạy lại năm mục kiểm tra của Bước 4 sau khi vá. Hai commit riêng.
```

Nhớ cập nhật bảng **Tiến độ** ở đầu file này sau mỗi bước.

---

## Từ bước 6 trở đi — bắt đầu code

Chạy `/init` sau khi có code để `CLAUDE.md` được bổ sung lệnh build/test/run.

Thứ tự đề xuất, mỗi session một việc:

1. Khởi tạo monorepo: `backend/` Spring Boot + Postgres + Flyway + docker compose; `frontend/`
   Angular 20, `ng build` ra `static/app/` + controller fallback *(D5)*
2. Schema `org` / `scan_point` / `member` / `program` / `member_program` *(D7)*, `member` có
   `UNIQUE (org_id, phone_normalized)` *(D8)* + RLS + **bộ test cô lập đa tenant** — làm trước,
   không làm sau
3. Email magic link cho chủ/nhân viên *(F10)*
4. Trang `/q/{code}` Thymeleaf + import danh sách (upsert theo SĐT, có preview) + bind device token *(F2)*
5. `entitlement` + `checkin_event` + dedupe unique index + Testcontainers test *(F5)*
6. `/staff` Angular: danh sách + outbox IndexedDB + sync *(F3)*
7. `/staff` offline mức 2: `ng add @angular/pwa` + `ngsw-config.json` + cache danh sách + `SwUpdate`
   + màn hình hướng dẫn cài trên iOS *(cơ chế 3)* — tách khỏi bước 6 để bước 6 dùng được trước
   khi offline xong
