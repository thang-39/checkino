# 03a · `/admin` — chủ trung tâm, phần nền tảng

> **Đọc trước, theo đúng thứ tự:**
> 1. `docs/design/prompts/00-he-thong.md` — luật chung, bắt buộc, áp cho mọi thứ dưới đây
> 2. `docs/design/ban-2-operational.html` — hướng thiết kế đã chốt; hai màn
>    `/admin · trang chủ` và `/admin · xếp hạng tháng` trong đó là điểm xuất phát
>
> **Ghi ra:** `designs/admin.dc.html` · **Báo cáo tự kiểm:** `docs/design/admin-A8.md`
>
> Lượt `03b` sẽ mở rộng **chính file này** ở một phiên khác. Nên đặt tên biến, tên class và cấu
> trúc dữ liệu cho rõ ràng, có chú thích chỗ nào để thêm màn mới.

---

## B0. Người dùng và hoàn cảnh

Chủ một trung tâm nhỏ ở Việt Nam. Không phải dân kỹ thuật. Thường tự dạy hoặc tự trực quầy, xem
phần mềm này **trên điện thoại**, giữa hai ca. Trước đây quản lý bằng sổ giấy hoặc Google Sheet.

Điều duy nhất quyết định họ có ở lại hay không: **từ lúc mở tài khoản tới lúc quét thử thành công
phải dưới 10 phút.** Mọi màn trong lượt này phục vụ mục tiêu đó.

Đừng thiết kế cho màn hình máy tính rồi thu nhỏ. Điện thoại là chính.

## B1. Wizard mở tài khoản — 5 bước, thứ tự cố định

Thứ tự này chịu lực, không được đổi:

1. **Thông tin trung tâm** — tên, địa chỉ
2. **Điểm quét** — nơi dán mã QR (ví dụ "Cửa chính"), kèm **giờ mở cửa**
3. **Bộ môn** — *bỏ qua được bằng một cú bấm*
4. **Nhập danh sách hội viên** — bắt buộc, xem B2
5. **Poster QR** — xem trước và tải PDF A4

Ba điều quan trọng:

- Bước 3 phải có nút **"Bỏ qua"** rõ ràng, **ngang hàng** với nút tiếp tục, không giấu dưới chân
  trang. Bỏ qua rồi thì **không màn nào sau đó được đòi bộ môn**. Đây là đường mặc định, không
  phải ngoại lệ.
- Bước 4 phải nói rõ vì sao **bắt buộc**: danh sách rỗng thì không ai điểm danh được, và cú quét
  thử của chính chủ sẽ rơi vào form học thử khiến họ tưởng phần mềm hỏng.
- Có thanh tiến trình 5 bước, quay lại bước trước được.

## B2. Màn nhập danh sách — màn khó nhất của lượt này

Luồng: chọn file hoặc dán từ Excel → **xem trước** → xác nhận → mới ghi.

Màn xem trước phải hiện đủ bốn thứ:

- **Tổng kết**: "12 mới · 3 cập nhật · 185 không đổi". Ba con số, ba ô riêng.
- **Danh sách dòng**, có nhãn phân biệt dòng nào mới, dòng nào cập nhật.
- **Dòng lỗi liệt kê riêng**, nói rõ lỗi gì: số điện thoại không đọc được, trùng trong cùng file.
  **Không được im lặng bỏ qua.**
- **Bộ môn mới sẽ tạo**: "Sẽ tạo 2 bộ môn mới: Boxing, Yoga" — chỉ tạo khi bấm xác nhận.

Nói rõ trên màn: **nhập danh sách không xoá ai cả.** Hội viên vắng mặt trong file để nguyên.

Cần cả trạng thái file hỏng hoàn toàn (không đọc được cột nào) — nói **cách sửa**, không chỉ báo lỗi.

## B3. Danh sách màn phải dựng

| # | Màn | Yêu cầu nội dung |
|---|---|---|
| 1 | Trang chủ | ô số hôm nay, cảnh báo cần xử lý, luồng người đang vào cập nhật trực tiếp |
| 2 | Wizard bước 1 · trung tâm | tên, địa chỉ |
| 3 | Wizard bước 2 · điểm quét | tên điểm quét, giờ mở cửa, hiện mã QR sẽ sinh ra |
| 4 | Wizard bước 3 · bộ môn | thêm nhiều bộ môn, và nút **Bỏ qua** ngang hàng nút tiếp tục |
| 5 | Wizard bước 4 · chọn nguồn | chọn file hoặc dán từ Excel, kèm lời giải thích vì sao bắt buộc |
| 6 | Xem trước nhập liệu | xem B2 |
| 7 | Xem trước — có dòng lỗi | biến thể của #6, phần lỗi nổi rõ |
| 8 | Xem trước — file hỏng hoàn toàn | không đọc được cột nào; nói cách sửa |
| 9 | Nhập xong | xác nhận + đường dẫn sang bước 5 |
| 10 | Wizard bước 5 · poster QR | xem trước poster A4, nút tải PDF, gợi ý in và dán ở đâu |
| 11 | Danh sách hội viên | tìm theo tên hoặc SĐT, chip lọc theo bộ môn và trạng thái |
| 12 | Danh sách hội viên — rỗng | chưa nhập ai → chỉ đường sang màn nhập |
| 13 | Chi tiết hội viên | thông tin, thẻ hiện tại, lịch sử điểm danh, các thao tác ở B4 |

## B4. Các thao tác trên hồ sơ hội viên

Từ màn chi tiết phải làm được: gán gói mới · gia hạn · tạm dừng thẻ · sửa số điện thoại ·
thu hồi thiết bị đã ghi nhớ · ngưng hội viên · sửa bộ môn.

Hai điều:

- Sửa số điện thoại trùng với hội viên khác → **báo rõ trùng với ai**, không nuốt lỗi im lặng.
- Mọi thao tác trong nhóm này **đều ghi nhật ký**. Trên UI thể hiện bằng một dòng nhỏ:
  "Thao tác này được ghi lại". Màn đọc nhật ký thuộc lượt `03b`.

## B5. Dữ liệu giả

Ít nhất **20 hội viên**, 3 bộ môn, 2 điểm quét. Để trong một object JS ở đầu file, đặt tên rõ ràng
— lượt `03b` sẽ dùng lại chính object đó và thêm dữ liệu học thử, xếp hạng, nhật ký vào.

## B6. Chữ nghĩa

Gọi người dùng là **"anh/chị"** hoặc dùng câu không xưng hô. Được phép dùng từ nghiệp vụ quen
thuộc với người kinh doanh (gói, thẻ, gia hạn, hội viên), nhưng **không** dùng từ kỹ thuật:
không "đồng bộ", không "bản ghi", không "token", không "API".
