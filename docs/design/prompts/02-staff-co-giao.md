# 02 · `/staff` — cô giáo và nhân viên

> **Đọc trước, theo đúng thứ tự:**
> 1. `docs/design/prompts/00-he-thong.md` — luật chung, bắt buộc, áp cho mọi thứ dưới đây
> 2. `docs/design/ban-2-operational.html` — hướng thiết kế đã chốt; màn
>    `/staff · cô giáo điểm danh` trong đó là điểm xuất phát
>
> **Ghi ra:** `designs/staff.dc.html` · **Báo cáo tự kiểm:** `docs/design/staff-A8.md`

---

## B0. Người dùng và hoàn cảnh — đọc kỹ, nó quyết định mọi thứ

Cô giáo đang **đứng giữa lớp**, lớp đã bắt đầu, học viên đang vào. Cầm điện thoại **một tay**,
tay kia đang làm việc khác. Nhìn xuống màn hình khoảng một giây mỗi lần, rồi ngẩng lên.

Hệ quả bắt buộc, ghi đè cả A4 trong file hệ thống:

- **Vùng chạm tối thiểu 56px** ở hàng danh sách, không phải 52px. Bấm nhầm giữa lớp thì không có
  thời gian sửa.
- **Bấm nhầm phải bỏ được ngay** bằng chính cú bấm thứ hai vào hàng đó. Không hộp thoại xác nhận,
  không menu.
- **Tên phải là thứ to nhất trong hàng.** Mọi thứ khác nhỏ hơn.
- **Không bao giờ hiện số điện thoại.**

Đây là **PWA cài lên màn hình chính**, chạy toàn màn hình, và **phải dùng được khi mất mạng**.
Mất mạng là chuyện thường: phòng tập tầng hầm, lớp học cuối hành lang.

## B1. Cơ chế offline — phần khó nhất, phải thể hiện được trong UI

Cách nó hoạt động:

1. Cô giáo tap một tên → **tick hiện ra ngay lập tức**, không chờ mạng. Đây là điều kiện sống còn:
   chờ mạng thì không dùng được.
2. Lượt tap đó vào **hàng chờ** trong máy.
3. Có mạng thì hàng chờ tự gửi lên, tick chuyển từ "chờ" sang "đã lưu".
4. Danh sách hội viên cũng lưu sẵn trong máy, nhưng **có thể cũ**.

UI phải nói ba điều này ra, không giấu:

- **Bao nhiêu lượt đang chờ gửi** — một nút hoặc dải ở đáy, luôn thấy được.
- **Tick nào đã lưu, tick nào còn chờ** — hai trạng thái khác nhau về **hình**, không chỉ khác màu.
- **Danh sách lưu lúc mấy giờ** — vì nó có thể cũ, và cô giáo cần biết để đoán ai bị thiếu.

Giới hạn đã chấp nhận, phải nói thẳng trong UI chứ không che: **lần đầu mở app bắt buộc phải có
mạng.** Câu hướng dẫn: "Mở app một lần ở nơi có mạng, trước giờ lên lớp."

## B2. Danh sách trạng thái phải dựng đủ

| # | Trạng thái | Yêu cầu nội dung |
|---|---|---|
| 1 | Danh sách hôm nay | mặc định. Tiêu đề lớp + giờ, sĩ số, hàng tên với nút tick tròn bên phải |
| 2 | Lọc theo bộ môn | dãy chip, chip đang chọn nền `--c-sky`. Có chip "Tất cả" |
| 3 | Không có bộ môn nào | trung tâm chưa khai báo → **ẩn hẳn dãy chip**, không hiện bộ lọc rỗng |
| 4 | Vừa tap một tên | tick hiện ngay, hàng đổi trạng thái, **đếm số đã tới tăng** |
| 5 | Tap lại để bỏ tick | huỷ được, không hỏi lại |
| 6 | Có N lượt đang chờ gửi | dải ở đáy: "2 lượt đang chờ gửi". Phân biệt rõ với tick đã lưu |
| 7 | Đang gửi | dải chuyển sang trạng thái đang chạy |
| 8 | Gửi xong | dải biến mất hoặc báo "Đã lưu tất cả", mọi tick chuyển sang đã lưu |
| 9 | Mất mạng | dải trạng thái mạng + "Danh sách lưu lúc 17:02". Vẫn tap được bình thường |
| 10 | Danh sách rỗng — chưa nhập ai | "Chưa có hội viên nào. Chủ trung tâm cần nhập danh sách trước." |
| 11 | Danh sách rỗng — ngoài giờ lớp | khác hẳn #10: "Hôm nay chưa có lớp nào" |
| 12 | Tìm nhanh theo tên | ô tìm, lọc ngay khi gõ. Gõ **không dấu** vẫn ra ("hong nhung" → "Hồng Nhung") |
| 13 | Có bản mới | dải trên cùng "Có bản mới — bấm tải lại". Xem B3 |
| 14 | Hướng dẫn cài trên iPhone | xem B4 |

## B3. Dải "có bản mới" — cấm tự động

Khi có bản cập nhật, hiện một dải và **để người dùng tự bấm tải lại**.

**Tuyệt đối không tự đổi bản trong im lặng.** Tự tải lại giữa giờ học sẽ mất những lượt tap còn
trong bộ nhớ chưa kịp gửi — cô giáo mất công điểm danh lại cả lớp và không hiểu vì sao.

Dải phải nói rõ hệ quả, ví dụ: "Có bản mới. Gửi xong 2 lượt đang chờ rồi hãy tải lại."

## B4. Màn hướng dẫn cài trên iPhone

iOS **không bao giờ** tự mời cài PWA. Nếu phát hiện Safari trên iPhone thì phải hiện một màn hướng
dẫn: bấm nút Chia sẻ → chọn "Thêm vào MH chính".

Màn này **không phải tuỳ chọn** — không có nó thì người dùng iPhone không bao giờ cài được, và
toàn bộ phần offline vô nghĩa. Cần hình minh hoạ vị trí nút Chia sẻ; vẽ bằng SVG đơn giản, đừng
dùng ảnh chụp.

Trên Android thì dùng lời mời cài của trình duyệt, không cần màn này.

## B5. Dữ liệu giả

Ít nhất **12 hội viên**, 3 bộ môn, để danh sách đủ dài mà thấy được cuộn và lọc.

## B6. Chữ nghĩa

Gọi người dùng là **"cô"** hoặc dùng câu không xưng hô. Ngắn, mệnh lệnh, đọc lướt được.
Không thuật ngữ: không "sync", không "đồng bộ hoá", không "hàng đợi", không "cache".

Câu mẫu đúng giọng:

- "2 lượt đang chờ gửi"
- "Đã lưu tất cả"
- "Danh sách lưu lúc 17:02"
- "Mở app một lần ở nơi có mạng, trước giờ lên lớp"
- "Chưa có hội viên nào. Chủ trung tâm cần nhập danh sách trước."
