# Báo cáo tự kiểm A8 — `/admin` nền tảng (`designs/admin.dc.html`)

> Báo cáo hai lượt dựng `/admin` theo `00-he-thong.md § A8`.
> Bản dựng: `designs/admin.dc.html` — **27 màn** (15 của `03a` + 12 của `03b`).
> Prompt: `docs/design/prompts/03a-admin-nen-tang.md` và `03b-admin-bao-cao.md`.
>
> **Gồm hai phần:** lượt 3/4 (`03a`, phần chính bên dưới) và lượt 4/4 (`03b`, nối ở cuối file
> đúng yêu cầu prompt). `docs/DESIGN.md` viết sau khi đủ bốn bản dựng — xem
> `DECISIONS.md § D12 · Chưa làm`; còn thiếu `q-A8.md`. Đừng chắt lọc file này thành hệ thiết kế bây giờ.

---

## § 0a · Hero đổi vì **dữ liệu**, không đổi vì **bộ lọc**

Đây là mục mà bản dựng trỏ tới ở hai chỗ (`admin.dc.html:420` và `:703`). Nó cũng là câu trả lời
cho luật còn treo `staff-A8.md § 4.4`, mà `DECISIONS.md § D12 · Chưa làm` đã hoãn lại chờ dựng
`/admin` để "quyết một lần cho cả hệ".

**Vấn đề gốc.** Ở `/staff`, hero `2/12` nhảy thành `2/5` khi cô giáo bấm chip bộ môn. Thứ duy nhất
giải thích cú nhảy là nhãn nhỏ 10.5px `opacity:.62` — **thành phần mờ nhất đỡ nghĩa cho thành phần
to nhất**. Người dùng nhìn thấy con số lớn đổi mà không thấy nguyên nhân.

**Quyết định.** Tách theo *nguyên nhân làm con số đổi*, không theo *màn nào*:

| Con số đổi vì | Sống ở đâu |
|---|---|
| **Dữ liệu đổi** (thêm người quét, thẻ hết hạn, nhập xong danh sách) | **Hero 36px.** Đây đúng là "câu trạng thái" của `§ A1b` |
| **Người dùng vừa bấm bộ lọc / gõ tìm** | **Dòng kết quả** ngay cạnh bộ lọc — không bao giờ leo lên hero |

Hai chỗ bản dựng thi hành luật này:

- Trang chủ: hero `{{ arrived }} ĐÃ TỚI HÔM NAY` tăng thật khi `tick()` (`:704`) đẩy người mới vào
  luồng. Không nút nào của người dùng làm nó đổi. Đây là hero **đúng nghĩa**.
- Màn 11: hero là **hằng số của màn** — `22 HỘI VIÊN TRONG DANH SÁCH` (`:817`), đứng yên qua mọi
  bộ lọc. Con số của tập đang lọc nằm ở `resultLine` (`:421`, `:808`), viết đủ cả nguyên nhân:
  `Sắp hết hạn — 4 trong 22 hội viên`.

Đường dẫn khó nhất đã kiểm: từ trang chủ bấm ô cảnh báo `4 THẺ SẮP HẾT HẠN` (`goExpiring`, `:891`)
nhảy thẳng sang màn 11 **đã lọc sẵn**. Với luật cũ, hero sẽ hiện `4` và người dùng không biết `4`
là trên tổng bao nhiêu. Với luật mới, hero giữ `22`, `resultLine` nói `Sắp hết hạn — 4 trong 22`.

Bản dựng giữ lại công tắc `heroTheoBoLoc` (`:815`, mặc định `false`) để bật lại cách cũ mà so bằng
mắt. **Đừng xoá công tắc này trước khi `docs/DESIGN.md` chốt xong** — nó là bằng chứng.

**Ripple sang `/staff`:** `designs/staff.dc.html` đang làm theo cách cũ. Việc sửa `/staff` **không**
thuộc lượt này (`/staff` đã đóng ở `staff-A8.md:50`) — xem mục *Việc để lại* ở cuối.

## § 0b · Nhãn nhỏ rỗng — luật còn treo thứ hai, và câu trả lời là "không gặp"

`staff-A8.md § 4.6` treo câu hỏi: khoảng trống ở nhãn nhỏ mang nghĩa gì — theo `§ A1b` là *"chưa
biết là ai"* (tạm thời), theo `§ B0b` của `/staff` là *"không có tập nội dung để mô tả"* (cấu trúc,
vĩnh viễn). Cùng một khoảng trống, hai nghĩa.

Bản dựng `/admin` trả lời bằng cách **không bao giờ để trống**: bảng `EYE` (`:752-758`) phủ đủ 15/15
màn. Trên `/admin` không tồn tại trạng thái "chưa biết là ai" — người cầm máy luôn là chủ trung tâm,
và nhãn nhỏ mang **tên khu vực** (`§ B0b`).

Nên nghĩa *"trống = chưa biết là ai"* của `§ A1b` **chỉ áp cho `/q`**, nơi thiết bị thật sự có thể
chưa nhận ra ai. Đề nghị viết thẳng điều đó vào `§ A1b` thay vì để hai nghĩa cùng tồn tại, và giữ
nguyên yêu cầu `min-height` cố định vì lý do đó là bố cục, không phải ngữ nghĩa.

---

## Lượt kiểm bằng mắt — **đã chạy**, bố cục 15/15 đạt

Chạy bằng Chromium headless trên `designs/support.js` (runtime Claude Design, nay đã commit — xem
`§ 7`), phục vụ qua HTTP cục bộ. **Archivo tải thật** (`document.fonts.check('800 36px Archivo')`
= `true`) — đo bằng font fallback thì mọi con số dưới đây vô nghĩa. Không lỗi console, không
request nào hỏng. **30 lượt đo: 15 màn × {390px, 360px}**, mỗi lượt chạy lại hai lần với tên trung
tâm ngắn (`Gym Thành Phát`) và dài (`TRUNG TÂM ANH NGỮ SAO MAI`, đúng như `§ A1b:81` bắt).

Ba con số quan trọng nhất, đều đạt tuyệt đối:

- **Không một phần tử nào bị cắt ngang** ở cả 30 lượt. Kể cả các span khai `text-overflow:ellipsis`
  cũng **chưa lần nào phải dùng tới** ellipsis — `scrollWidth − clientWidth = 0` khắp nơi.
- **Hàng nhãn nhỏ cao đúng 22px ở cả 30 lượt**, kể cả màn 13/14/15 (tên hội viên, không chip).
- **`heroTop` = 182px đứng im ở cả 30 lượt.** Hero không nhảy một pixel nào khi chuyển màn — đây
  chính là thứ `§ A1b` điều 3 muốn bảo vệ, và nó đạt ở mức tuyệt đối chứ không phải "gần đúng".

Bốn chỗ tôi nghi nhất trước khi render — **cả bốn đều không vỡ**:

| Nghi ngờ | Đo được ở 360px |
|---|---|
| Lưới thao tác 2 cột, nhãn `SỬA SỐ ĐIỆN THOẠI` | ô rộng **143px**; 5 ô cao 62px, 2 ô (`TẠM DỪNG THẺ`, `SỬA SỐ ĐIỆN THOẠI`) giãn lên **70px**. Không cắt chữ, lưới tự giãn. Đọc tốt |
| Hàng ba nút ở màn 4 (`Bỏ qua` phải ngang hàng `Tiếp tục`) | mỗi nút **126×56px**, tràn = 0. Vẫn ngang hàng, không rớt dòng |
| Tên trung tâm dài | header **2 dòng, cao 36px**, không tràn, ở cả 15 màn. Trên thẻ hội viên: 1 dòng, rộng 207px trong lòng thẻ 328px, **cách mép phải đúng 18px = đúng padding**. Trên poster A4: 1 dòng |
| Poster A4 có phải cuộn không | tờ giấy cao **337px**, cả màn `scrollHeight = clientHeight = 458` → **không phải cuộn**, nút tải không bị đẩy khuất |

### Hành vi động — kiểm bằng cách bấm thật

- **Hero đổi vì dữ liệu:** để trang chủ chạy 9,2 giây (hai nhịp `tick()`), hero tự đi từ
  `24 ĐÃ TỚI HÔM NAY` → `26 ĐÃ TỚI HÔM NAY`. Không ai bấm gì.
- **Hero đứng yên khi lọc — `§ 0a` đúng như viết.** Qua **năm** trạng thái lọc khác nhau (chưa lọc
  → lọc `Sắp hết hạn` → thêm ô tìm `hoang` → `0938` → `zzz`), hero giữ nguyên
  `22 HỘI VIÊN TRONG DANH SÁCH` **không đổi một lần nào**, còn dòng kết quả đổi đủ:
  `Tất cả · 22 hội viên` → `sắp hết hạn — 4 trong 22 hội viên` → `sắp hết hạn · "hoang" — 0 trong 22`.
- **Đường khó nhất cũng đúng:** bấm ô cảnh báo `4 THẺ SẮP HẾT HẠN` ở trang chủ → nhảy sang màn 11
  đã lọc sẵn, hero vẫn `22`, dòng kết quả `sắp hết hạn — 4 trong 22 hội viên`. Đây là đoạn văn tôi
  viết ở `§ 0a` trước khi render được, và render xác nhận đúng từng chữ.
- **Ô tìm lọc thật, và chuẩn hoá cả dấu lẫn dấu cách:** `nguyen thi` (không dấu) → ra
  `Nguyễn Thị Hồng Nhung`; `HOÀNG` → ra `Lê Hoàng Phương Anh`; `0938` và `0938 447` (có dấu cách)
  đều ra đúng người; `zzz` → 0 kết quả kèm câu chỉ đường *"Thử gõ ngắn hơn, hoặc gõ 3 số cuối"*.

### Ba việc lượt render báo về mà đọc source không thấy

**R1 · Vùng chạm của thanh nav là 86×41px — dưới ngưỡng 52px của `§ A4`.** Có ở cả 6 màn ngoài
wizard. Nguyên nhân: `:570-577` dùng `padding:11px 0` quanh icon 19px = 41px, không khai
`min-height`.

**R2 · Năm nút phụ dạng chữ gạch chân chỉ cao 44px** — cũng dưới 52px: `Chọn file khác` (màn 6, 7),
`Xem danh sách hội viên` (màn 9), `Xong, vào trang chủ` (màn 10), `Quay lại hồ sơ` (màn 14, 15).
Đây là `min-height:44px` **cố ý gõ trong code**, không phải tai nạn.

R1 và R2 cùng đặt một câu hỏi mà `§ A4` chưa trả lời: ngưỡng 52px được viết ra với lý do *"tay ướt
mồ hôi ở cửa phòng tập"* — đó là hoàn cảnh của `/staff`. Chủ trung tâm ngồi giữa hai ca thì 44px
(ngưỡng của Apple HIG) có đủ không? Đề nghị `§ A4` nói rõ: **52px cho hành động chính và cho
`/staff`; 44px là sàn tuyệt đối cho nút phụ trên `/admin`** — và nếu chốt thế thì thanh nav ở R1
vẫn phải sửa, vì nav là điều hướng chính chứ không phải nút phụ.

**R3 · Hai icon nav chưa dùng tới bị vẽ hỏng.** Đo hộp bao nét vẽ trong khung 24×24:

| Mục nav | Số nét | Nét vẽ | Lấp đầy |
|---|---|---|---|
| Trang chủ | 1 | 18×19 | 59% |
| Hội viên | 3 | 20×18 | 63% |
| **Báo cáo** | 1 | **12×9 tại (6,11)** | **19%** |
| **Cài đặt** | 2 | **15×10 tại (4,7)** | **26%** |

Icon `Cài đặt` ở `:577` chỉ có một vòng tròn `r=3` cộng **hai mẩu cung 2px rời rạc**
(`M19.4 15a1.6 1.6 0 0 0 .3 1.8M4.6 9a1.6 1.6 0 0 0-.3-1.8`) — vành bánh răng bị thiếu, nên trên
màn hình nó hiện ra như dấu `'o,`. Thấy rõ trong ảnh chụp ở màn 11 và màn 13. Icon `Báo cáo` không
hỏng nhưng ba cột chỉ cao 9/24 và dồn xuống đáy khung, nên nhỏ hơn hẳn hai icon bên cạnh.

Cả hai đều là mục lượt `03b` sẽ nối vào, nhưng chúng **đang hiển thị** — người bấm thử sẽ đọc ra
là lỗi render chứ không phải "chưa làm". Phải sửa trong lượt này, không để lại cho `03b`.

---

## 1 · Luật bốn tầng hộp (§ A1)

Rà 15 màn: **không thấy vi phạm bốn điều cấm ở `§ A1`**. Ba chỗ đáng ghi lại:

### 1.1 Chuỗi ba tầng ở trang chủ — đạt, và là ví dụ mẫu

`:74` tier 1 coral `#F4573F` → `:80` tier 2 `#F98872` (shade nhạt **của chính coral**, đúng luật
"thẻ con chỉ dùng shade nhạt của cha") → `:83` tier 3 badge `rgba(255,255,255,.55)`. Đúng 3 tầng,
không hơn. Khối tím `:98 → :103 → :106` lặp đúng cấu trúc đó với họ tím.

Chip legend ở `:93-94` là tier 3 nằm **thẳng** trong tier 1, bỏ qua tier 2. `§ A1` không cấm — ghi
lại vì `docs/DESIGN.md` sẽ cần nói rõ tầng được phép nhảy cóc.

### 1.2 Ô icon 42px lấy màu **cha**, không lấy shade nhạt — gặp lại lần thứ hai

Màn 8 (`:326` khối vàng `#F3C24A` → `:329` thẻ con `#F8DC96` → `:330` ô icon `#F3C24A`). Ô icon
dùng **đúng màu của ông nội**, không phải shade nhạt của cha. Nhìn thì đúng (số bước nổi lên khỏi
nền nhạt), nhưng bảng `§ A1` không mô tả được nó.

Đây là **lần thứ hai** — `staff-A8.md § 1.3` đã báo đúng chỗ này và `§ A1` đã thêm tier 3b để đỡ.
Bản dựng `/admin` cho thấy tier 3b còn thiếu một câu: *ô icon 42px được lấy màu bão hoà của tầng
bao ngoài nó hai lớp, vì nó là **dấu hiệu**, không phải hộp chứa.* Đề nghị thêm câu đó vào `§ A1`.

### 1.3 Poster A4 nằm ngoài hệ bốn tầng — đúng, và bản dựng đã tự chú thích

`:370-371`: tờ A4 kem `#F2F2EC` chiếm gần hết màn 10. Nếu đọc nó là khối UI thì đây là vi phạm
"màu sáng tràn màn". Nhưng nó là **ảnh của tờ giấy sẽ in ra**, không phải khối giao diện — cùng
loại với "khung máy mô phỏng" mà `staff-A8.md § 4.3` đã tách ra.

Đề nghị `§ A1` có một câu cho lớp này: *vật mô phỏng thế giới thật (tờ giấy in, khung điện thoại,
thẻ nhựa) không nằm trong hệ bốn tầng và không tính vào hạn mức khối bão hoà.* Thẻ hội viên ở `:457`
cũng thuộc lớp này — nó vẽ ra **một tấm thẻ**, không phải một khối UI.

---

## 2 · Màu (§ A2) — đếm khối bão hoà từng màn

Không màn nào quá hai khối bão hoà. Bảng đầy đủ:

| Màn | Khối bão hoà | Ghi chú |
|---|---|---|
| 1 Trang chủ | **2** — coral `:74`, tím `:98` | + ô icon vàng `:67` (tier 3b, không tính là khối) |
| 2 Bước 1 | 0 | |
| 3 Bước 2 | 0 | ô QR trắng, không phải màu bão hoà |
| 4 Bước 3 | 0 | chip bộ môn sky `:193` là tier 3 |
| 5 Bước 4 | 0 | + ô icon vàng `:212` |
| 6 Xem trước | **1** — coral `:257` | |
| 7 Xem trước có lỗi | **2** — coral `:257`, vàng `:270` | đúng ngưỡng |
| 8 File hỏng | **1** — vàng `:326` | |
| 9 Nhập xong | 0 | nền màn sage (một trong ba giá trị nền, D12) |
| 10 Poster | 0 | + ô icon sky `:380`; tờ A4 xem § 1.3 |
| 11 Danh sách | 0 | pill sky khi đang lọc là tier 3 |
| 12 Danh sách rỗng | 0 | |
| 13 Chi tiết | **1** — thẻ tím `:457` | gradient `#9E98F6 → #6F66F0`, **một họ màu**, đạt |
| 14 Sửa SĐT trùng | **1** — vàng `:515` | |
| 15 Tạm dừng | 0 | nền màn rust |

Nghĩa của từng màu — kiểm đủ:

- **Vàng = còn kịp, phải xử lý sớm.** Ba chỗ dùng vàng đều đúng nghĩa đó: cảnh báo thẻ sắp hết hạn
  (`:67`), dòng nhập lỗi *sẽ bị bỏ qua chứ không chặn* (`:270`), SĐT trùng *đổi số khác là xong*
  (`:515`). Không chỗ nào vàng mang nghĩa "bị chặn".
- **Rust = hôm nay không vào được.** Đúng một chỗ: màn 15 tạm dừng thẻ (`:871`). Rust chỉ làm nền
  màn, không làm khối — đúng D12.
- **Sage = xong xuôi.** Đúng một chỗ: màn 9 nhập xong. Và một chỗ thứ hai đáng ghi: cột biểu đồ
  **hôm nay** ở trang chủ `:892` (`i === 6 → #C4D4C1`) — sage ở đây mang nghĩa "cột này là hôm nay",
  hơi lệch khỏi "xong xuôi". Đọc được vì có chip legend `Hôm nay 24` giải thích bằng chữ
  (`:93`, `:893`), nhưng đây là chỗ `docs/DESIGN.md` phải quyết: sage có được mang nghĩa phụ
  "cái đang diễn ra" hay không.
- **Coral không bao giờ là lỗi** — đạt. Coral chỉ ở khối nhịp độ trang chủ và khối "sẽ ghi vào
  danh sách".
- **Sky = đã chọn** — đạt ở cả ba chỗ: chip bộ môn wizard, pill lọc đang bật, mục nav đang đứng.
- Chữ trên vàng luôn là `#2C1F03` (`:67`, `:212`, `:270`, `:326`, `:515`) — đạt `§ A2:110`.
- Liên kết là kem + gạch chân (`:17`, và hai thẻ `<a>` thật ở `:244`, `:335`) — đạt luật màu liên
  kết mà `staff-A8.md` vừa thêm vào.

### 2.1 Lệch thật với `§ A6`: bản dựng **không** khai `:root`

`§ A6:177` đòi *"toàn bộ token màu khai báo bằng CSS custom properties trên `:root`, không viết mã
màu rải rác"*. `admin.dc.html` dùng hex rời khắp nơi, không có `:root`.

Không phải lỗi riêng của lượt này — **cả ba** bản dựng đều thế (`q.dc.html`, `staff.dc.html`,
`admin.dc.html`), và hai bản kia đã đóng. Định dạng `.dc.html` viết style nội tuyến trên từng thẻ,
`:root` chỉ khai được trong `<helmet>` rồi vẫn phải gõ `var(--c-coral)` vào từng thuộc tính
`style="…"` — được, nhưng không phải cách file này được viết.

**Đã xử lý bằng cách sửa `§ A6`**, không sửa ba bản dựng: token sống ở `design/tokens.css` (chưa
viết, `§ D12 · Chưa làm`), bản dựng `.dc.html` được miễn. Sửa tài liệu cho khớp thực tế thì đúng
hơn là để một luật bị vi phạm ba lần mà không ai sửa.

---

## 3 · Trạng thái chưa dựng — rà đủ 13 mục `§ B3` đối chiếu 15 màn

**13/13 mục `§ B3` đã dựng**, thừa 2 màn lấy từ `§ B4` (sửa SĐT trùng, tạm dừng thẻ). Ánh xạ nằm
ở `SCREENS`/`NAV` (`:670`, `:672`), cùng thứ tự với bảng `§ B3`.

Cái **chưa** dựng, liệt kê đủ:

| Chưa dựng | Có bắt buộc không |
|---|---|
| 5/7 thao tác `§ B4` là `go: () => {}` (`:848-854`): gán gói mới, gia hạn, sửa bộ môn, thu hồi thiết bị, ngưng hội viên | **Không.** `§ B3` không đòi màn cho chúng; `§ B4` chỉ đòi hai chỗ đặc biệt và cả hai đã dựng thành màn 14/15 |
| Nút `Tải poster PDF` là `noop` (`:389`, `:942`) | Không — việc của code |
| Nút `Tải file mẫu` (`:244`, `:335`) là `href="#tai-file-mau"` | Không |
| Màn "dán từ Excel" chỉ có ô `textarea`, không có xem trước riêng cho luồng dán | Không — `§ B2` nói một màn xem trước chung, và bản dựng đưa cả hai nguồn vào cùng màn 6 |
| Trạng thái **đã đầy 50 chỗ** khi nhập | Không thuộc lượt này — là `M4-S01`, đang có dấu `QUYẾT ĐỊNH CẦN CHỐT` ở `STORIES.yml:721` |

### 3.1 Màn 15 có hai nút làm đúng một việc

`:560` nút chính `Tạm dừng thẻ` và `:561` nút phụ `Quay lại hồ sơ` **cùng gọi `goMember`**. Trong
bản dựng thì chấp nhận được (không có state thẻ để đổi), nhưng người bấm thử sẽ tưởng nút chính
hỏng. Đề nghị lượt sau cho nút chính chuyển sang màn 13 với thẻ **đang ở trạng thái tạm dừng** —
trạng thái đó `/q` đã có mà `/admin` chưa từng thấy.

---

## 4 · Chỗ phải tự quyết vì prompt không nói rõ

Phần quan trọng nhất theo `§ A8`. Liệt kê cả quyết định nhỏ.

### 4.1 `STEP_OF` gán bước **4** cho bốn màn liền nhau

`:675`: `pv`, `pvErr`, `pvBad`, `done` đều là bước 4. Nghĩa là thanh tiến trình đứng yên qua bốn
màn, và chip header giữ `BƯỚC 4/5` suốt cả luồng xem trước → xác nhận → nhập xong.

Có chủ ý: `§ B1` chốt **5 bước**, mà xem trước và xác nhận là *bên trong* bước "nhập danh sách",
không phải bước thứ sáu. Nếu thanh tiến trình nhích ở màn xem trước thì người dùng đếm ra sáu bước
trong khi chip nói năm.

Hệ quả phải chấp nhận: màn 9 "Nhập xong" là màn ăn mừng nền sage nhưng vẫn đeo chip `BƯỚC 4/5`.
Đọc được, nhưng đây là chỗ `docs/DESIGN.md` nên xem lại.

### 4.2 Đồng hồ giả kể một câu chuyện thời gian

`:868` `CLOCK` cho mỗi màn một giờ khác nhau: wizard chạy buổi sáng (`09:04`–`09:28`), vận hành
hằng ngày chạy buổi tối (`18:47`–`18:53`). Prompt không nói gì. Làm thế vì hai cụm màn thuộc hai
hoàn cảnh khác nhau, và giờ nhảy lung tung giữa các màn trông như lỗi.

Cùng loại với `staff-A8.md § 4.3` (thanh trạng thái iOS là khung máy mô phỏng, không phải sản phẩm).

### 4.3 Ngày `04/08` và năm `/26` là chuỗi cứng

`:879` chip `HÔM NAY · 04/08`, `:961` `mExp: m.exp + '/26'`, `:976` `pauseSum` cứng "Nghỉ 31 ngày…
sang 05/10" không theo hai ô nhập ngày. Cùng lý do `staff-A8.md § 4.2`: bản dựng không được phụ
thuộc ngày chạy thật, nếu không thì mở lại sau ba tháng sẽ đọc ra khác.

### 4.4 Chip `HÔM NAY · 04/08` **chỉ** hiện ở trang chủ

`:879` `chipShow: scr === 'home'`. `§ A1b` cho chip eyebrow mang mốc giờ. Trên `/admin` chỉ trang chủ
có con số phụ thuộc "hôm nay là ngày nào"; các màn khác gắn chip vào chỉ là trang trí.

### 4.5 Hàng danh sách bỏ hẳn số điện thoại khi **không** có bộ môn

`:794-795`: dòng phụ ghép `[bộ môn] · [còn n buổi · tới dd/mm]`. Nếu trung tâm bỏ qua bộ môn (`D7`),
dòng phụ chỉ còn phần thẻ. Không rơi vào SĐT — cố ý, vì `/admin` **được phép** hiện SĐT (khác
`/staff`, `§ A5`) nhưng ở màn danh sách thì hạn thẻ mới là thứ chủ trung tâm quét mắt tìm. SĐT
sống ở màn 13 (`:473`) và ở ô tìm — gõ số vẫn ra người (`:783`).

### 4.6 Tên hội viên **xám đi** khi thẻ hết hạn hoặc tạm dừng

`:798` `nameFg` đổi sang `#9AA096`. Prompt không nói. Làm thế vì hàng đã có chip chữ `HẾT HẠN` /
`TẠM DỪNG` rồi, và màu xám ở đây **không phải tín hiệu duy nhất** — đúng mặt còn lại của luật D12
("chữ đã nói rồi thì đừng thêm tín hiệu thứ hai" chỉ cấm thêm tín hiệu **mang nghĩa mới**).

Kiểm bằng ảnh đen trắng vẫn đọc được vì chip còn đó.

### 4.7 Ba quyết định nhỏ còn lại

- **Nút quay lại của wizard là ô tròn 56px** (`:173`), không phải chữ. `§ A4` đã có "nút tròn trắng
  42px cho nút quay lại" — bản dựng dùng 56px `rgba(255,255,255,.10)` để bằng chiều cao nút chính
  đứng cạnh. Đề nghị `§ A4` nới thành "42px khi đứng một mình, 56px khi đứng cùng hàng với nút chính".
- **Bỏ qua bộ môn thì xoá luôn bộ môn đã gõ** (`:913` `skipProgs` set `progs: []`). Cố ý: `§ B1` bắt
  "bỏ qua rồi thì không màn nào sau đó được đòi bộ môn", nên để lại bộ môn nửa vời là mâu thuẫn.
- **Nav có 4 mục nhưng 2 mục cuối chưa bấm được** (`:574-577`, xám `#9AA096`). Đó là chỗ lượt `03b`
  nối Báo cáo và Cài đặt vào — đã chú thích ở `:566`.

---

## 5 · Hai ngoại lệ `§ B0b` — có mâu thuẫn ở đâu không

`03a-admin-nen-tang.md:53` yêu cầu ghi rõ. Rà đủ 15 màn:

**Ngoại lệ 1 (header đổi giữa bước 1 và bước 2): không mâu thuẫn.** `:875` `headTitle` là
`'Checkino'` đúng một màn `w1`, sau đó là tên trung tâm. Có một chỗ tinh tế đã xử lý: `:738`
`orgName` fallback về `ORG.name` khi ô nhập còn trống, nên bước 2 luôn có tên để hiện kể cả khi
người dùng bấm qua mà không gõ gì.

**Ngoại lệ 2 (nhãn nhỏ mang tên khu vực): mâu thuẫn ở đúng một chỗ, đã xử lý.** `§ B0b` cho phép
màn **chi tiết hội viên** mang tên người. Nhưng màn 14 và 15 cũng nói về đúng một người mà `§ B0b`
không nhắc tới. Bản dựng cho cả ba màn `member`/`phoneEdit`/`pause` mang tên hội viên (`:757`).

Lý do: ranh giới thật không phải "màn chi tiết" mà là **"màn này đang nói về một người cụ thể hay
về một khu vực"**. Đề nghị `§ B0b` viết lại theo ranh giới đó, vì lượt `03b` sẽ đẻ thêm màn cùng
loại (nhật ký của một hội viên, chi tiết một lead học thử).

---

## 6 · Lỗi tìm được khi rà mã — bốn cái, **cả bốn đã xác nhận bằng render**

Không cái nào là lỗi thiết kế; đều là lệch số liệu hoặc lệch hành vi trong bản dựng.

### 6.1 Tổng số dòng không khớp giữa các màn

Bấm liền ba màn là thấy:

| Màn | Bản dựng nói | Cộng lại |
|---|---|---|
| 5, sau khi chọn file | `hoi-vien-thang-8.xlsx · 25 DÒNG` | 25 |
| 6, xem trước | `12 MỚI · 3 CẬP NHẬT · 7 KHÔNG ĐỔI` | **22** |
| 7, có dòng lỗi | `10 MỚI · 3 CẬP NHẬT · 7 KHÔNG ĐỔI` + `3 DÒNG KHÔNG GHI ĐƯỢC` | **23** |

Ba con số cho cùng một file. Chỗ sai: `:241` gõ cứng `25 DÒNG`, `:837-841` gõ cứng bộ ba tổng kết.

### 6.2 Màn 9 không đổi số khi đến từ nhánh lỗi

Render màn 9 luôn ra `ĐÃ GHI 22 HỘI VIÊN` + `12 mới · 3 cập nhật · 7 không đổi`, kể cả khi đi từ
màn 7 (chỉ 10 mới). `doneTags` gõ cứng ở `:936`; hero lấy `C.MEMBERS.length` ở `:768` — ra đúng 22
chỉ vì **trùng số**, không phải vì tính từ kết quả nhập.

### 6.3 Kiểm trùng SĐT không chuẩn hoá dấu cách — xác nhận, và là **lệch nội bộ**

Gõ thật vào ô SĐT ở màn 14, cùng một số điện thoại:

| Gõ vào | Bản dựng báo |
|---|---|
| `0983 550 128` | **TRÙNG VỚI HỘI VIÊN KHÁC** ✓ |
| `0983550128` | **"Số này chưa ai dùng"** ✗ |

Đây không phải giới hạn chung của bản dựng: ô tìm ở màn 11 gõ `0938 447` **có** chuẩn hoá dấu cách
và ra đúng người. Cùng một file, hai chỗ xử lý SĐT theo hai kiểu — `:748` so khớp chuỗi thô
(`x.p === S.editPhone`) trong khi `:783` có chuẩn hoá. Và đây đúng là chỗ `§ B4` cấm nuốt lỗi im
lặng: người dùng gõ liền số rồi bấm Lưu, tưởng xong.

### 6.4 Nút `Tiếp tục` ở bước 1 luôn sáng

Render: ô tên **trống**, nút `opacity: 1`, `disabled = false`. `w1Op: 1` gõ cứng ở `:906`, trong
khi `w4Op` ngay cùng dòng thì mờ đúng luật. `§ A6` đòi "gõ ô nhập thì nút bật".

Tuỳ: nếu tên trung tâm được phép để trống và lấy mặc định (`:738` có fallback thật) thì nút sáng
là đúng — nhưng lúc đó `§ B1` phải nói ra điều đó, chứ không để hai bước cùng wizard hành xử khác nhau.

---

**Xếp việc:** 6.1, 6.2, 6.3 và R3 (icon hỏng) nên sửa trong bản dựng — người xem sẽ vấp phải cả
bốn. 6.4, R1, R2 là câu hỏi luật, phải chốt ở `§ A4` / `§ B1` trước rồi mới sửa.

> **ĐÃ SỬA (lượt đóng 03a).** Cả bảy việc treo đã xử lý và kiểm chứng bằng render headless:
> - **6.1** — thêm `IMPORT.clean = {moi:15, capnhat:3, khongdoi:7}` làm nguồn duy nhất. Màn 5 hiện
>   `fileRowsLabel` (=25), pv = 15/3/7 (=25), pvErr = 12/3/7 + 3 lỗi (=25). Ba màn khớp.
> - **6.2** — `confirmImport` lưu `impResult` của nhánh đang đi; màn 9 lấy `dSum`/`dCounts` từ đó.
>   Nhánh sạch → "Đã ghi 25", nhánh lỗi → "Đã ghi 22". Không còn hằng số 22.
> - **6.3** — thêm `stripPhone()` chuẩn hoá dấu cách ở `dup`; `0983550128` và `0983 550 128` cùng
>   báo TRÙNG (id 13).
> - **6.4** — chọn phương án theo `§ A6` (không mở ngoại lệ mới): `w1Op = orgName.trim() ? 1 : .5`
>   và `next()` chặn khi ô tên trống. Opacity render 0.5 → 1.0 khi gõ.
> - **R1** — nav thêm `min-height:52px` + `box-sizing:border-box`; đo lại cả 4 mục = 52px đều.
> - **R2** — 44px của năm nút phụ nay **hợp lệ** theo `§ A4` mới (sàn 44px cho nút phụ `/admin`),
>   không cần đổi code.
> - **R3** — vẽ lại icon "Báo cáo" (biểu đồ cột + trục) và "Cài đặt" (bánh răng đủ vành); bbox
>   render ~18–20px, ngang hai icon bên cạnh.
> Luật R1/R2 đã nâng lên `00-he-thong.md § A4`.

## 7 · `designs/support.js` đã được commit

Trước lượt này `.gitignore` chặn `designs/support.js`, và `§ A7` ghi runtime "không có trong repo,
mở bằng trình duyệt thường sẽ ra trang trắng". Hệ quả thật: **không ai chạy được lượt kiểm `§ A8`**
— báo cáo này suýt nữa đóng lại mà chỉ có phần đọc source, đúng cái mà `staff-A8.md` đã chứng minh
là không đủ.

Đã bỏ chặn và commit file. Nó là file generated (`// GENERATED from dc-runtime/src/*.ts — do not
edit`), 69KB, và kéo React 18.3.1 + ReactDOM + Babel từ `unpkg.com` kèm SRI khi chạy — nghĩa là
**mở bản dựng vẫn cần mạng**, chỉ không cần runtime của Claude Design nữa.

Cách chạy lại lượt kiểm này về sau: phục vụ thư mục `designs/` qua HTTP cục bộ (mở thẳng `file://`
thì runtime báo lỗi fetch), rồi lái bằng Chromium headless — bảng nhảy nhanh ở `:587` cho phép
tới thẳng từng màn, và bề rộng khung đổi bằng cách set `style.width` trên div `844px`.

---

## Việc để lại

| Việc | Cho ai |
|---|---|
| ~~**Sửa 3 lỗi số liệu** (§ 6.1, 6.2, 6.3) + **2 icon nav hỏng** (R3)~~ — **xong**, kiểm bằng render | ~~trước khi đóng `03a`~~ |
| ~~**Chốt ngưỡng vùng chạm ở `§ A4`** (R1, R2, § 6.4)~~ — **xong**: `§ A4` tách 52px chính / 44px nút phụ `/admin`; nav ≥52px; nút bước 1 theo `§ A6` | ~~trước khi đóng `03a`~~ |
| **`/staff` hero còn làm theo cách cũ** — `§ 0a` vừa lật luật, `designs/staff.dc.html` chưa theo | lượt sửa riêng, không phải `03a` |
| **`§ A1` còn thiếu hai câu**: ô icon 42px lấy màu ông nội (§ 1.2); vật mô phỏng thế giới thật nằm ngoài hệ bốn tầng (§ 1.3) | khi viết `docs/DESIGN.md` |
| **Sage có được mang nghĩa phụ "cái đang diễn ra" không** (§ 2, cột hôm nay) | khi viết `docs/DESIGN.md` |
| **`§ B0b` viết lại theo ranh giới "màn nói về một người"** (§ 5) | trước lượt `03b` |
| **`q-A8.md` chưa từng tồn tại** dù `01-q-hoi-vien.md:7` có hứa — `docs/DESIGN.md` cần đủ bốn báo cáo | trước `docs/DESIGN.md` |

## Quyết định sản phẩm cần chốt

Lượt này **không** lộ ra quyết định nghiệp vụ mới nào phải gắn vào `docs/STORIES.yml`. Ba câu hỏi
gần nhất đều đã có dấu `QUYẾT ĐỊNH CẦN CHỐT` từ trước: nhập liệu khi đã đầy cap (`STORIES.yml:721`,
`M4-S01`), cửa sổ hiệu lực khi đóng băng thẻ (`:412`, `M2-S04`), và định dạng giờ mở cửa (`:496`,
`M2-S10`) — màn 3 của bản dựng đang giả định `HH:MM`–`HH:MM` cùng ngày, đúng nhánh đơn giản nhất
của câu hỏi đó.

Một quyết định **thiết kế** đã chốt trong lượt này và đã ghi ra ngoài repo tài liệu:
`DECISIONS.md § D13` — `/admin` trên màn rộng là một cột căn giữa, không bố cục lại.

---

# Báo cáo tự kiểm A8 — `/admin` báo cáo (lượt `03b`)

> Phần thêm vào chính file này (đúng yêu cầu prompt `03b`). Bản dựng: `designs/admin.dc.html`
> mở rộng từ 15 lên **27 màn**. Prompt: `docs/design/prompts/03b-admin-bao-cao.md`.
> **Báo cáo lượt 4/4.** Sau lượt này đủ bốn bản dựng — trừ `q-A8.md` chưa từng tồn tại (xem dưới).

## B0b · Hai tiền đề đã làm trước khi dựng

1. **`§ B0b` viết lại** (`03a-admin-nen-tang.md:41`) theo ranh giới "màn này đang nói về ai" thay
   cho "màn chi tiết hội viên" — đúng đề nghị `§ 5` ở trên. Hai màn mới nói về một người
   (`memberHist`, `trialConvert`) mang nhãn nhỏ là **tên người**; chín màn còn lại mang tên khu vực.
2. **Hoà số màn.** `03b` đánh số màn của nó từ #14, nhưng `SCREENS` đã dùng chỉ số 14/15 cho
   `phoneEdit`/`pause` (hai màn `§ B4` dựng thành màn đầy đủ ở `03a`). `SCREENS` là mảng phẳng nên
   11 màn mới **nối tiếp** thành vị trí 16–27 trong bảng nhảy dev; không đè, không đánh số lại màn cũ.
   Ánh xạ: 16 trang gom báo cáo (glue) · 17 xếp hạng · 18 sắp hết hạn · 19 lịch sử một hội viên ·
   20 học thử · 21 chuyển học thử · 22 tỉ lệ chuyển đổi · 23 bất thường · 24 nhật ký · 25 chạm trần ·
   26 nâng gói · 27 trạng thái chung.

## Đã giữ nguyên (ràng buộc "không viết lại")

Token màu, tên class inline, `state`/`renderVals`, ba slot đầu màn, thanh nav, và object dữ liệu
`03a` đều giữ nguyên. 11 màn mới **dùng lại** `MEMBERS`/`PROGRAMS`/`SCAN_POINTS`; dữ liệu mới đổ vào
đúng ba mảng `03a` đã chừa (`TRIAL_LEADS`/`RANKING`/`AUDIT_LOG`) cộng `ANOMALIES`. Hai tab nav
"Báo cáo"/"Cài đặt" trước để xám nay nối: Báo cáo → trang gom, Cài đặt → nâng gói.

## Lượt kiểm bằng mắt — đã chạy (Chromium headless, phục vụ HTTP cục bộ)

- **Bố cục 12 màn mới đạt ở 390px và 360px**: đo `scrollWidth` trong khung ép 360px, **0 phần tử
  tràn ngang** ở cả 12 màn. Một chỗ suýt cắt (tên quán quân "Huỳnh Thị Tuyết Mai" vượt 11px ở 360)
  đã sửa cho **xuống dòng** thay vì ellipsis — giữ mạch "chưa phải dùng ellipsis".
- **Ba slot đầu màn đúng `§ B0b`** (đọc từ render): nhãn nhỏ `memberHist` = `NGUYỄN THỊ HỒNG NHUNG`,
  `trialConvert` = `NGUYỄN HOÀI AN` (tên người); chín màn còn lại là tên khu vực. Hero là câu trạng
  thái/con số ở cả 12 màn.
- **Hành vi động (bấm thật)**: xếp hạng đổi quán quân theo tháng (T8 Huỳnh Thị Tuyết Mai 21 · T7
  Nguyễn Khánh Vy 23 · T6 Huỳnh Thị Tuyết Mai 22) và theo bộ lọc bộ môn (T8·Yoga → Nguyễn Khánh Vy
  19); thẻ sắp hết hạn 7→30 ngày đổi 4→5 dòng (số dẫn từ ngày hết hạn, không gõ cứng); nhật ký lọc
  "Anh Phát" còn 7/15 dòng đúng người; bất thường duyệt/bỏ giảm 3→2→…→hiện "Đã xử lý hết"; học thử
  đổi trạng thái ngay trên hàng (Đã liên hệ → Đã chuyển đổi); nút chuyển đổi sáng (lead không trùng).
- **Không có lỗi JS** trên console (chỉ một 404 favicon vô hại).

## Màu — đối chiếu `§ A2`

- **Vàng** cho việc *cần xử lý sớm*: khối quán quân xếp hạng (prompt `§ B1` yêu cầu "quán quân là
  khối vàng"), thẻ sắp hết hạn (tag ≤7 ngày), bất thường chờ duyệt, chạm trần 50. Đây là chỗ đáng
  bàn: vàng ở Checkino nghĩa "còn kịp, phải xử lý sớm" — hợp cho *sắp hết hạn / chờ duyệt / chạm
  trần*, nhưng **quán quân** không phải "cần xử lý". Prompt ép màu vàng cho quán quân, nên bản dựng
  theo prompt; ghi lại đây làm dữ liệu cho `docs/DESIGN.md`: **vàng đang mang hai nghĩa** ("cần xử
  lý" và "phần thưởng/nổi bật"), cần chốt một khi viết hệ thiết kế.
- **Sage** cho *đã xong / kết quả tốt*: thẻ tỉ lệ chuyển đổi, tag "đã chuyển đổi" ở học thử, empty
  state "đã xử lý hết" của bất thường — nhất quán với nghĩa sage ở `03a` (nền màn `done`).
- **San hô** cho *không phải lỗi* nhưng cần chú ý: khối "mất mạng" ở màn trạng thái chung.
- **Tím** (họ màu tiền, như ô "chỗ gói miễn phí" ở trang chủ) cho thẻ Gói Pro ở màn nâng gói.

## Chỗ phải tự quyết vì prompt không nói rõ (A8.4 — dữ liệu cho `docs/DESIGN.md`)

- **Tab "Cài đặt" trỏ vào đâu.** Prompt không có màn "Cài đặt" trong 11 màn. Nối tab này → **nâng
  gói**, vì với chủ free đó là thứ "cài đặt" đáng quan tâm nhất; `cap`/`states` để tới bằng bảng nhảy.
  Nếu sau này có màn cài đặt thật (đổi giờ mở cửa, sửa điểm quét) thì tab này nên trỏ vào đó.
- **Thêm một màn glue** ("trang gom báo cáo", vị trí 16) không nằm trong 11 màn — cần để tab "Báo
  cáo" có nơi đáp xuống và liệt kê 6 báo cáo. Đánh dấu là điều hướng, không phải màn nghiệp vụ.
- **Số ngày còn lại** ở "thẻ sắp hết hạn" tính bằng công thức thô `(tháng-8)*30 + (ngày-4)` từ chuỗi
  `exp`, vì `MEMBERS` không có trường ngày. Đủ để ngưỡng 7/14/30 dẫn số đúng chiều; ngày thật thì
  backend tính.
- **`cap`/`states` không có lối vào cố định trong nav** — `cap` bật theo ngữ cảnh (thêm hội viên khi
  đã 50), `states` là màn minh hoạ meta. Cả hai tới được qua bảng nhảy dev; `cap` cũng tới từ nút
  "nâng gói" ngược lại. Đúng bản chất tình huống của chúng.
- **Chữ nghĩa tiền/kỹ thuật** (`§ B5`): tránh hết từ kỹ thuật — dùng "nối với Google Sheet, sửa bên
  nào cũng khớp" thay cho "đồng bộ"; "nhận thẻ bằng mã gửi tin nhắn" thay cho "OTP".

## Việc còn lại sau lượt 03b

| Việc | Cho ai |
|---|---|
| **`q-A8.md` chưa từng tồn tại** dù `01-q-hoi-vien.md:7` hứa | trước khi viết `docs/DESIGN.md` |
| **`§ A1` còn thiếu hai câu** (ô icon 42px lấy màu ông nội; vật mô phỏng thật ngoài hệ bốn tầng) | khi viết `docs/DESIGN.md` |
| **Vàng mang hai nghĩa** (cần-xử-lý vs phần-thưởng, § Màu trên) và **sage nghĩa phụ "đang diễn ra"** | khi viết `docs/DESIGN.md` |
| **`/staff` hero chưa theo luật `§ A1b`** (đổi vì dữ liệu, không vì bộ lọc) | lượt sửa riêng `/staff` |
| **`design/tokens.css`, `styleguide.html`, skill `/design-screen`** | sau khi đủ bốn báo cáo |

## Quyết định sản phẩm cần chốt (lượt 03b)

Không lộ quyết định nghiệp vụ mới cho `STORIES.yml`. Màn nâng gói giả định chuyển khoản VietQR +
mã trung tâm, **không** cổng thanh toán — đúng `§ B1` và khớp free-tier chi phí biến đổi 0đ. Danh
sách lợi ích Pro lấy từ `DECISIONS.md § D6` (gỡ cap, mirror Sheet, đa cơ sở + phân quyền, Zalo,
member OTP), không thêm hứa hẹn mới.
