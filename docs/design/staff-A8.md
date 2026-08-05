# Báo cáo tự kiểm A8 — `/staff` (`designs/staff.dc.html`)

> Báo cáo do lượt dựng `02` tự sinh theo `00-he-thong.md § A8`, đã qua duyệt.
> Bản dựng: `designs/staff.dc.html` · Prompt: `docs/design/prompts/02-staff-co-giao.md`
>
> **Đây là báo cáo lượt 2/4.** `docs/DESIGN.md` viết sau khi có đủ bốn bản dựng và bốn báo cáo
> `*-A8.md` — xem `DECISIONS.md § D12 · Chưa làm`. Đừng chắt lọc file này thành hệ thiết kế bây giờ.

## Trạng thái xử lý — đọc trước

Báo cáo bên dưới giữ gần như nguyên văn vì nó là **nguồn cho `docs/DESIGN.md`**. Nhưng ba kết luận
của nó đã được duyệt lại:

| Mục | Báo cáo nói | Kết quả duyệt |
|---|---|---|
| §3.1 | Dải mất mạng thiếu câu "Danh sách lưu lúc 17:02", nên thêm dòng thứ ba | **Không làm.** Chip eyebrow đã đổi thành `DANH SÁCH 17:02` nên § B1 đã được nói ra bằng chữ đầy đủ. Thêm lại vào dải là dựng lại đúng chỗ trùng lặp mà lượt sửa trước vừa bỏ (`§ A2`: khi chữ đã nói rồi thì đừng thêm tín hiệu thứ hai) |
| §4.1 mục 2 | "Bản dựng hiện đang là `LƯU`, chưa đổi" | **Đã lạc hậu.** Đã đổi sang `DANH SÁCH` / `LIST` (`staff.dc.html:223`, `:246`). Phần lập luận vì sao `LƯU` sai thì giữ — nó là lý do của quyết định |
| Cần ghi vào `DECISIONS.md` mục 3 | "Bản dựng giả định hàng chờ **không** bền qua reload — cần chốt" | **Đã chốt từ trước, báo cáo không đọc backlog.** `M2-S13` yêu cầu outbox **IndexedDB** với `client_event_id`, và `M3-S02` yêu cầu roster trong IndexedDB để "F5 lúc offline không mất danh sách". Hàng chờ **bền**. Dải "có bản mới" vẫn cần vì cái mất là những cú tap **chưa kịp ghi vào outbox**, đúng như `M3-S03` AC 2 |

Bốn quyết định sản phẩm còn lại đã được gắn dấu `QUYẾT ĐỊNH CẦN CHỐT:` vào `docs/STORIES.yml`
(`M2-S13` ×3, `M3-S02` ×1) để `/status` dò được và `/plan` buộc phải xử lý. Ba lỗ hổng hệ thiết kế
(§1.1, §1.3, và màu liên kết ở §2) đã vá thẳng vào `00-he-thong.md`. Trạng thái thiếu ở §4.5 đã
thêm vào `02-staff-co-giao.md § B2` thành mục #15.

## Lượt kiểm bằng mắt — 9/9 đạt

Toàn bộ phần rà bên dưới là **đọc source**. Bản dựng đã qua thêm một lượt render thật, chín bước,
**không bước nào vỡ và không sửa một dòng thiết kế nào**. Số đo đáng ghi lại:

- **Header tên dài** `TRUNG TÂM ANH NGỮ SAO MAI`: 2 dòng, cao 36px, `scrollWidth − clientWidth = 0`,
  pill `Quận 7` (74×40) cùng hàng cách tên 11px. Giống hệt ở 360px. Đây là bước dễ vỡ nhất theo
  `§ A1b` và nó đạt.
- **Hàng nhãn nhỏ** cao đúng 22px ở cả 15 màn nên hero không nhảy. Dài nhất `KICKFIT · HÔM NAY`
  123px + chip 118px + gap 10 = **251px trên 358px** khả dụng — chưa cần tới ellipsis.
- **Vùng danh sách cuộn** ở màn kẹp hai đầu nhất (13, có dải vàng trên + dải vàng đáy) còn 326px
  cho nội dung 626px, cuộn tốt, dải không đè hàng cuối.
- **Màn 14** vừa khít 844px (`scrollHeight = clientHeight`), không cần cuộn.
- **Ở 360px**: cả 15 màn `contentOverflowX = 0`.

Lượt kiểm báo về năm việc **ngoài checklist**. Cả năm đã xử lý:

| Việc | Xử lý |
|---|---|
| **Chip `Kickfit` bị cắt còn "Ki"** ở cả 390px và 360px — mà màn 15 lại buộc phải bấm đúng chip đó, nên đường duy nhất tới trạng thái này gần như vô hình | **Đã sửa, hai phần.** (a) Chốt thứ tự chip: `Tất cả` luôn đầu, còn lại **A→Z** → `Kickfit` lên thứ tư, đọc được không cần cuộn. Không xếp theo số người hôm nay vì bộ môn sẽ nhảy chỗ giữa buổi mà cô giáo tap theo vị trí đã nhớ. (b) Thêm **vệt mờ 26px** ở rìa phải dãy chip để nói "còn bộ môn nữa" — gradient trong **một** họ màu (than trong suốt → than), đúng `§ A2`. Việc này cũng chốt luôn một mục đang để ngỏ ở §4.7 |
| **`<helmet>` vẫn khai `a { color:#A9E5F1 }`** — vi phạm sẵn luật màu liên kết mới thêm vào `§ A2`, chưa lộ chỉ vì file không có thẻ `<a>` nào | **Đã sửa:** `a { color:#F2F2EC; text-decoration:underline }`, hover giữ san hô nhạt |
| **`§ B2 #9` mâu thuẫn với quyết định ở §3.1** — luật vẫn đòi dải mất mạng nói "Danh sách lưu lúc 17:02", bản dựng thì không | **Đã sửa tài liệu cho khớp quyết định**, không để treo. `§ B2 #9` giờ ghi dải mang **số lượt chờ**, và `§ B1` gạch thứ ba nói rõ chỗ duy nhất của mốc giờ là chip đầu màn, kèm lý do phải có tiền tố `DANH SÁCH` |
| **Nút Tải lại vô hiệu chỉ bằng màu chữ**, DOM vẫn nhận cú bấm (§3.3) | **Chuyển thành AC của `M3-S03`**: `disabled` + `aria-disabled` thật. Đây là việc của code, không phải của bản dựng — trong bản dựng thì đổi màu là đủ để xem |
| **Chip mốc giờ tắt ở màn 11** (§4.6 đề xuất bật) | **Đã bật:** `chipShow: isList \|\| S.empty === 'class'`. Vẫn tắt ở màn 10 (chưa từng có danh sách) và màn 14 (chưa cài xong app) |

Sau lượt này `/staff` **đóng**. Hai luật còn treo (§4.4 hero đổi mẫu số, §4.6 nhãn nhỏ rỗng mang hai
nghĩa) không phải việc của `/staff` — chúng là luật của `§ A1b`, phải dựng `/admin` rồi quyết một
lần cho cả hệ.

---

Rà bản dựng hiện tại (đã qua một lượt sửa: bỏ khái niệm lớp, tick chờ đổi sang vàng, s5 tách khỏi
s4, nút Tải lại vô hiệu đúng luật, thông báo "không khớp" chỉ hiện khi gõ tìm). Đối chiếu với
`00-he-thong.md` §A1/§A1b/§A2 và `02-staff-co-giao.md` §B0b/§B2 bản mới.

---

## 1 · Luật bốn tầng hộp (§A1)

### 1.1 Lỗ hổng thật của tài liệu: **hàng danh sách chưa có tầng**

Hàng tên (màn 1–9, 12, 13) là: `min-height 56px`, nền `#1F211F`, bo **18px**, padding `8/10/8/16`,
nằm **trực tiếp trên tầng 0** (nền than `#131413`) — không có khối tầng 1 nào bao quanh.

Chiếu vào bảng §A1 thì nó không khớp ô nào:

- **không phải tầng 1 (khối)**: không có tiêu đề khối, bo 18px chứ không phải 24px, padding
  8–16px chứ không phải 16–17px;
- **không phải tầng 2 (thẻ con)**: định nghĩa tầng 2 là "ô nằm **trong** khối tầng 1", mà nó
  không nằm trong khối nào;
- **không phải tầng 3 (nhãn nhỏ)**: không phải badge/chip, không bo 999px.

Cùng loại vật đó, cũng đang lơ lửng ngoài bảng: **ô tìm** (nền `#1F211F`, bo 999px), **dải mất
mạng** (`#1F211F`, bo 18px), **thẻ "không thấy tên nào khớp"** (`#1F211F`, bo 18px), **dải hàng
chờ / đang gửi / đã lưu tất cả** (bo 999px, cao 56px, đặt thẳng trên tầng 0), **dải "có bản mới"**
(`#F3C24A`, bo 18px, đặt thẳng trên tầng 0). Tức đây không phải một ngoại lệ lẻ mà là **một lớp
vật hạng nhất của bề mặt này**: vật lặp hoặc dải đơn, đặt trực tiếp trên nền màn, không có khối
cha.

Đây là **lỗ hổng của §A1**, không phải lỗi của bản dựng: nếu ép hàng danh sách thành khối tầng 1
thì phải bo 24px + padding 16px cho 12 hàng liền nhau — danh sách sẽ đọc ra như 12 khối nội dung
rời rạc, và mất luôn cảm giác "một danh sách". Bản dựng chọn 18px là chọn đúng về hình, nhưng
tài liệu đang không đỡ cho lựa chọn đó.

**Đã chốt: cách B — thêm tầng `1b` vào bảng §A1**, kèm hai ràng buộc để `1b` không thành cửa sau
phá luật: vật tầng 1b **không được chứa** khối tầng 1 (con của nó chỉ được là chữ hoặc nhãn tầng
3); và dải màu bão hoà ở tầng 1b **vẫn tính** vào hạn "tối đa hai khối màu bão hoà một màn".
Cách A (ép vào tầng 1, đổi bo thành 24px) bị loại: 12 hàng bo 24px + padding 16px sẽ cao thêm
~8px/hàng (≈100px, hơn một hàng rưỡi trên màn 844px) và hàng nào cũng "nặng" như một khối có tiêu
đề. Cách B hợp thức hoá đúng thứ cả `/q` lẫn `/staff` đang làm, và sẽ còn dùng lại ở `/admin`
(bảng dữ liệu = hàng thẻ, §A4).

### 1.2 Màn 14 (hướng dẫn iPhone) — **không vi phạm**

Chuỗi lồng là: tầng 0 `#131413` → khối `#1F211F` (bo 24px, padding 17px, có tiêu đề khối 20px
"Mở bằng Safari, làm 2 bước") → hai thẻ con `#2A2D2A` (bo 18px, padding 12–13px).

Không vi phạm, vì ba lý do:

1. **Ràng buộc "thẻ con chỉ dùng sắc nhạt của chính màu khối cha" được thoả.** §A2 khai `--c-ink`
   có **hai** sắc nhạt: `#1F211F` và `#2A2D2A`. Khối cha là ink, thẻ con dùng sắc nhạt sâu hơn của
   chính ink — không mượn tím, vàng hay san hô.
2. **Không quá 3 tầng màu** — đếm được đúng 3 nền: `#131413` → `#1F211F` → `#2A2D2A`. Hết tầng thì
   bản dựng chuyển sang dùng chữ (`tIosCap`, `tIosAndroid` là chữ dim trên nền, không thêm nền).
3. **Khối tầng 1 không chứa khối tầng 1 khác màu** — `#2A2D2A` là thẻ con (bo 18px, không tiêu đề),
   không phải khối thứ hai.

Cùng logic đó cũng bảo vệ hai màn rỗng (10, 11): `#1F211F` bọc `#2A2D2A`, hợp luật.

### 1.3 Chỗ thứ hai bảng §A1 không đỡ: **ô icon vuông 42px**

Ở màn 14 có hai ô icon 42px, bo **13px**: một nền `#F4573F` (icon Chia sẻ, nằm trong thẻ con
`#2A2D2A`) và một nền `#F3C24A` (dấu `!`, nằm trực tiếp trong khối `#1F211F`).

- Nếu gọi chúng là **tầng 3 (nhãn nhỏ)** thì sai hình: §A1 buộc tầng 3 bo 999px và nền
  `rgba(255,255,255,.55)` / `rgba(0,0,0,.20)` — đây là bo 13px và nền bão hoà.
- Nếu gọi chúng là **tầng 2 (thẻ con)** thì ô vàng vi phạm ràng buộc 2: khối cha là ink, thẻ con
  lại mượn vàng.

Nó cùng bản chất với **logo tròn 42px** ở header (nền trắng đặt thẳng trên tầng 0) và **tick tròn
44px** trong hàng danh sách (nền sky/vàng) — cả `/q` cũng làm y vậy. **Đã chốt: thêm định nghĩa
"ô hình 42–44px"** vào §A1 — vật vuông/tròn chỉ chứa **một** icon hoặc một chữ, được dùng màu bão
hoà bất kỳ theo nghĩa §A2 kể cả khác màu cha, bo 13px (vuông) hoặc 999px (tròn), **không tính** là
khối bão hoà khi đếm hạn hai khối (nó là nhãn, không phải mảng). Không chốt thì người code F3 sẽ
phải đoán, và sẽ đoán khác nhau ở mỗi bề mặt.

### 1.4 Ngoài ba chỗ trên, không thấy vi phạm

Rà cả 15 màn: không có khối tầng 1 chứa khối tầng 1 khác màu; không chỗ nào quá 3 tầng màu; không
màu bão hoà nào tràn kín màn (dải vàng luôn có than trên/dưới, mảng lớn nhất là dải cao 56px);
header + eyebrow + hero giữ nguyên vị trí qua mọi màn, hàng eyebrow có `min-height:22px` nên hero
không nhảy khi eyebrow trống (màn 10, 11, 14).

---

## 2 · Màu (§A2) — đếm khối bão hoà **từng màn**

Quy ước đếm dùng trong bảng: **"khối bão hoà" = một mảng nền coral / rust / purple / vàng / sage
cỡ khối hoặc cỡ dải.** Không tính: tick tròn 44px, chip, pill, ô icon 42px (đều là nhãn — xem
§1.3), và không tính viền.

| Màn | Preset | Khối bão hoà | Đó là gì |
|---|---|---|---|
| 1 Danh sách hôm nay | s1 | **0** | tick sky 44px là nhãn |
| 2 Lọc theo bộ môn | s2 | **0** | chip đang chọn nền sky = nhãn tầng 3 |
| 3 Không có bộ môn nào | s3 | **0** | |
| 4 Vừa tap một tên | s4 | **0** | tick chờ là **viền gạch vàng + nền vàng 12%** — không phải mảng bão hoà |
| 5 Tap lại để bỏ tick | s5 | **0** | |
| 6 N lượt chờ gửi | s6 | **1** | dải hàng chờ vàng (56px, bo 999px) |
| 7 Đang gửi | s7 | **1** | dải đang gửi vàng (dải hàng chờ bị thay, không cộng thêm) |
| 8 Gửi xong | s8 | **1** | dải "Đã lưu tất cả" nền sage `#C4D4C1` |
| 9 Mất mạng | s9 | **0** | dải mất mạng nền `#1F211F`, không bão hoà |
| 10 Rỗng — chưa nhập ai | s10 | **0** | |
| 11 Rỗng — chưa ai cần điểm danh | s11 | **0** | |
| 12 Tìm nhanh theo tên | s12 | **0** | |
| **13 Có bản mới** | s13 | **2** | **dải "có bản mới" (vàng, trên cùng) + dải hàng chờ (vàng, đáy)** |
| 14 Cài trên iPhone | s14 | **0** | hai ô icon 42px (coral, vàng) là nhãn — nếu ai đó tính là khối thì vẫn đúng 2, chưa vượt |

**Màn 13 có đúng hai khối vàng — vẫn trong hạn.** Trần §A2 là "tối đa hai", nên s13 **chạm trần
nhưng không vượt**. Đây là màn duy nhất chạm trần, và nó chạm có chủ ý: hai dải cùng một họ vàng
vì chúng cùng một nghĩa §A2 ("cần xử lý và **còn kịp**") và cùng một việc — dải trên nói *chưa
được tải lại*, dải dưới nói *lý do chưa được tải lại*. Nếu về sau muốn thêm bất cứ mảng bão hoà
thứ ba nào vào màn danh sách (ví dụ dải "thẻ hết hạn"), **màn 13 là chỗ sẽ vỡ trước** — ghi lại
để lượt sau biết.

**Tick chờ không tính là khối.** Nó là hình tròn 44px, `border: 2.5px dashed #F3C24A` +
`background: rgba(243,194,74,.12)` + icon đồng hồ. Vàng ở đây là **viền và chữ**, không phải mảng
nền bão hoà. Nó cũng thoả luật "màu không bao giờ là tín hiệu duy nhất" theo cách mạnh nhất:
chờ vs đã lưu khác nhau **về hình** (gạch rời + đồng hồ ↔ nền đầy + dấu ✓), khác nhau **về chữ**
(dòng phụ `Boxing · chờ gửi · 18:33` ↔ `Boxing · đã lưu · 18:31`), rồi mới khác nhau về màu —
đúng yêu cầu §B1 "hai trạng thái khác nhau về hình, không chỉ khác màu".

**Xanh băng chỉ còn một nghĩa.** Sau khi tick chờ đổi sang vàng, rà toàn file: sky `#A9E5F1` chỉ
xuất hiện ở tick **đã lưu**, chip bộ môn **đang chọn**, mục nav **đang chọn**, và nút VI/EN **đang
chọn**. Cả bốn đều là "đã chọn / đã tick" — đúng §A2, và **không có** chỗ nào dùng sky làm nút bấm
(§A2: "xanh băng không bao giờ là nút"). Dải hàng chờ và dải đang gửi tuy bấm được nhưng nền vàng,
không phải sky.

Một chỗ cần biết: `<helmet>` khai `a { color:#A9E5F1 }` / `a:hover { color:#F98872 }`. Hiện bản
dựng **không có thẻ `<a>` nào**, nên chưa phát sinh nghĩa thứ hai cho sky; nhưng nếu F3 thêm liên
kết thật thì sky sẽ mang thêm nghĩa "bấm được" — trái §A2. **Đã chốt màu liên kết riêng** trong
§A2: kem `#F2F2EC` + gạch chân, không dùng sky.

**Nền màn chỉ có một giá trị.** Cả 14 màn đều nền than `#131413`; `/staff` **không** dùng sage hay
rust làm nền màn ở bất cứ đâu. Điều đó đúng với bề mặt này:

- **Rust = "hôm nay không vào được"**, là câu trả lời cho **một hội viên**. `/staff` không bao giờ
  trả lời về một người — nó là danh sách. Nếu một hội viên bị chặn thì thông tin đó phải nằm
  **trong hàng của người đó** (chip chữ), không thể nhuộm cả màn, vì 11 người còn lại vẫn vào
  được. Nhuộm màn rust ở `/staff` là nói sai nghiệp vụ.
- **Sage = "xong xuôi"**, và `/staff` **không có** trạng thái xong xuôi. Danh sách còn mở suốt buổi:
  gửi xong 2 lượt không có nghĩa là điểm danh xong, vì học viên vẫn đang vào. Nên "xong xuôi" ở đây
  chỉ đúng ở cỡ một **dải tạm** ("Đã lưu tất cả", tự ẩn sau ~1,9s) — và bản dựng làm đúng vậy.
- Suy ra: `/staff` là bề mặt **một-nền-duy-nhất**. Điều này khác `/q` (nơi rust và sage đều là nền
  màn hợp lệ), nên đáng ghi vào DESIGN.md như một tính chất của bề mặt, không phải một thiếu sót.

Một giá trị ngoài bảng §A2: `body` (mặt bàn quanh khung máy, để xem trên desktop) dùng `#0B0C0B`,
sâu hơn than. Nó **ở ngoài sản phẩm**, không phải nền màn — F3 không có vật này.

---

## 3 · Trạng thái chưa dựng — rà đủ 14 mục §B2 đối chiếu s1–s14

| # | §B2 yêu cầu | Preset | Đủ? | Ghi chú |
|---|---|---|---|---|
| 1 | Danh sách hôm nay: bộ môn + ngày, sĩ số, hàng tên + tick tròn phải | s1 | ✅ | eyebrow `HÔM NAY · 04/08`, hero `2/12 ĐÃ TỚI`, không còn `LỚP 18:00` |
| 2 | Dãy chip, chip chọn nền sky, có chip "Tất cả" | s2 | ✅ | eyebrow đổi thành `BOXING · HÔM NAY` |
| 3 | Không có bộ môn → **ẩn hẳn** dãy chip | s3 | ✅ | `showChips` false, và dòng phụ hàng cũng bỏ phần bộ môn |
| 4 | Tick hiện ngay, hàng đổi, **đếm tăng** | s4 | ✅ | `arrived` đếm cả tick chờ nên hero nhảy 2/12 → 3/12 ngay khi tap |
| 5 | Tap lại để bỏ tick, không hỏi lại | s5 | ✅ | s5 để hàng 3 ở trạng thái **đã lưu** nên tap lại là huỷ một tick đã gửi — khác hình hẳn s4 |
| 6 | Dải đáy "2 lượt đang chờ gửi", phân biệt rõ với tick đã lưu | s6 | ✅ | |
| 7 | Dải chuyển sang trạng thái đang chạy | s7 | ✅ | spinner thật (`@keyframes ckspin`), bấm dải ở s6 cũng chạy ra s7 → s8 |
| 8 | Dải biến mất hoặc "Đã lưu tất cả", mọi tick chuyển đã lưu | s8 | ✅ | tick đổi sang sky, dòng phụ đổi `đã lưu · 18:34`, dải tự ẩn |
| 9 | Dải trạng thái mạng **+ "Danh sách lưu lúc 17:02"**. Vẫn tap được | s9 | ✅ | xem 3.1 — mốc giờ nằm ở chip `DANH SÁCH 17:02`, không lặp lại ở dải |
| 10 | Rỗng — chưa nhập ai | s10 | ✅ | đúng câu §B6 |
| 11 | Rỗng — hôm nay chưa ai cần điểm danh, **khác hẳn #10** | s11 | ✅ | hero + thân bài + câu phụ đều khác |
| 12 | Ô tìm, **lọc ngay khi gõ**, gõ không dấu vẫn ra | s12 | ✅ **gõ thật có lọc** | xem 3.2 |
| 13 | Dải trên cùng "Có bản mới — bấm tải lại", cấm tự đổi bản | s13 | ✅ | xem 3.3 |
| 14 | Hướng dẫn cài iPhone, có hình minh hoạ SVG | s14 | ✅ | 2 bước + hình thanh dưới Safari + câu "mở một lần ở nơi có mạng" |
| 15 | Bộ môn hôm nay không ai học | s15 | ✅ | chip `Kickfit` (0 hội viên), hero `0/0`, thẻ chỉ đường về `Tất cả` — xem 4.5 |

### 3.1 #9 — mốc giờ lưu danh sách nằm ở chip, không lặp lại ở dải

§B2 #9 và §B1 gạch đầu dòng thứ ba đều yêu cầu UI nói **"Danh sách lưu lúc 17:02"**. Trong bản
dựng, mốc giờ đó sống ở **chip eyebrow `DANH SÁCH 17:02`**, và có mặt ở **mọi** màn danh sách chứ
không riêng màn mất mạng. Dải mất mạng chỉ có hai dòng: tiêu đề "MẤT MẠNG — VẪN ĐIỂM DANH ĐƯỢC"
và dòng phụ "1 lượt sẽ gửi khi có mạng".

Lượt dựng đề xuất thêm dòng thứ ba `Danh sách lưu lúc 17:02` vào dải, lập luận rằng khi mất mạng
người dùng phải tự liên hệ chip ở đầu màn với dải ở đáy màn. **Đã duyệt: không thêm.** Lý do:

- Chip đã nói đủ chủ thể (`DANH SÁCH`) và mốc giờ (`17:02`) — §B1 yêu cầu UI *nói ra* mốc giờ,
  không yêu cầu nói ra ở dải mất mạng. Chỗ nào cũng được, miễn có.
- Thêm lại vào dải là dựng lại đúng chỗ trùng lặp mà lượt sửa trước vừa bỏ, và §A2 nói thẳng:
  "khi chữ đã nói rồi thì đừng thêm tín hiệu thứ hai".
- Chip hiện ở **mọi** màn danh sách nên mốc giờ luôn có, không phải "xuất hiện đúng lúc xấu nhất".

Phần còn lại của #9 đủ: `netLabel` đổi thành "Không có mạng", vẫn tap được (không chặn `tap`),
dải hàng chờ bị ẩn khi offline (`stripQueue` yêu cầu `!S.offline`) và số lượt chờ dồn về dòng phụ
của dải mất mạng — hợp lý, vì offline thì "Gửi ngay" là nút bấm không làm gì được.

### 3.2 #12 đã kiểm: **gõ thật có lọc**, không chỉ là preset dựng sẵn

Kiểm trong logic: `onQuery` ghi thẳng vào `state.query`; mỗi lần render, `shown` được tính lại bằng
`roster.filter(m => !q || this.norm(m.n).includes(q))`; `norm()` bỏ dấu bằng `NFD` +
`̀-ͯ` và ánh xạ `đ/Đ → d/D`. Nên gõ `hong nhung`, `dang thuy`, `do van` đều ra kết quả
ngay từng ký tự. Preset s12 chỉ là **điểm vào có sẵn** cho người xem, không phải thay thế cho hành
vi. Thông báo `noMatch` cũng gắn với `q.length > 0` nên chỉ hiện khi đang gõ.

Hai giới hạn của cách lọc này, F3 nên biết: tìm là `includes` trên **cả chuỗi họ tên đã bỏ dấu**,
nên gõ `nhung hong` (đảo thứ tự) **không** ra; và ô tìm lọc **trong** bộ môn đang chọn, không phải
toàn trung tâm.

### 3.3 #13 — nút Tải lại vô hiệu đúng luật, nhưng chỉ vô hiệu bằng **màu chữ**

`reload()` không làm gì khi còn lượt chờ, và `reloadFg` đổi từ kem `#F2F2EC` sang dim `#9AA096`
(§A2: vật vô hiệu = ink + dim). Nhưng nút **vẫn nhận được cú bấm** và không có `aria-disabled` hay
thuộc tính `disabled` — về hình là "vô hiệu", về máy là "bấm không phản hồi". Ở màn dùng một tay
giữa lớp, "bấm mà không có gì xảy ra" dễ bị đọc là app treo. Đề xuất F3: `disabled` thật + giữ
nguyên màu dim, hoặc để nút bấm được và nó **chạy gửi** rồi mới tải lại.

**Tổng: 15/15 mục có màn bấm tới được.**

---

## 4 · Chỗ phải tự quyết vì prompt không nói rõ

Mục này viết cho người sẽ code F3 (`M2-S13`, `M3-S01..S03`). Mỗi gạch đầu dòng: bản dựng làm gì ·
tài liệu nói gì · cần chốt ra sao.

### 4.1 Chip mốc giờ ở eyebrow là **tự sinh** — và tiền tố của nó cũng tự sinh

§A1b chỉ nói "chip bên phải = **mốc giờ**". §B0b chỉ nói "chip mốc giờ lưu bên phải". **Không tài
liệu nào nói chip đó viết chữ gì** — chỉ nói nó mang một mốc giờ. §B1 thì đòi UI nói "Danh sách lưu
lúc 17:02". Nên toàn bộ hình thức của chip là quyết định của bản dựng.

1. **Vì sao phải có tiền tố.** Nếu chip chỉ ghi `17:02` thì nó nằm ngay cạnh hàng giờ `18:33` của
   thanh trạng thái và ngay trên các dòng phụ `· 18:31`, `· 18:33` trong hàng tên — ba loại mốc giờ
   khác nghĩa nhau (giờ tải danh sách / giờ hiện tại / giờ tick), cùng cỡ chữ nhỏ. Không có tiền
   tố thì `17:02` sẽ bị đọc thành "giờ lớp" — đúng thứ §B0b cấm tuyệt đối.
2. **Vì sao không dùng `LƯU`.** Từ `LƯU` **đụng nghĩa** với dòng phụ `đã lưu · 18:31` của hàng tên:
   ở dòng phụ, "lưu" = *lượt tick đã gửi lên server*; ở chip, "lưu" = *danh sách hội viên tải về
   máy lúc nào*. Hai nghĩa ngược chiều nhau (một là đẩy lên, một là lấy về) mà dùng chung một từ,
   trên cùng một màn, cách nhau 80px. **Đã chốt và đã sửa: `DANH SÁCH 17:02`** (en `LIST 17:02`) —
   dài hơn 5 ký tự nhưng nói đúng chủ thể, và khớp với câu §B6 "Danh sách lưu lúc 17:02" nên không
   phải học hai cách gọi.
3. Chip này hiện ở **mọi** màn danh sách (kể cả khi có mạng). Không tài liệu nào nói nó chỉ hiện
   khi offline; bản dựng chọn hiện luôn để mốc giờ không "xuất hiện đúng lúc xấu nhất". **Giữ.**

### 4.2 Ngày `04/08` là **chuỗi cứng**, không phải ngày thật

`T.vi.today = 'HÔM NAY · 04/08'` và `T.en.today = 'TODAY · AUG 4'` là chuỗi hằng trong bản dựng.
Nó **không** đọc `Date`, không đổi theo máy, và bản EN dùng khuôn `AUG 4` chứ không phải `04/08`.
**Đừng ai code F3 theo chuỗi này.** F3 phải sinh từ ngày máy, và cần chốt: khuôn `dd/MM` cho VI,
múi giờ nào (đề xuất `Asia/Ho_Chi_Minh` cố định, không theo máy — nếu theo máy thì cô giáo đi công
tác sẽ điểm danh vào ngày hôm trước), và cắt ngày lúc mấy giờ (nửa đêm? hay 04:00, vì lớp tối
muộn?). Cùng loại chuỗi cứng: giờ tick mới luôn `18:33`, giờ gửi xong luôn `18:34`, giờ thanh
trạng thái luôn `18:33`, `savedAt` mặc định `17:02`.

> Phần timezone/cắt ngày trùng với `QUYẾT ĐỊNH CẦN CHỐT` đã có ở `M2-S10` (spec timezone giờ mở
> cửa). Chốt một lần ở đó, dùng lại ở đây.

### 4.3 Thanh trạng thái iOS giả là **khung máy mô phỏng**, không phải chỉ báo của sản phẩm

Hàng trên cùng (`18:33` bên trái, `4G` / `Không có mạng` + hình cục pin bên phải) nằm **ngoài**
vùng nội dung, thuộc khung điện thoại 390×844 mà §A6 yêu cầu để xem trên desktop. Nó **không phải
một thành phần của sản phẩm**.

Hệ quả cho F3, nói thẳng: đây là **PWA chạy toàn màn hình** (§B0), nên hàng này do **iOS** vẽ, ứng
dụng không đọc được và không sửa được. Chữ `4G` / `Không có mạng` trong bản dựng chỉ để người xem
trên desktop hiểu màn 9 đang mô phỏng cảnh gì. **Chỉ báo mất mạng duy nhất của sản phẩm là dải
`#1F211F` ở đáy màn** — nên dải đó không được coi là dư thừa, và không được ẩn đi để "đỡ chật":
nếu ẩn, sản phẩm thật sẽ không còn chỗ nào nói là đang mất mạng.

### 4.4 Hero đổi mẫu số khi lọc bộ môn, và eyebrow là thứ **duy nhất** nói điều đó

§B0b nói hero = "số đã tới trên sĩ số", ví dụ `12/24`. Nó **không nói sĩ số của gì khi đang lọc**.
Bản dựng tự quyết: mẫu số theo **danh sách đang lọc** — chọn chip `Boxing` thì hero đổi từ `2/12`
thành `2/5`. Cách còn lại (giữ mẫu số toàn trung tâm, `2/12`, chỉ lọc hàng bên dưới) bị loại vì
khi đó hero mô tả một tập khác với thứ đang thấy trên màn.

**Rủi ro đọc sai, thật và cụ thể:** cô giáo tap chip Boxing lúc đầu buổi, nhìn xuống một giây thấy
`2/5` và tin cả trung tâm chỉ có 5 người phải điểm danh — trong khi thực tế là 12. Mẫu số vừa nhảy
từ 12 xuống 5 mà **thứ duy nhất giải thích cú nhảy đó là eyebrow `BOXING · HÔM NAY` viết ở 10.5px,
opacity .62** — tức thành phần nhỏ nhất và mờ nhất trên màn đang phải đỡ nghĩa cho thành phần to
nhất (36px). Đó là một cặp lệch trọng lượng.

Đề xuất, **không** phải thêm chữ vào hero (§A3 giữ hero 36px 2 dòng, thêm chữ là phải thu cỡ —
cấm): nâng eyebrow lên khi và chỉ khi **đang lọc** — cụ thể là bỏ `opacity .62` cho phần tên bộ
môn và cho nó nền sky nhạt như chip đang chọn, để "màn này đang bị lọc" đọc được cùng nhịp với con
số. **Chưa chốt** — để lượt `03a`/`03b` xem `/admin` có gặp cùng vấn đề không rồi quyết một lần
cho cả hệ, vì nó là luật của §A1b chứ không riêng `/staff`.

### 4.5 Chip bộ môn lọc ra danh sách rỗng: hiện **không có thông báo nào**

Câu "Không thấy tên nào khớp. Thử gõ ngắn hơn." bị buộc vào `q.length > 0`, nên nó **chỉ** hiện khi
đang gõ tìm. Nếu chọn một bộ môn không có ai đăng ký hôm nay thì: dãy chip vẫn đó, ô tìm vẫn đó,
vùng danh sách **trống trơn không một chữ**, hero `0/0 ĐÃ TỚI`.

Dữ liệu giả hiện tại **không chạm** vào trường hợp này — cả 3 bộ môn (Boxing 5, Yoga 4, Gym 3) đều
có người, đúng yêu cầu §B5, nên xem bản dựng sẽ không bao giờ thấy lỗi này. **Thực tế sẽ chạm
ngay**: trung tâm khai 4–6 bộ môn, mỗi ngày chỉ 2–3 bộ môn có lớp, nên "chọn bộ môn hôm nay không
ai học" là thao tác thường ngày, không phải ca biên.

Tài liệu **không có câu nào** cho trường hợp này: §B2 #10 là "chưa nhập ai" (cấp trung tâm), #11 là
"hôm nay chưa ai cần điểm danh" (cấp ngày), còn đây là cấp **ngày × bộ môn** — mục thứ ba, chưa
tồn tại. **Đã thêm thành §B2 mục #15** với câu: *"Hôm nay chưa ai đăng ký Boxing. Bấm **Tất cả**
để xem cả danh sách."* — giọng §B6, nói lý do và chỉ đường theo §A5, đặt vào chỗ thẻ `noMatch`
đang dùng, cùng hình.

**Đã dựng: preset `s15`, nav mục 15.** Cách làm: gộp hai thẻ rỗng thành một (`emptyHint` +
`emptyHintText`) vì chúng cùng hình và chỉ khác chữ — `q.length > 0` thì lấy câu "không thấy tên
nào khớp", còn lại lấy câu #15. Thêm bộ môn **`Kickfit` không có hội viên nào** vào dãy chip để
trạng thái này chạm tới được; điều đó cũng làm rõ luôn một điểm §4.7: **dãy chip sinh từ danh sách
`program`, không sinh từ `MEMBERS`** — một bộ môn có 0 người vẫn phải hiện chip. Dữ liệu giả vẫn
đủ §B5 (12 hội viên, 3 bộ môn có người). Hero ở màn này là `0/0 ĐÃ TỚI`.

### 4.6 §B0b cho eyebrow mang bộ môn thay vì tên người — ngoại lệ này **có mâu thuẫn ở 3 màn**

§A1b chốt "eyebrow là chỗ của **người**, một slot chỉ mang một nghĩa". §B0b cố ý phá luật đó cho
`/staff` (eyebrow mang bộ môn + ngày) và yêu cầu báo cáo nếu thấy mâu thuẫn. Sau khi dựng đủ 14
màn, câu trả lời là: **ngoại lệ đứng vững ở 11 màn danh sách, nhưng lộ ra ở 3 màn không có danh
sách.**

Ở **màn 10, 11, 14**, `eyebrow = ''` — hàng eyebrow rỗng, chỉ còn `min-height:22px` giữ chỗ cho
hero không nhảy. Chip mốc giờ cũng tắt (`chipShow = isList`). Vấn đề không phải "trống trơn thì
xấu", mà là **hai luật cho ra hai lý do khác nhau cho cùng một khoảng trống**, và người code F3 sẽ
không biết theo lý do nào:

- Theo §A1b, eyebrow trống nghĩa là **"chưa biết là ai"** — một trạng thái tạm, sẽ có nội dung khi
  biết người.
- Theo §B0b, eyebrow trống ở `/staff` nghĩa là **"không có danh sách nào để lọc"** — một trạng thái
  cấu trúc, sẽ **không bao giờ** có nội dung ở ba màn đó.

Cùng một khoảng trống, hai nghĩa. Cụ thể hơn ở từng màn:

- **Màn 14 (hướng dẫn iPhone) là chỗ mâu thuẫn rõ nhất.** Nó không phải một trạng thái của danh
  sách — nó là một màn *hướng dẫn cài đặt*, có tiêu đề khối riêng, và vẫn phải giữ header (§B0b:
  "không có ngoại lệ nào cho header"). Ở màn này cả eyebrow **và** chip đều rỗng, tức hai trong ba
  slot đầu màn không dùng, nhưng vẫn chiếm chỗ — 22px trống giữa header và hero mà không mang gì.
- **Màn 10 và 11** thì trống có lý hơn (thật sự chưa có danh sách để nói bộ môn nào), nhưng chip
  mốc giờ tắt ở đây lại **mất một thông tin có ích**: màn 11 ("hôm nay chưa có ai") là đúng lúc cô
  giáo cần biết *danh sách này tải lúc mấy giờ* — vì "chưa có ai" rất có thể chỉ là do danh sách cũ.

Đề xuất chốt (chọn một, đừng để mỗi màn tự xử): **giữ ngoại lệ §B0b**, và bổ sung một dòng vào
§A1b — *"eyebrow rỗng là trạng thái hợp lệ và cố định ở những màn không có tập nội dung để mô tả;
nó vẫn giữ min-height"*. Kèm hai sửa nhỏ: **bật lại chip mốc giờ ở màn 11** (không bật ở 10 và 14 —
màn 10 chưa từng có danh sách, màn 14 chưa cài xong app), và ở màn 14 xem lại có nên để eyebrow
mang `HƯỚNG DẪN` hay không — **khuyến nghị: không**, vì eyebrow mang chữ tĩnh sẽ mở đường cho việc
nhồi tiêu đề màn vào eyebrow, đúng thứ §A1b cấm. **Chưa chốt** — cùng loại với §4.4, để quyết một
lần sau khi dựng `/admin`.

### 4.7 Các quyết định nhỏ khác — liệt kê cho đủ, để DESIGN.md không phải đoán lại

- **Dòng phụ hàng tên** = `bộ môn · trạng thái · giờ` (`Boxing · đã lưu · 18:31`). §B2 chỉ nói hàng
  có "tên + nút tick"; toàn bộ dòng phụ là tự thêm, để thoả §B1 ("tick nào đã lưu, tick nào còn
  chờ" phải nói bằng chữ). Khi trung tâm không khai bộ môn thì bỏ phần bộ môn (suy từ §B2 #3).
  Hệ quả: hàng có dòng phụ cao hơn hàng chưa tick — danh sách "răng lược" khi mới điểm danh nửa lớp.
  Đã chấp nhận, vì tên vẫn là thứ to nhất trong hàng (17px/700 so với 12px/600) — §B0.
- **Cả dải hàng chờ bấm được**, không riêng nút `GỬI NGAY`. §B2 #6 chỉ nói "dải ở đáy". Vùng chạm
  toàn dải 56px×toàn chiều rộng là cho tay một ngón (§B0); nút `GỬI NGAY` bên trong chỉ còn vai trò
  **nhãn** nói rằng dải bấm được.
- **Màu chữ dòng phụ của dải mất mạng** nâng từ dim `#9AA096` lên kem `#F2F2EC`, 14px/700. Prompt
  không nói màu. Lý do: số lượt đang chờ khi mất mạng là thông tin phải đọc được dưới nắng trong
  một giây (§B0); giữ dim thì 700 vẫn không đủ.
- **Toast "Đã lưu tất cả" tự ẩn sau ~1,9s**, và `gửi` mô phỏng mất 1,4s. §B2 #8 cho phép "biến mất
  **hoặc** báo Đã lưu tất cả" — bản dựng làm cả hai (báo rồi biến mất). Thời lượng là tự chọn.
- **Nhãn chip bộ môn là chuỗi cứng** `SPORTS = ['Boxing','Gym','Kickfit','Yoga']` trong
  `renderVals`, không sinh từ `MEMBERS` — và **đó là đúng**: `Kickfit` có 0 hội viên nhưng vẫn phải
  hiện chip, vì chip sinh từ bảng `program`. F3 sinh từ `program` thật.
  **Thứ tự đã chốt: `Tất cả` luôn đầu, còn lại A→Z.** Loại "xếp theo số người hôm nay" vì bộ môn sẽ
  nhảy chỗ giữa buổi trong khi cô giáo tap theo vị trí đã nhớ; loại "theo thứ tự chủ trung tâm khai"
  vì nó không đoán được từ phía cô giáo. Dãy chip **cuộn ngang** khi vượt bề rộng — hợp lệ, `§ A4`
  chỉ cấm cuộn ngang với *bảng dữ liệu*, không cấm với bộ lọc — và có **vệt mờ 26px** ở rìa phải để
  nói còn chip nữa. **Còn để ngỏ:** có cắt bớt khi trung tâm khai 10 bộ môn hay không.
- **Bản EN toàn bộ là tự dịch** (`Nobody to check in`, `No sports set`, …). Prompt chỉ yêu cầu
  tiếng Việt; bản EN thêm vào để kiểm chiều dài nhãn theo §A3 ("tiếng Việt dài hơn ~15%"). Nó
  **không phải** cam kết đa ngữ của sản phẩm — đừng lấy làm nguồn dịch.
- **Bảng nhảy nhanh 14 trạng thái** (bên phải khung máy) và nút VI/EN là **dụng cụ xem bản dựng**
  theo §A6, không phải UI sản phẩm. F3 bỏ hẳn.
- **Logo tròn 42px hiện là emoji 🏋️** — chỗ giữ chỗ cho logo thật của trung tâm, không phải quyết
  định thiết kế.
- **Không có thanh nav nổi** (§A4 có mô tả "thanh nav nổi hình viên thuốc"). `/staff` chỉ có một
  việc — điểm danh — nên không dựng nav. Nếu sau này `/staff` có thêm màn thứ hai (ví dụ xem lịch
  sử) thì nav phải xuất hiện, và nó sẽ ăn ~72px đáy màn, đúng chỗ dải hàng chờ đang đứng. Cần biết
  trước khi thiết kế màn thứ hai.

---

## Quyết định sản phẩm cần chốt — đã gắn vào backlog

Bốn điều dưới đây là quyết định **sản phẩm**, không phải thẩm mỹ. Bản dựng đang **ngầm giả định**
chúng để có gì mà vẽ. Theo `CLAUDE.md`, quyết định chưa chốt được thì gắn dấu `QUYẾT ĐỊNH CẦN CHỐT:`
vào AC của story để `/status` dò được và `/plan` buộc phải xử lý — **không** viết sẵn một `D13` mà
chưa ai quyết.

| # | Câu hỏi | Gắn vào |
|---|---|---|
| 1 | Cái gì quyết định "ai cần điểm danh hôm nay"? §B2 #11 chỉ có nghĩa nếu tồn tại khái niệm *danh sách theo ngày*, mà D7 chỉ có `program` + `scan_point` — không bảng nào giữ ai học ngày nào. Bản dựng lấp bằng cách coi cả 12 hội viên là danh sách hôm nay | `M2-S13` |
| 2 | Huỷ một tick **đã gửi lên server** có được phép không? §B0 + §B2 #5 nói "huỷ được, không hỏi lại", và preset s5 dựng đúng cảnh đó — tức cần một lệnh xoá/vô hiệu một lượt điểm danh đã ghi, kèm giới hạn thời gian, ghi vết, và cách `/admin` đếm | `M2-S13` |
| 3 | Hội viên hết hạn / bị chặn hiện thế nào trong danh sách `/staff`? §A2 có rust = "hôm nay không vào được" nhưng §B2 không có mục nào cho ca này, và cả 12 hội viên giả đều hợp lệ. `M2-S07` chỉ phủ phía `/q` | `M2-S13` |
| 4 | "Danh sách lưu lúc 17:02" đo cái gì — lần tải danh sách thành công cuối, lần mở app cuối, hay lần server đổi danh sách? Ba nghĩa ra ba con số, và §B1 dùng mốc này để cô giáo "đoán ai bị thiếu". Kèm: cũ bao lâu thì cảnh báo, cảnh báo bằng gì | `M3-S02` |

**Đã loại một đề xuất:** lượt dựng đề nghị chốt thêm *"lượt tap còn trong máy có sống qua reload
không"*. Đã chốt từ trước — `M2-S13` yêu cầu outbox **IndexedDB** với `client_event_id`, `M3-S02`
yêu cầu roster trong IndexedDB để "F5 lúc offline không mất danh sách". Hàng chờ **bền**. Dải "có
bản mới" ở màn 13 vẫn cần, vì cái mất khi tự đổi bản là những cú tap **chưa kịp ghi vào outbox** —
đúng như `M3-S03` AC 2.
