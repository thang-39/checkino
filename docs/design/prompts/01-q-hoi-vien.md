# 01 · `/q` — hội viên

> **Đọc trước, theo đúng thứ tự:**
> 1. `docs/design/prompts/00-he-thong.md` — luật chung, bắt buộc, áp cho mọi thứ dưới đây
> 2. `docs/design/ban-2-operational.html` — hướng thiết kế đã chốt
>
> **Ghi ra:** `designs/q.dc.html` · **Báo cáo tự kiểm:** `docs/design/q-A8.md`
>
> **Trạng thái: đã dựng xong.** File này giờ là đặc tả của bản dựng đang có, không phải đơn đặt
> hàng cho một bản dựng mới. Sửa gì thì sửa ở đây trước, rồi mới nhờ Claude Design sửa file.

---

## B0. Người dùng và hoàn cảnh

Hội viên phòng tập. Quét mã QR dán ở cửa bằng camera điện thoại, trang mở ra trong trình duyệt.
**Chưa từng vào trang này bao giờ**, không cài app, không có tài khoản. Đang đứng ở cửa, có thể
ngoài nắng, tay ướt mồ hôi, muốn xong trong 5 giây rồi vào tập.

Ba điều quy định hình dáng màn hình:

- Đây là trang **server-render**, mỗi trạng thái là **một lần tải trang mới**, không phải SPA.
  Trong bản dựng cứ chuyển màn bằng JS cho tiện xem, nhưng **đừng thêm hiệu ứng chuyển cảnh mượt**
  — nó tạo ảo giác sai về sản phẩm thật.
- Trang phải xong **dưới 2 giây trên 4G**. Không ảnh nặng, không hiệu ứng tốn tài nguyên.
- **Không service worker, không manifest.** Đây không phải PWA.

Thêm một điều **cấm** riêng của `/q`: **không dropdown chọn cơ sở.** Hội viên quét mã QR của
**một** `scan_point` cụ thể — cơ sở đã biết trước, không có gì để chọn. Đa cơ sở là tính năng
gói Pro (D6), và `00` § A4 quy định pill dropdown chỉ dùng cho **bộ lọc**. Cơ sở hiện dưới dạng
pill tĩnh trong header, theo `00` § A1b.

## B1. Màn nhập số điện thoại — một màn duy nhất

`ban-2-operational.html` từng có hai phương án 1A và 1B. **1A đã bị loại.** Chỉ còn một màn nhập,
theo khung 1B — nửa dưới sáng dễ đọc ngoài nắng hơn:

```
nền than
├ header bar (00 § A1b)
├ hero  BẠN LÀ / AI NHỈ?
├ hai ô số cạnh nhau:  san hô "18:41 · đang mở cửa"  |  tím "24 · người đã tới"
└ tấm sáng --c-sage, bo 28px 28px 0 0, chiếm hết phần còn lại:
    nhãn SỐ ĐIỆN THOẠI · ô nhập trắng · nút chính than · dòng chữ trấn an · link học thử
```

Ô giờ là **san hô** vì nó là hoạt động đang diễn ra; ô sĩ số là **tím** vì nó là con số đếm
được có trần. Xem `00` § A2.

**Trạng thái "đang gửi" là chính màn này**, không phải một màn khác. Nó khác đúng ba thứ:
nhãn nút, spinner, và `disabled` của ô nhập. Nền **không** được đổi màu, ô nhập **không** được
nhảy vị trí — người ta vừa bấm nút xong, cả trang lật màu là mất phương hướng.

## B2. Danh sách trạng thái phải dựng đủ

Đây là toàn bộ logic của `/q`. Thiếu một cái là thiếu logic, không phải thiếu màn đẹp.

| # | Trạng thái | Nền màn | Yêu cầu nội dung |
|---|---|---|---|
| 1 | Nhập SĐT / đang gửi | than | như B1. Hai trạng thái, **một màn** |
| 2 | Điểm danh xong — bình thường | sage | tên hội viên, giờ, **còn 12 buổi**, hết hạn 04/08. Số buổi là con số to nhất màn |
| 3 | Điểm danh xong — sắp hết hạn | sage | thêm khối vàng: "Thẻ hết hạn sau 3 ngày", chỉ đường "nhắc lễ tân gia hạn" |
| 4 | Điểm danh xong — sắp hết buổi | sage | biến thể: "Còn 2 buổi" |
| 5 | Không cho điểm danh — thẻ hết hạn | **rust** | thẻ ở dạng **xám vô hiệu** + chip vàng `HẾT HẠN · 28/07` + câu chỉ đường. **Không viết "Lỗi"** |
| 6 | Ngoài giờ mở cửa | than | nói **giờ mở cửa cụ thể** ("Trung tâm mở 06:00–22:00"), không viết "Lỗi". Không màu cảnh báo — chẳng có gì để xử lý |
| 7 | SĐT lạ → mời học thử | than | form: tên, SĐT, bộ môn (tuỳ chọn), checkbox đồng ý. Xem B3 |
| 8 | Gửi form học thử xong | sage | cảm ơn + nói bước tiếp theo là gì |
| 9 | Thiết bị đã nhớ | than | mở ra thấy ngay tên mình + một nút điểm danh, không phải nhập lại SĐT. Có link "Không phải bạn? Đổi số" |
| 10 | Quét lại quá nhanh | sage | "Bạn đã điểm danh lúc 18:42 rồi" — đây **không phải lỗi**, không được đỏ hay cảnh báo |
| 11 | Mất mạng khi bấm | than | báo rõ, nút thử lại, và gợi ý "nhờ nhân viên điểm danh hộ". Không phải lỗi của hội viên → không màu cảnh báo |

**Thẻ hội viên là vật neo.** Nó hiện ở **mọi màn đã biết bạn là ai** — màn 2, 3, 4, 10 (gradient
tím `#9E98F6 → #6F66F0`) và màn 5 (xám vô hiệu: nền `#2A2D2A`, viền mảnh, chữ `--c-dim`, số buổi
`opacity .45`). Ngoại lệ: màn 9 **không** có thẻ — màn đó đã có hai ô số và một nút to, thêm thẻ
là chật.

Ba màn cố tình **không** có màu cảnh báo nào: 6, 10, 11. Ngoài giờ thì chẳng có gì để xử lý;
quét lại không phải lỗi; mất mạng không phải lỗi của hội viên.

## B3. Màn học thử — chỗ dễ làm sai nhất

Khi số điện thoại không có trong danh sách, người đứng đó thường là **chính chủ trung tâm đang
quét thử lần đầu**, hoặc một khách vãng lai.

Màn này **không được trông như lỗi**. Không đỏ, không dấu chấm than, không chữ "không tìm thấy"
đặt to. Nó phải đọc ra như một lời mời: "Bạn mới tới à? Để lại thông tin, trung tâm sẽ liên hệ."

Checkbox đồng ý là bắt buộc về pháp lý, nhưng đặt nhỏ ở dưới, không chắn đường.

## B4. Chữ nghĩa

Xưng hô: gọi hội viên là **"bạn"**. Không "quý khách", không "anh/chị" — không biết giới tính và
tuổi. Câu ngắn. Không thuật ngữ kỹ thuật: không "token", không "thiết bị đã xác thực", không "phiên".

Câu mẫu đúng giọng:

- "Điểm danh xong rồi!"
- "Còn 12 buổi"
- "Thẻ hết hạn sau 3 ngày. Nhắc lễ tân gia hạn giúp bạn nhé."
- "Trung tâm mở cửa 06:00–22:00. Giờ này chưa điểm danh được."
- "Bạn đã điểm danh lúc 18:42 rồi."
