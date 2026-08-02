# 03b · `/admin` — chủ trung tâm, phần báo cáo và tiền

> **Đọc trước, theo đúng thứ tự:**
> 1. `docs/design/prompts/00-he-thong.md` — luật chung, bắt buộc
> 2. `docs/design/prompts/03a-admin-nen-tang.md` **§ B0b** — ánh xạ ba slot đầu màn riêng cho
>    `/admin` (nhãn nhỏ mang tên khu vực, hero là con số, nav ở đáy). Chỉ đọc mục đó là đủ
> 3. `designs/admin.dc.html` — **file do lượt `03a` sinh ra**. Đây là thứ bạn sẽ mở rộng
> 4. `docs/design/admin-A8.md` — báo cáo tự kiểm của lượt `03a`, để biết chỗ nào còn hở
>
> **Ghi ra:** mở rộng **chính** `designs/admin.dc.html` ·
> **Báo cáo tự kiểm:** thêm vào `docs/design/admin-A8.md`
>
> Lượt này chạy ở phiên riêng, **không có ngữ cảnh của `03a`**. Bắt buộc đọc lại file HTML đó,
> giữ nguyên token, tên class, cấu trúc dữ liệu và thanh điều hướng đã có. **Không viết lại từ
> đầu. Không đổi tên biến. Không tạo file mới.**

---

## B0. Đang tiếp tục việc gì

Lượt `03a` đã dựng phần nền tảng của `/admin`: trang chủ, wizard 5 bước mở tài khoản, nhập danh
sách, danh sách hội viên, chi tiết hội viên. Lượt này thêm phần **báo cáo, tiền, và các biên** —
những màn chủ trung tâm dùng sau khi đã chạy được vài tuần.

Trước khi thêm gì, hãy đọc `admin.dc.html` và liệt kê ra: bộ token đang dùng, các class component đã
có, object dữ liệu giả đang ở đâu, thanh điều hướng hoạt động thế nào. Rồi mới viết tiếp.

## B1. Danh sách màn phải thêm

| # | Màn | Yêu cầu nội dung |
|---|---|---|
| 14 | Xếp hạng tháng | chọn tháng, lọc theo bộ môn và cơ sở. Quán quân là khối vàng, dưới là bảng xếp hạng. Nút xuất CSV |
| 15 | Thẻ sắp hết hạn | ngưỡng cảnh báo đổi được (7 / 14 / 30 ngày). Mỗi hàng bấm sang chi tiết hội viên |
| 16 | Lịch sử một hội viên | phân trang, nhóm theo tháng |
| 17 | Danh sách học thử | trạng thái đã liên hệ / đã chuyển đổi / bỏ. Đổi trạng thái ngay trên hàng |
| 18 | Chuyển học thử thành hội viên | một cú bấm, không gõ lại thông tin. Trùng số điện thoại thì báo rõ trùng với ai |
| 19 | Tỉ lệ chuyển đổi | theo tháng và bộ môn |
| 20 | Bất thường chờ duyệt | các lượt điểm danh đáng ngờ, duyệt hoặc bỏ nhanh trước khi phát thưởng tháng |
| 21 | Nhật ký thao tác | ai làm gì lúc nào. Lọc theo người và theo loại. Xem B3 |
| 22 | Chạm trần 50 hội viên | chặn thêm hội viên, thông báo chỉ đúng hai đường. Xem B2 |
| 23 | Trang nâng gói | mã VietQR + mã trung tâm để ghi vào nội dung chuyển khoản. **Không** cổng thanh toán |
| 24 | Trạng thái chung | ba biến thể của một màn danh sách bất kỳ: đang tải, rỗng, lỗi mạng |

## B2. Màn chạm trần — chỗ duy nhất sản phẩm chặn người dùng

Chữ nghĩa phải cẩn thận. Không doạ, không bán hàng lộ liễu. Nói đúng tình trạng và hai lối ra:

> "Gói miễn phí dùng được 50 hội viên. Bạn đang có 50.
> Ngưng những người đã nghỉ để lấy chỗ, hoặc nâng gói để bỏ giới hạn."

Cả hai lối ra phải là **nút bấm được**, không phải chữ suông.

## B3. Màn nhật ký thao tác

Người đọc là chủ trung tâm, không phải kỹ sư. Viết như câu tiếng Việt, không như dòng log:

- Đúng: "Cô Lan sửa số điện thoại của Trần Quốc Bảo · hôm nay 14:32"
- Sai: `UPDATE member.phone id=4482 by user=3`

## B4. Dữ liệu giả cần thêm

Dùng lại object dữ liệu của `03a`, thêm vào: **5 lead học thử** ở đủ ba trạng thái, dữ liệu xếp
hạng 3 tháng gần nhất, **15 dòng nhật ký** đủ loại thao tác, vài lượt điểm danh bất thường.

## B5. Chữ nghĩa

Giữ nguyên giọng của `03a`: gọi **"anh/chị"** hoặc câu không xưng hô. Từ nghiệp vụ thì được
(gói, thẻ, gia hạn, hội viên), từ kỹ thuật thì không (không "đồng bộ", không "bản ghi",
không "token", không "API").
