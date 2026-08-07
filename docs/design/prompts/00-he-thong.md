# 00 · Hệ thiết kế Checkino — đọc trước mọi lượt

> **⏳ Tài liệu build-time — đã đóng vai trò.** Bốn lượt dựng UI đã xong. Nguồn sự thật của hệ
> thiết kế cho code sản phẩm giờ là [`docs/DESIGN.md`](../../DESIGN.md) — nó đã hấp thụ luật
> chung của file này (`DECISIONS.md § D15`). Giữ file này làm lịch sử build-time; khi nội dung
> lệch, DESIGN.md thắng.

File này là **nguồn sự thật** cho cả bốn lượt dựng UI. Mỗi file prompt (`01`, `02`, `03a`, `03b`)
đều bắt đầu bằng "đọc file này trước". Sửa ở đây thì cả bốn lượt đổi theo — không chép nội dung
file này đi đâu khác.

---

Bạn đang dựng UI cho **Checkino** — SaaS điểm danh QR + quản lý thẻ hội viên cho trung tâm nhỏ ở
Việt Nam (gym, võ thuật, yoga, trung tâm ngoại ngữ). Toàn bộ giao diện **tiếng Việt**.

Sản phẩm có ba bề mặt, ba người dùng khác nhau. File prompt kèm theo sẽ nói bạn đang dựng bề mặt
nào. File này nói **luật chung** cho cả ba.

## A0. Hai file phải đọc trước khi viết dòng code nào

1. `docs/design/ban-2-operational.html` — hướng thiết kế **đã chốt**. Lấy token màu, thang chữ,
   hình dáng component, cách lồng hộp từ đây. Hai màn `/q · nhập SĐT — 1A` và `— 1B` trong file
   này **đã được duyệt**, giữ nguyên, đừng thiết kế lại.
2. `docs/design/refs/` — ảnh tham chiếu gốc, để hiểu ngôn ngữ hình muốn nhắm tới.

## A1. Bốn tầng hộp — luật quan trọng nhất

Đây là luật hay bị vi phạm nhất, và vi phạm nó là lý do một giao diện "đẹp nhưng không đồng nhất".
Mỗi phần tử phải thuộc đúng một tầng, và tầng quyết định màu nền, bo góc, padding:

| Tầng | Là gì | Nền | Bo góc | Padding |
|---|---|---|---|---|
| 0 · Màn hình | nền toàn màn | `--c-ink` #131413 | — | 16px hai bên |
| 1 · Khối | một nhóm nội dung có tiêu đề | một màu khối ở § A2 — **trừ `--c-rust`**, màu đó chỉ làm nền màn | 24px | 16–17px |
| 1b · Hàng danh sách / dải đơn | vật **lặp** (hàng tên) hoặc **dải đơn** (ô tìm, dải trạng thái) đặt **trực tiếp trên tầng 0**, không có tiêu đề | sắc nhạt của `--c-ink` (`#1F211F`) — hoặc một màu bão hoà § A2 nếu dải đó **mang nghĩa** | 18px, hoặc 999px nếu là dải/ô cao ≤ 56px | 8–16px |
| 2 · Thẻ con | ô số / ô nhập nằm **trong** khối tầng 1 | sắc **nhạt** của **chính** màu khối cha | 18px | 13–14px |
| 3 · Nhãn nhỏ | badge, chip, pill trong thẻ con | `rgba(255,255,255,.55)` hoặc `rgba(0,0,0,.20)` | 999px | 3px 9px |
| 3b · Ô hình 42–44px | vật vuông/tròn chứa **đúng một** icon hoặc một chữ (logo header, tick hàng danh sách, ô icon bước hướng dẫn) | màu bão hoà bất kỳ theo nghĩa § A2, **được phép khác màu cha** | 13px (vuông) hoặc 999px (tròn) | — |

Vì sao có `1b`: một danh sách 12 hàng mà mỗi hàng là khối tầng 1 (bo 24px, padding 16px) sẽ đọc ra
như 12 khối nội dung rời rạc và mất luôn cảm giác "một danh sách" — chưa kể cao thêm ~8px mỗi hàng.
Nhưng hàng danh sách cũng không phải thẻ con, vì tầng 2 định nghĩa là "nằm **trong** khối tầng 1".
Hai ràng buộc để `1b` không thành cửa sau phá luật: vật tầng 1b **không được chứa khối tầng 1**
(con của nó chỉ được là chữ, nhãn tầng 3, hoặc ô hình tầng 3b); và **dải màu bão hoà ở tầng 1b vẫn
tính** vào hạn "tối đa hai khối màu bão hoà một màn" ở § A2.

Vì sao có `3b`: nó là **nhãn**, không phải mảng — nên nó được mượn màu khác cha (một tick vàng trong
hàng nền than là đúng), và **không tính** khi đếm hạn hai khối bão hoà. Đừng nhét nó vào tầng 3:
tầng 3 buộc bo 999px và nền mờ, còn ô hình dùng màu đặc.

Bốn điều **cấm**, không có ngoại lệ:

1. **Khối tầng 1 không được chứa khối tầng 1 khác màu.** Muốn hai màu thì đặt hai khối cạnh nhau
   ở tầng 0, không lồng.
2. **Thẻ con chỉ được dùng sắc nhạt của màu cha.** Thẻ con trong khối san hô dùng `#F98872`, không
   được mượn tím hay vàng.
3. **Không lồng quá 3 tầng màu.** Hết tầng 3 thì dùng chữ, không dùng thêm nền.
4. **Màu bão hoà không bao giờ tràn kín màn hình.** Nó luôn phải có nền than bao quanh — hoặc là
   một khối, hoặc là một mảng chiếm phần trên rồi nhường lại cho phần dưới. Tràn viền là hỏng.

## A1b. Ba slot đầu màn — mỗi slot một nghĩa

Luật bốn tầng hộp nói **hộp** lồng nhau thế nào. Mục này nói **phần đầu mỗi màn** đặt gì vào đâu.
Vi phạm nó là cách nhanh nhất để hai màn liền kề đọc ra như hai sản phẩm khác nhau.

| Slot | Nội dung | Đổi theo trạng thái? |
|---|---|---|
| Header bar | logo tròn 42px + **tên tổ chức** 17px + pill cơ sở | **Không.** Giống hệt mọi màn |
| Nhãn nhỏ (eyebrow) | **tên người**, trống nếu chưa biết là ai · chip bên phải = mốc giờ | Có |
| Hero tầng 0 | **câu trạng thái**, 36px | Có |

Ba điều **cấm**:

1. **Tên tổ chức không bao giờ nằm ở eyebrow.** Nó chỉ sống ở header. Eyebrow là chỗ của
   **người**, và một slot chỉ được mang một nghĩa.
2. **Header không mang chữ hành động** (`ĐIỂM DANH`, `XONG RỒI`) hay icon trạng thái (✓, 👋).
   Header trả lời "đây là đâu", hero trả lời "đang xảy ra chuyện gì". Header đứng yên tuyệt đối
   qua mọi màn — đó chính là thứ làm người dùng tin họ vẫn đang ở cùng một chỗ.
3. **Hàng eyebrow giữ `min-height` cố định kể cả khi trống**, để hero không nhảy vị trí khi
   chuyển màn.

Ngoại lệ duy nhất cho phép hero mang tên người: màn "đã nhận ra bạn" (thiết bị đã nhớ) — ở đó
việc nhận ra tên **chính là** nội dung của trạng thái.

**Eyebrow trống chỉ có một nghĩa: "chưa biết là ai".** Và nghĩa đó chỉ tồn tại ở `/q`, nơi thiết bị
thật sự có thể chưa nhận ra người đang cầm máy. Ở `/staff` và `/admin` thì eyebrow **luôn có chữ**
(tên bộ môn, tên khu vực, tên hội viên) — không được để trống rồi mượn khoảng trống đó nói "màn này
không có gì để mô tả". `min-height` cố định vẫn giữ, nhưng đó là lý do **bố cục**, không phải nghĩa.
Nguồn: `admin-A8.md § 0b`.

### Con số nào được lên hero

Tách theo **nguyên nhân làm con số đổi**, không theo màn:

| Con số đổi vì | Sống ở đâu |
|---|---|
| **Dữ liệu đổi** — thêm người quét, thẻ hết hạn, nhập xong danh sách | **Hero 36px.** Đây đúng là "câu trạng thái" |
| **Người dùng vừa bấm bộ lọc hoặc gõ tìm** | **Dòng kết quả cạnh bộ lọc.** Không bao giờ leo lên hero |

Ở màn có bộ lọc, hero là **hằng số của màn** (`22 HỘI VIÊN TRONG DANH SÁCH`), và dòng kết quả mang
đủ cả nguyên nhân lẫn mẫu số: `Sắp hết hạn — 4 trong 22 hội viên`.

Lý do: hero đổi theo bộ lọc thì thứ duy nhất giải thích cú nhảy là eyebrow 10.5px `opacity:.62` —
**thành phần mờ nhất đỡ nghĩa cho thành phần to nhất**. Người dùng thấy con số lớn đổi mà không
thấy nguyên nhân. Nguồn: `admin-A8.md § 0a` (lật lại cách `/staff` đang làm — xem `staff-A8.md § 4.4`).

Tên tổ chức tiếng Việt dài, phải xuống được 2 dòng ở bề rộng 360px mà không tràn, không cắt
chữ. Thử bằng `TRUNG TÂM ANH NGỮ SAO MAI`, đừng thử bằng `GYM ABC`.

## A2. Màu, và nghĩa cố định của từng màu

Màu **không** được chọn cho đẹp. Mỗi màu mang một nghĩa, dùng sai nghĩa là lỗi:

| Token | Mã | Sắc nhạt (thẻ con) | Chữ trên nền | Nghĩa cố định |
|---|---|---|---|---|
| `--c-ink` | `#131413` | `#1F211F` / `#2A2D2A` | `#F2F2EC` | nền mặc định, khối trung tính, **và vật đã vô hiệu** |
| `--c-coral` | `#F4573F` | `#F98872` | `#2A0C06` | **số chính, hoạt động đang diễn ra** |
| `--c-rust` | `#8E2C1B` | — | `#F2F2EC` | **bị chặn, hôm nay không vào được. CHỈ làm nền màn** |
| `--c-purple` | `#6F66F0` | `#9E98F6` | `#0D0838` | **sức chứa, hạn mức, sĩ số** |
| `--c-yellow` | `#F3C24A` | `#F8DC96` | `#2C1F03` | **cần xử lý và CÒN KỊP, và thành tích** |
| `--c-sage` | `#C4D4C1` | `#DCE7DA` | `#14180F` | **trạng thái xong xuôi, mảng dịu** |
| `--c-sky` | `#A9E5F1` | — | `#052430` | **đã chọn / đã tick** |
| `--c-dim` | `#9AA096` | — | — | chữ phụ trên nền than, và chữ trên vật đã vô hiệu |

Câu để nhớ khi phân vân giữa vàng và đỏ:

> **Vàng = vẫn vào được, nhưng phải xử lý sớm. Rust = hôm nay không vào được.
> San hô không bao giờ là lỗi — nó là nhịp của hiện tại.**

Sáu luật màu thêm:

- **Màu không bao giờ là tín hiệu duy nhất.** Mọi ô màu phải có chữ đi kèm. Mù màu, nắng gắt, in
  đen trắng đều làm màu mất tác dụng. Hệ quả ngược cũng đúng: **khi chữ đã nói rồi thì đừng
  thêm tín hiệu thứ hai.** Thẻ hết hạn có chip chữ `HẾT HẠN · 28/07` là đủ — không gạch ngang
  số buổi nữa, hai tín hiệu chồng nhau chỉ làm rối.
- **Vàng làm nền thì chữ phải đậm màu** (`#2C1F03`), không dùng chữ trắng — tương phản không đạt.
- **Một màn tối đa hai khối màu bão hoà.** Nhiều hơn thì không còn gì nổi bật.
- **Nền màn chỉ có đúng ba giá trị**: than (mặc định) · sage (xong xuôi) · rust (bị chặn).
  Không màu nào khác được làm nền màn. Rust đi ngược lại thì cũng đúng: nó **chỉ** làm nền màn,
  không bao giờ làm khối.
- **Gradient chỉ được đi trong một họ màu** (sắc nhạt → chuẩn), ví dụ `#9E98F6 → #6F66F0`.
  Gradient bắc cầu hai họ là trộn hai nghĩa vào một vật, và vật đó hết đọc được.
- **Nút chính**: nền sáng → nút than `#131413`; nền than hoặc rust → nút kem `#F2F2EC`.
  Nút phụ = chữ gạch chân, không nền. **Xanh băng không bao giờ là nút** — nó chỉ mang nghĩa
  "đã chọn / đã tick".
- **Liên kết (`<a>`) dùng kem `#F2F2EC` + gạch chân**, không dùng xanh băng. Xanh băng đã có nghĩa
  "đã chọn / đã tick"; cho nó thêm nghĩa "bấm được" là làm một màu mang hai nghĩa, và người dùng sẽ
  không biết ô xanh nào bấm được ô nào không.

## A3. Chữ

Font **Archivo** (Google Fonts, miễn phí, **có** khối `vietnamese`, có trục width 75–125). Nếu môi
trường chặn font ngoài thì lùi về `system-ui` — **đừng** thay bằng Poppins hay Outfit, hai font đó
**không** có bộ dấu tiếng Việt, chữ "ế ộ ữ" sẽ vỡ.

| Vai trò | Cỡ | Đậm | Ghi chú |
|---|---|---|---|
| Hero (câu trạng thái) | **36px** | 800 | **VIẾT HOA**, `wdth` 82, `ls -0.035em`, `lh 0.95`, tối đa 2 dòng. **Cỡ này cố định, không đổi theo màn** |
| Tên tổ chức (header) | 17px | 800 | VIẾT HOA, `wdth` 88, `lh 1.05`, xuống được 2 dòng |
| Tiêu đề khối | 20px | 800 | VIẾT HOA, cùng `wdth` 88, xuống dòng được |
| Số lớn | 33px | 800 | `letter-spacing: -0.035em`. **Không được to hơn hero** |
| Thân | 15px | 400–600 | bề rộng thường (`wdth` 100) |
| Nhãn nhỏ | 10.5px | 700 | VIẾT HOA, `letter-spacing: 0.13em`, opacity .62 |

Hero cùng một cỡ ở **mọi** màn. Cho mỗi màn tự chọn cỡ theo độ dài câu chữ là cách chắc chắn
nhất để bộ màn hình đọc ra rời rạc — dài quá thì rút chữ, đừng thu cỡ.

Tiếng Việt dài hơn tiếng Anh ~15%. Nhãn và nút phải chịu được chữ dài, không thiết kế vừa khít
chữ tiếng Anh rồi vỡ khi dịch. Tên người Việt dài — hàng danh sách phải chứa được
"Nguyễn Thị Hồng Nhung" mà không vỡ.

## A4. Kích thước và hình dáng

- Bo góc: khối 24px · thẻ con 18px · nút và chip 999px (viên thuốc)
- Vùng chạm tối thiểu **52px** cho **hành động chính** và cho **mọi thứ ở `/staff`**
  (tay ướt mồ hôi ở cửa phòng tập), **kể cả điều hướng** — thanh nav là điều hướng
  chính nên cũng ≥52px. **44px là sàn tuyệt đối cho nút phụ trên `/admin`** (link
  chữ gạch chân kiểu "Chọn file khác", "Quay lại hồ sơ") — chủ trung tâm ngồi giữa
  hai ca, 44px (ngưỡng Apple HIG) đủ; dưới 44px là sai. Ràng buộc này rõ ra từ
  `admin-A8.md` R1/R2.
- Chữ trong ô nhập tối thiểu **16px**. Dưới ngưỡng đó Safari iOS tự phóng to trang khi chạm vào ô.
- Mọi màn phải đọc được ở bề rộng **360px**.
- **Màn rộng ≥ 720px: một cột `max-width:440px` căn giữa trên nền than, không bố cục lại.** Không
  có breakpoint nào khác, không có bố cục hai cột, không có bảng ngang. Đây là quyết định sản phẩm
  (`DECISIONS.md § D13`), không phải chỗ để cân nhắc lại lúc dựng.
- Bảng dữ liệu trên điện thoại: dùng **hàng thẻ**, không dùng bảng cuộn ngang.
- Nút tròn trắng 42px cho nút quay lại; pill dropdown cho bộ lọc; thanh nav nổi hình viên thuốc,
  mục đang chọn nền `--c-sky`.

## A5. Ràng buộc sản phẩm — vi phạm là sai nghiệp vụ, không phải sai thẩm mỹ

- **Không có mã 6 số dự phòng ở `/q`.** Mất mạng thì `/q` không mở được, nên chẳng có mã nào để hiện.
- **`/staff` không bao giờ hiện hay hỏi số điện thoại.** Cô giáo tap theo **tên**.
- **Một số điện thoại = một hội viên.** Không hỗ trợ hội viên không có SĐT.
- **Hội viên không có mật khẩu, không có OTP.** Nhập SĐT khớp danh sách là vào.
- **Bộ môn là tuỳ chọn.** Mọi màn phải chạy được khi trung tâm không khai báo bộ môn nào — và
  không được hiện bộ lọc rỗng chình ình.
- **Gói miễn phí giới hạn 50 hội viên.**
- **Nhập danh sách không bao giờ xoá ai.** Hội viên vắng mặt trong file để nguyên.
- **Không dịch vụ trả tiền theo lượt.** Không bản đồ, không realtime bên thứ ba, không webfont
  trả phí, không gửi tin nhắn.
- Thông báo từ chối phải **nói lý do và chỉ đường**, không được viết "Lỗi" hay "Error".

## A6. Yêu cầu kỹ thuật của bản dựng

- **Một file HTML duy nhất**, tự chứa. CSS và JS nội tuyến. Không thư viện ngoài, không CDN (trừ
  Google Fonts; chặn thì bỏ luôn và dùng `system-ui`). Biểu đồ vẽ bằng CSS hoặc SVG thuần, không
  dùng thư viện chart.
- **Tương tác thật**, không phải ảnh tĩnh: bấm nút thì chuyển màn, gõ ô nhập thì nút bật, tick thì
  đổi trạng thái, chọn chip thì lọc danh sách. Dữ liệu giả để trong một object JS ở đầu file.
- Token màu đặt tên **đúng như bảng A2**. Bản dựng `.dc.html` **được miễn** luật `:root` — định dạng
  này viết style nội tuyến trên từng thẻ nên `var(--c-…)` phải gõ lại vào từng thuộc tính, không lãi
  gì. Nơi token sống thật là `design/tokens.css`, viết sau khi đủ bốn bản dựng (`D12 · Chưa làm`).
  Đổi lại: **mã màu trong bản dựng phải khớp từng ký tự với bảng A2** — không tự pha shade mới ngoài
  các shade nhạt đã dùng. Nguồn của miễn trừ này: `admin-A8.md § 2.1` (cả ba bản dựng đều đã vi phạm
  luật cũ, nên sửa luật chứ không sửa ba file đã đóng).
- **Mỗi trạng thái là một màn bấm tới được**, không phải mô tả bằng chữ. Thêm một thanh điều hướng
  nhỏ cố định ở góc để nhảy nhanh giữa các trạng thái khi xem.
- Khung điện thoại 390×844 để xem trên desktop, nhưng bên trong phải responsive tới 360px.
- Tiếng Việt có dấu đầy đủ. Tên người Việt thật: Nguyễn Thị Hồng Nhung, Trần Quốc Bảo,
  Lê Hoàng Phương Anh, Phạm Minh Khang, Võ Thị Thanh Trúc, Đặng Thuỳ Linh.

## A7. File đầu ra — cố định, đừng đặt tên khác

| Lượt | Ghi ra |
|---|---|
| 01 · `/q` | `designs/q.dc.html` — **đã dựng xong** |
| 02 · `/staff` | `designs/staff.dc.html` |
| 03a · `/admin` nền tảng | `designs/admin.dc.html` |
| 03b · `/admin` báo cáo | **mở rộng chính** `designs/admin.dc.html` |

Bản dựng sống ở **`designs/`**, định dạng **Claude Design** (`.dc.html`) chứ không phải HTML tự
chứa như dự tính ban đầu. File trỏ tới `./support.js` — runtime của Claude Design, **đã commit
trong repo** (`designs/support.js`, file generated, đừng sửa tay). `docs/design/proto/` bỏ trống,
không dùng nữa.

**Mở bản dựng thế nào:** phục vụ thư mục `designs/` qua **HTTP cục bộ** rồi mở, đừng mở thẳng
`file://` — runtime dùng `fetch` để đọc lại chính file `.dc.html`, mà `fetch` không nhận scheme
`file:`. Cũng **cần mạng**: `support.js` kéo React 18.3.1 + ReactDOM + Babel từ `unpkg.com` kèm SRI,
và `<helmet>` kéo Archivo từ Google Fonts. Không có Archivo thì mọi phép đo bố cục đều sai — kiểm
bằng `document.fonts.check('800 36px Archivo')` trước khi tin một con số nào.

Lượt `03b` chạy ở phiên riêng, không có ngữ cảnh của `03a`. Nó phải **đọc lại**
`designs/admin.dc.html`, giữ nguyên token và component đã có, rồi thêm màn vào — không viết
lại từ đầu, không đổi tên biến.

## A8. Sau khi dựng xong, tự kiểm và báo cáo

Ghi vào `docs/design/<tên>-A8.md` và in ra cuối câu trả lời:

1. Có chỗ nào vi phạm luật bốn tầng hộp ở A1 không? Nếu có, chỉ ra chính xác chỗ nào.
2. Màu nào bị dùng sai nghĩa so với bảng A2? Màn nào có quá hai khối màu bão hoà?
3. Trạng thái nào trong danh sách của file prompt **chưa** dựng?
4. **Chỗ nào bạn phải tự quyết vì prompt không nói rõ?** Đây là phần quan trọng nhất — nó chỉ ra
   lỗ hổng trong tài liệu thiết kế, và sẽ được dùng để viết `docs/DESIGN.md`. Liệt kê càng cụ thể
   càng tốt, kể cả những quyết định nhỏ.
