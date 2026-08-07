# Bốn lượt dựng UI

Dùng để nhờ một phiên Claude khác dựng bản mẫu UI tương tác từ hướng thiết kế đã chốt
(**Bản 2 · Bảng điều khiển**, `../ban-2-operational.html`).

## Chạy thế nào

Mỗi lượt là **một phiên riêng**. Mỗi lượt đọc `00-he-thong.md` trước, rồi đọc file của lượt đó.

| Lượt | Nói với Claude | Bề mặt | Số màn | Ghi ra |
|---|---|---|---|---|
| 1 | ~~đọc `00-he-thong.md` rồi làm `01-q-hoi-vien.md`~~ | `/q` | 11 trạng thái | `designs/q.dc.html` — **xong** |
| 2 | ~~đọc `00-he-thong.md` rồi làm `02-staff-co-giao.md`~~ | `/staff` | 14 trạng thái | `designs/staff.dc.html` — **đóng** (`../staff-A8.md`) |
| 3 | ~~đọc `00-he-thong.md` rồi làm `03a-admin-nen-tang.md`~~ | `/admin` | 13 màn → dựng 15 | `designs/admin.dc.html` — **đóng** (`../admin-A8.md`), 7 việc treo đã sửa |
| 4 | đọc `00-he-thong.md` rồi làm `03b-admin-bao-cao.md` | `/admin` | 11 màn | mở rộng `designs/admin.dc.html` |

Bản dựng làm bằng **Claude Design**, ra file `.dc.html` ở `designs/`. File trỏ tới `./support.js`
(runtime của Claude Design, **đã commit** từ `e0fc80e`). Mở bản dựng phải **phục vụ qua HTTP cục
bộ** — runtime `fetch` lại chính file, mở thẳng `file://` sẽ báo lỗi — và **cần mạng** cho React +
Archivo. `docs/design/proto/` là dự tính cũ, bỏ trống.

Lượt 1 đã chạy xong và đã qua bốn đợt sửa. Những gì nó chốt ra đã được nâng lên `00-he-thong.md`
(§ A1b ba slot đầu màn, § A2 token `--c-rust` + sáu luật màu, § A3 hero 36px) và `DECISIONS.md`
**D12**. Ba lượt còn lại thừa hưởng tự động vì đều đọc `00` trước.

Lượt 2 đóng ở `../staff-A8.md`, để lại hai luật treo có chủ ý. Lượt 3 đã trả cả hai (hero không đổi
theo bộ lọc; nhãn nhỏ rỗng chỉ có một nghĩa và chỉ tồn tại ở `/q`) — nâng lên `00-he-thong.md § A1b`,
kèm `§ A4` một cột 440px và `§ A6` miễn luật `:root`, và `DECISIONS.md` **D13**.

Lượt 3a đã **đóng**: bốn lỗi số liệu (`§ 6`), ba việc render (R1–R3) và câu hỏi luật `§ 6.4` đều đã
sửa và kiểm bằng render headless; luật vùng chạm nâng lên `§ A4` (52px chính / 44px nút phụ `/admin`).

**Trước khi chạy lượt 4**, đọc `../admin-A8.md § Việc để lại` — hai mục còn mở áp cho `03b`: viết lại
`§ B0b` theo ranh giới "màn nói về một người", và hoà số màn (`03b` đánh số từ 14 nhưng bản dựng đã
dùng 14/15 cho `phoneEdit`/`pause`).

**`00-he-thong.md` là nguồn sự thật duy nhất của phần luật chung.** Nội dung của nó không được
chép sang file nào khác — sửa một chỗ là cả bốn lượt đổi theo.

Mỗi file `01` / `02` / `03a` / `03b` chỉ chứa phần riêng của bề mặt đó: người dùng là ai, hoàn
cảnh dùng ra sao, danh sách trạng thái phải dựng, và giọng chữ.

### Vì sao `/admin` tách hai lượt

23 màn, nhiều gấp đôi hai bề mặt kia. Dồn một lượt thì nửa sau chắc chắn nông.

Vì `03b` chạy ở **phiên riêng**, nó không nhớ gì từ `03a`. Nên `03b` được viết để **đọc lại**
`designs/admin.dc.html` và mở rộng chính file đó — không viết lại từ đầu, không đổi tên biến, không
tạo file mới. Chạy `03a` trước, xong mới chạy `03b`.

## Vì sao prompt dài như vậy

Vấn đề gặp phải khi thử ngắn hơn: kết quả **đẹp nhưng không đồng nhất**. Nguyên nhân không phải
thiếu ảnh mẫu — mà là ảnh mẫu không mang theo **luật**. Có màu và có bo góc thì mỗi màn hình lại
tự quyết cách lồng hộp một kiểu.

Nên bộ prompt mang theo năm thứ mà một ảnh không nói được:

1. **Luật bốn tầng hộp** (`00` § A1) — màn hình → khối → thẻ con → nhãn, mỗi tầng cố định nền,
   bo góc, padding, kèm bốn điều cấm. Đây là phần chữa đúng bệnh "không đồng nhất".
2. **Nghĩa cố định của từng màu** (`00` § A2) — san hô là nhịp của hiện tại, tím là hạn mức và
   sĩ số, vàng là "còn kịp, phải xử lý sớm", rust là "hôm nay không vào được", xanh rêu là xong
   xuôi, xanh băng là đã chọn. Màu không được chọn cho đẹp.
3. **Ba slot đầu màn** (`00` § A1b) — header là nơi chốn và đứng yên mọi màn, nhãn nhỏ là tên
   người, hero 36px là câu trạng thái. Một slot mang hai nghĩa là gốc của cảm giác lệch, và nó
   không sửa được bằng cách chỉnh lề.
4. **Danh sách trạng thái đầy đủ** (Phần B của từng file) — gồm cả các trạng thái xấu: rỗng, lỗi,
   mất mạng, hết hạn, ngoài giờ, chạm trần, file nhập hỏng. Không liệt kê ra thì chỉ nhận về màn
   đẹp nhất.
5. **Ràng buộc nghiệp vụ** (`00` § A5) — ví dụ `/staff` không bao giờ hiện số điện thoại, `/q`
   không có mã 6 số dự phòng. Vi phạm mấy cái này là sai sản phẩm, không phải sai thẩm mỹ.

Mục **A8** trong `00` bắt mỗi lượt tự kiểm và ghi báo cáo ra `../<tên>-A8.md`. Ý **A8.4** —
"chỗ nào bạn phải tự quyết vì prompt không nói rõ" — là phần đáng đọc nhất: đó chính là danh sách
lỗ hổng của tài liệu thiết kế, và sẽ được dùng để viết `docs/DESIGN.md`.

## Ba cái bẫy đã biết

- **Google Fonts có thể bị chặn** trong môi trường chạy sandbox hoặc artifact. `00` § A3 đã dặn:
  chặn thì lùi về `system-ui`, và **cấm** thay bằng Poppins hay Outfit — hai font đó không có bộ
  dấu tiếng Việt, chữ "ế ộ ữ" sẽ vỡ. Font đúng là **Archivo** (miễn phí, có khối `vietnamese`,
  có trục width 75–125).
- **Prompt dài có thể bị đọc lướt.** Nếu bản dựng bỏ sót nhiều trạng thái, đừng viết lại prompt —
  cứ hỏi tiếp trong cùng phiên: "Trạng thái 9, 11, 13 chưa có, dựng nốt."
- **Sửa cũng nên chia lượt.** Lượt 1 phải sửa bốn đợt sau khi dựng xong, và cách chạy được là
  chia thành bốn tin nhắn ngắn — hệ màu, ba slot đầu màn, gộp màn, thẻ hội viên — dán lần lượt
  trong cùng phiên. Một tin nhắn dài gộp cả bốn thì phần cuối bị làm hời hợt.

## Sau khi có bốn bản dựng

Bản dựng là **để xem và sửa**, không phải code sản phẩm. Thứ được giữ lại:

- `design/tokens.css` — token đã chốt, CSS thuần, dùng chung cho Thymeleaf ở `/q` và Angular ở
  `/admin` + `/staff`
- `docs/DESIGN.md` — luật bốn tầng hộp, nghĩa của màu, đặc tả từng màn
- `docs/design/styleguide.html` — trang render mọi component, sống lâu dài
- ~~một mục **D12** trong `DECISIONS.md`~~ — **đã viết** sau lượt 1, vì hệ màu và ba slot đầu màn
  phải chốt trước khi ba lượt sau chạy, không đợi được tới cuối
- skill `/design-screen` để lần sau chỉ cần gõ `/design-screen M2-S01`

Gom cả bốn file `*-A8.md` lại trước khi viết `DESIGN.md` — viết trước khi đọc chúng thì gần như
chắc chắn phải viết lại.
