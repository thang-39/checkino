# Kế hoạch — viết lại PRD + PLAN lên v2

Tài liệu thi hành. Quyết định nền nằm ở [`../DECISIONS.md`](../DECISIONS.md) — đọc file đó trước.

| | |
|---|---|
| Tạo | 2026-07-25 |
| Trạng thái | Bước 0 ✅ xong · Bước 1–4 ⬜ chưa làm |

---

## Tiến độ

| Bước | Việc | Trạng thái |
|---|---|---|
| 0 | `git init`, snapshot v1, tạo `DECISIONS.md` + `CLAUDE.md` + file này | ✅ 2026-07-25 |
| 1 | Viết lại `PRD.md` → v2.0 | ⬜ |
| 2 | Viết lại `PLAN.md` → v2.0 | ⬜ |
| 3 | Thêm ghi chú superseded vào `GRILL-LOG.md` | ⬜ |
| 4 | Đối chiếu chéo PRD ↔ PLAN | ⬜ |

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
| **§1** | Giữ kết luận PWA. Bổ sung: `/q/{code}` **không phải SPA** — server-render để tối ưu cold start. *(D4)* |
| **§2 Stack** | Viết lại toàn bộ theo D4: Spring Boot 3.5 + Postgres 16 + Flyway + Angular 20 PWA + Thymeleaf. Nêu lý do lật quyết định. Giữ exit door: logic ở tầng app, `pg_dump` sang Postgres VN-region cho PDPL. Deploy Fly.io hoặc VPS + docker compose + Caddy, **không k8s** |
| **§3 Domain model** | Bổ sung bảng: `member_device`, `staff_user` (email auth), `notification_outbox`. Ghi rõ 3 cơ chế kỹ thuật trong `DECISIONS.md` |
| **§4 Milestones** | Sắp xếp lại — xem bảng dưới |
| **§6 Risk register** | Bỏ rủi ro "Supabase/PDPL". Thêm: gian lận khi không OTP; Angular PWA offline queue là phần frontend nặng nhất |
| **§7 Definition of done** | Bỏ "nhận digest Zalo" khỏi tiêu chí v1 — nó đã là tính năng Pro |

**Milestone mới** — Zalo không còn chặn đường:

| M | Nội dung | Exit criteria |
|---|---|---|
| **M0** (~2 ngày) | Chốt design partner #0. Test độ chính xác GPS tại cửa hàng thật. **Bỏ Zalo spike khỏi đường tới hạn** | Có design partner |
| **M1** | Spring Boot + Postgres + Flyway; RLS + **bộ test cô lập đa tenant**; email magic link cho chủ/nhân viên; F1 tạo scan point + xuất QR PDF; F2 import danh sách + bind device + trang `/q` Thymeleaf; dashboard live qua SSE | Demo end-to-end trên điện thoại thật tại cửa thật |
| **M2** | F5 entitlement + consume policy; dedupe race-safe (unique index + Testcontainers test); F9 giờ mở cửa + GPS mềm + cờ bất thường; F3 roster PWA offline + idempotency key; F4 trial pipeline | Toàn bộ workflow của gig FB gốc chạy được, **không cần Zalo** |
| **M3** | F7 xếp hạng tháng, thẻ sắp hết hạn, mirror Google Sheet (ghi theo batch), CSV | Design partner #0 dùng thật |
| **M4** | Billing (VietQR + xác nhận tay); trang PDPL; **rồi mới** Zalo OA/ZNS như tính năng Pro; onboard 5–10 pilot | Có khách trả tiền đầu tiên |

## Bước 3 — `GRILL-LOG.md`, chỉ thêm ghi chú

Giữ nguyên giá trị lịch sử, làm đúng cách Q14 đã được xử lý:

- **Q6** — thêm `> Superseded 2026-07-25:` Zalo rời khỏi lõi v1, lý do GPKD + không free tier
- **Q14** — thêm ghi chú lật lại lần hai về Spring Boot, kèm lý do (Supabase auth advantage bốc hơi)
- Thêm **Q16** mới: *"Bạn lấy tiền đâu trả OTP khi chưa có doanh thu?"* — ghi lại phép tính
  (device token: 1.000 hội viên = 300k một lần, so với login mỗi lần: 3,6tr/tháng) và kết luận D2

## Bước 4 — kiểm tra tính nhất quán

Không có code nên không chạy test được. Kiểm tra bằng tính nhất quán tài liệu:

1. **Rà mạch lạc** — `rg --color=never --no-heading -i "supabase|next\.js|OTP" *.md` → mọi kết quả
   còn lại phải nằm trong ngữ cảnh "đã bị thay thế" hoặc "tính năng Pro", không còn ở lõi v1
2. **Kiểm chứng north-star** — đọc lại luồng F1→F2 trong PRD, xác nhận không còn bước nào cần
   GPKD, cần OA, hoặc cần trả tiền → mục tiêu "< 10 phút không cần gặp người" thật sự đạt được
3. **Kiểm chứng chi phí** — liệt kê mọi chi phí biến đổi của một khách free tier, tổng phải bằng **0đ**
4. **Đối chiếu chéo** — mọi feature F1–F10 trong PRD phải xuất hiện ở đúng một milestone trong PLAN,
   không sót không trùng
5. `git log` hiển thị đủ các bước, `git show <commit-snapshot>:PRD.md` xem lại được bản v1

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

Nhớ cập nhật bảng **Tiến độ** ở đầu file này sau mỗi bước.

---

## Từ bước 5 trở đi — bắt đầu code

Chạy `/init` sau khi có code để `CLAUDE.md` được bổ sung lệnh build/test/run.

Thứ tự đề xuất, mỗi session một việc:

1. Khởi tạo Spring Boot + Postgres + Flyway + docker compose
2. Schema `org` / `scan_point` / `member` + RLS + **bộ test cô lập đa tenant** — làm trước, không làm sau
3. Email magic link cho chủ/nhân viên *(F10)*
4. Trang `/q/{code}` Thymeleaf + import danh sách + bind device token *(F2)*
5. `entitlement` + `checkin_event` + dedupe unique index + Testcontainers test *(F5)*
6. Angular PWA cho `/staff` với offline queue *(F3)*
