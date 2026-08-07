# Báo cáo tự kiểm A8 — `/q` (`designs/q.dc.html`)

> Báo cáo theo `00-he-thong.md § A8`. Bản dựng: `designs/q.dc.html` · Prompt: `docs/design/prompts/01-q-hoi-vien.md`
>
> **Đây là báo cáo lượt 1/4 — viết muộn.** Lượt `/q` chạy đầu tiên (05/08) và đã qua bốn đợt sửa;
> những gì nó chốt ra đã nâng thẳng lên `00-he-thong.md` (§ A1b ba slot đầu màn, § A2 token `--c-rust`
> + sáu luật màu, § A3 hero 36px) và `DECISIONS.md § D12`. Nhưng **báo cáo A8 thì chưa bao giờ được
> ghi ra** dù `01-q-hoi-vien.md:7` có hứa — nên đây là nó, viết bằng cách rà lại bản dựng đang có.
> `docs/DESIGN.md` viết sau khi đủ bốn báo cáo `*-A8.md`; đừng chắt lọc file này thành hệ thiết kế bây giờ.

## Trạng thái xử lý — đọc trước

Vì viết muộn, nhiều kết luận của lượt `/q` **đã được xử lý từ trước** qua các lượt sau. Ghi lại để
người đọc `DESIGN.md` không tưởng là việc còn treo:

| Mục | Tình trạng |
|---|---|
| Ba slot đầu màn (§ A1b), token màu (§ A2), hero 36px (§ A3) | **Đã nâng lên `00-he-thong.md` và `D12`.** `/q` là nơi ba luật này ra đời |
| `<helmet>` khai `a { color:#A9E5F1 }` (dòng 17) | **Đã vá 07/08** → `a { color:#F2F2EC; text-decoration:underline }`, hover giữ san hô. Vi phạm luật màu liên kết `§ A2` thêm sau lượt `/q`; chưa lộ vì `/q` không có thẻ `<a>`. Cùng loại lỗi đã sửa ở `staff-A8` |
| Lớp tiếng Anh + toggle vi/en (mặc định `en`) | **Chưa xử lý — xem § 4.1.** Đây là phần đáng bàn nhất của báo cáo này |

Không có trạng thái nào thiếu (§ 3 dưới: 11/11 dựng đủ). Không có việc gắn vào `STORIES.yml` từ lượt này.

## Lượt kiểm — rà source

Toàn bộ phần dưới là **đọc source** bản dựng hiện tại, đối chiếu `00-he-thong.md` §A1/§A1b/§A2/§A3 và
`01-q-hoi-vien.md` §B1–B4. Bản dựng `/q` đã qua bốn đợt render thật trong các phiên gốc (README lượt 1:
"đã chạy xong và đã qua bốn đợt sửa"); **lượt render không chạy lại trong phiên viết báo cáo này** —
runtime cần React nạp từ CDN, không nằm trong allowlist mạng của sandbox. Các số đo hình học dưới đây
là đọc từ style trong source, không phải đo `scrollWidth` mới.

---

## 1 · Luật bốn tầng hộp (§A1)

Không thấy vi phạm bốn điều cấm của §A1. Ba chỗ đáng ghi vì chúng là **lỗ hổng của tài liệu**, không
phải lỗi của bản dựng — trùng đúng hai câu mà `admin-A8.md` đã báo là `§ A1` còn thiếu:

### 1.1 Thẻ hội viên là **vật mô phỏng thật, nằm ngoài hệ bốn tầng**

Thẻ hội viên (dòng 80–100 gradient tím; 136–156 bản xám vô hiệu; 300–319 ở màn quét lại) mô phỏng một
**tấm thẻ nhựa thật**: aspect-ratio 1.586 (đúng tỉ lệ thẻ ISO/ID-1), chip SIM vẽ bằng SVG, số buổi cỡ
44px như số dập nổi. Nó **không** tuân bốn tầng hộp — nền là gradient (tầng khối cấm gradient nhiều họ,
nhưng đây là gradient một họ tím `#9E98F6→#6F66F0`, hợp `§ A2`), padding và bo góc theo "thẻ" chứ không
theo thang tầng khối. Đây là **vật neo** mà `01 § B2` gọi tên, và nó cố ý ngoài hệ — cùng loại với tờ
poster A4 (`admin`) và thanh trạng thái iOS giả. `§ A1` hiện **chưa có câu** nói "vật mô phỏng một đồ
vật thật (thẻ, poster, khung máy) được miễn luật bốn tầng". Đây là câu thứ nhất còn thiếu ở `§ A1`.

### 1.2 Ô icon vuông 42px lấy màu theo **ngữ cảnh**, không theo một luật riêng

Ô icon 42px (`border-radius:13px`) đổi nền theo nghĩa của khối chứa nó: **vàng `#F3C24A`** ở dải "sắp
hết hạn"/"sắp hết buổi" (dòng 103, 110 — cảnh báo còn kịp), **xám `#2A2D2A` trung tính** ở dải mất mạng
(357 — không phải lỗi hội viên nên không màu cảnh báo). Tức màu ô icon **kế thừa nghĩa của khối cha**,
không tự chọn. `§ A1` chưa nói ô icon 42px thuộc tầng nào và lấy màu ở đâu — đây là câu thứ hai còn
thiếu, khớp đúng ghi nhận của `admin-A8.md` và `staff-A8.md § 1.3`.

### 1.3 Tấm sáng sage bo `28px 28px 0 0` — khối hợp lệ, do prompt định

Tấm `#C4D4C1` chiếm nửa dưới màn nhập SĐT / học thử / thiết bị đã nhớ (dòng 50, 207, 273) có bo góc bất
đối xứng và tràn tới hai mép vùng nội dung. Nhìn thì lạ so với khối bo-24-đều, nhưng `01 § B1` **định
đích danh** ("tấm sáng --c-sage, bo 28px 28px 0 0") — lý do: nửa dưới sáng dễ đọc ngoài nắng. Ô nhập
trắng bên trong là tầng thẻ con. Không vi phạm.

### 1.4 Ngoài ba chỗ trên, không thấy vi phạm §A1

Header bar (§A1b), hai ô số san hô/tím, các dải `#1F211F` bo 24 với thẻ con `#2A2D2A` bo 18 — đều đúng
thang tầng.

---

## 2 · Màu (§A2) — đếm khối bão hoà từng màn

Trần `§ A2`: **không quá hai khối màu bão hoà mỗi màn**. Đọc từ `bgMap`/`fgMap` (dòng 477–478) và source
từng trạng thái:

| Màn | Khối bão hoà | Đếm | Nghĩa có đúng §A2? |
|---|---|---|---|
| 1 · nhập SĐT | ô giờ san hô + ô sĩ số tím | 2 | San hô = nhịp hiện tại ("đang mở cửa"); tím = con số có trần. Đúng |
| 2 · xong bình thường | thẻ hội viên tím | 1 | Nền màn sage = xong xuôi. Đúng |
| 3 · sắp hết hạn | thẻ tím + ô icon vàng nhỏ | 1 (+accent) | Vàng = còn kịp, phải xử lý sớm. Đúng |
| 4 · sắp hết buổi | thẻ tím + ô icon vàng nhỏ | 1 (+accent) | Như trên. Đúng |
| 5 · thẻ hết hạn | nền màn **rust** + chip vàng `HẾT HẠN` | (nền) +accent | Rust = hôm nay không vào được; thẻ **xám vô hiệu** (không tính bão hoà). `§ B2 #5` định đích danh chip vàng. Đúng |
| 6 · ngoài giờ | (không) | 0 | Cố ý không màu cảnh báo — chẳng có gì để xử lý. Đúng `§ B2` |
| 7 · học thử | tấm sage + chip bộ môn băng khi chọn | 0–1 | Không được trông như lỗi. Đúng `§ B3` |
| 8 · gửi học thử xong | (khối trắng trên nền sage) | 0 | Sage = xong. Đúng |
| 9 · thiết bị đã nhớ | ô giờ san hô + ô sĩ số tím | 2 | Như màn 1. Đúng |
| 10 · quét lại quá nhanh | thẻ tím + khối giờ trắng | 1 | Cố ý **không** đỏ/cảnh báo — không phải lỗi. Đúng `§ B2 #10` |
| 11 · mất mạng | (không) | 0 | Không phải lỗi hội viên → không màu cảnh báo. Đúng `§ B2 #11` |

Không màn nào vượt trần. Ba màn cố tình trắng cảnh báo (6, 10, 11) đúng như `§ B2` dặn.

**Một nuance màu để `DESIGN.md` chốt:** ô "24 · người đã tới" dùng **tím** (dòng 48, 271). `§ A2` cho
tím nghĩa "hạn mức và **sĩ số**", và `01 § B1` định đích danh tím cho ô này ("con số đếm được có trần").
"Người đã tới hôm nay" là lượt đếm trong ngày, không thật sự có trần như sĩ số/hạn mức 50 — tím ở đây
nghiêng về nghĩa "con số đáng đếm" hơn là "có trần". Bản dựng theo prompt; ghi lại làm dữ liệu.

---

## 3 · Trạng thái — 11/11 dựng đủ

`SCREENS` (dòng 464) có đúng 11 phần tử, khớp 1–1 với 11 mục `01 § B2`:

| # `§ B2` | Trạng thái | `screen` | Nền màn | Có |
|---|---|---|---|---|
| 1 | Nhập SĐT / **đang gửi** | `a1` | than | ✓ (đang gửi là **sub-state** cùng màn: spinner + `disabled` ô nhập, nền không đổi — đúng `§ B1`) |
| 2 | Xong — bình thường | `ok` | sage | ✓ |
| 3 | Xong — sắp hết hạn | `okexp` | sage | ✓ (thêm dải vàng + chip "còn 3 ngày" trên thẻ) |
| 4 | Xong — sắp hết buổi | `oklow` | sage | ✓ (`sessionsLeft` ép về 2, dòng 494) |
| 5 | Thẻ hết hạn | `expired` | rust | ✓ (thẻ xám vô hiệu + chip vàng `HẾT HẠN · 28/07`) |
| 6 | Ngoài giờ | `closed` | than | ✓ (nêu giờ mở `06:00–22:00`, không viết "Lỗi") |
| 7 | SĐT lạ → học thử | `trial` | than | ✓ (tên, SĐT, bộ môn tuỳ chọn, checkbox đồng ý) |
| 8 | Gửi học thử xong | `trialok` | sage | ✓ (cảm ơn + nói bước tiếp theo) |
| 9 | Thiết bị đã nhớ | `mem` | than | ✓ (tên + một nút to, **không** có thẻ — đúng ngoại lệ `§ B2`) |
| 10 | Quét lại quá nhanh | `again` | sage | ✓ ("Đã điểm danh lúc 18:42 rồi", không đỏ) |
| 11 | Mất mạng | `off` | than | ✓ (nút thử lại + gợi ý nhờ nhân viên) |

Hành vi động có thật: `submitPhone` (dòng 518) chờ 1100ms rồi định tuyến `0912345678 → ok`, số khác
`→ trial`; `memCheckin` (527) lần đầu → `ok`, tap lần hai → `again` (mô phỏng dedupe); `retry`/`submitTrial`
đổi màn đúng. Không trạng thái nào thiếu.

---

## 4 · Chỗ phải tự quyết vì prompt không nói rõ (A8.4 — phần quan trọng nhất)

### 4.1 **Toàn bộ lớp tiếng Anh + toggle vi/en + mặc định `en`** — quyết định tự phát lớn nhất

Bản dựng mang một `DICT` đầy đủ **hai ngôn ngữ** (dòng 387–462), một cặp nút EN/VI (370–371) và
**mặc định `en`** (dòng 383 `defaultLang default:"en"`, 469 `?? 'en'`). **Không prompt nào yêu cầu bản
tiếng Anh.** `00-he-thong.md:164` chỉ nói ngược lại: "Tiếng Việt dài hơn tiếng Anh ~15%… đừng thiết kế
vừa khít chữ tiếng Anh rồi vỡ khi dịch" — tức tiền đề là **thiết kế bằng tiếng Việt**, tiếng Anh chỉ là
mốc để chừa chỗ cho chữ Việt dài hơn. `PRD`/`DECISIONS` định vị sản phẩm cho trung tâm nhỏ ở VN, chủ và
hội viên đều người Việt.

Đây là mâu thuẫn cần chốt, không phải chi tiết nhỏ:

- **Cross-surface, mặc định chưa nhất quán:** `/q` mặc định `en` (dòng 469), `/staff` mặc định `vi`
  (`staff.dc.html:309`), `/admin` vừa được thêm toggle (05/08 → nay). Ba bề mặt ba kiểu.
- Nếu song ngữ **là** ý định sản phẩm thật → ripple lớn: mọi copy nhân đôi và phải giữ đồng bộ; ai
  dịch và duyệt bản dịch; `tokens.css`/`DESIGN.md`/styleguide phải mang cả hai và test nút/nhãn ở bản
  dài hơn; `/q` server-render (Thymeleaf) phải chọn ngôn ngữ bằng gì (query? Accept-Language? cột
  org?). **Phải thành một mục trong `DECISIONS.md`**, không quyết ngầm trong bản dựng.
- Nếu song ngữ **chỉ là** tiện ích demo (để trình bản dựng cho người xem không đọc tiếng Việt) → nên
  gỡ khỏi đặc tả sản phẩm, giữ mặc định **`vi`** ở cả ba bề mặt, và ghi rõ trong `DESIGN.md` rằng toggle
  là scaffold của bản dựng, ngoài sản phẩm — cùng loại với bảng nhảy trạng thái và khung máy iOS (§ 4.7).

→ **Đã đưa cho chủ dự án quyết**: chọn "ý định thật, ghi `DECISIONS.md`". Mục quyết định sẽ ghi phạm vi
(chỉ `/admin`? cả ba?), lý do, ai dịch, và cơ chế chọn ngôn ngữ ở `/q` server-render.

### 4.2 Header avatar: emoji 🏋️ (q, staff) vs chữ "C" (admin)

Ô avatar 42px trong header (§ A1b) chứa **emoji 🏋️** ở `/q` (dòng 36) và `/staff`, nhưng **chữ đơn
"C"** (monogram, `font-variation-settings:'wdth' 88`) ở `/admin`. `§ A1b` định nghĩa header là "nơi
chốn" nhưng **không nói ô avatar chứa gì**: emoji bộ môn? monogram tên trung tâm? logo do chủ tải lên?
Ba bề mặt của **cùng một trung tâm** đang hiện hai thứ khác nhau. Cần `DESIGN.md` chốt một nguồn (khả năng
đúng nhất: monogram/logo của **trung tâm**, vì header = nơi chốn = trung tâm, không phải bộ môn).

### 4.3 Dữ liệu demo cứng

Giờ trên khung máy (`clockMap` dòng 479: 18:41/18:42/18:44/05:12/18:45), ngày hết hạn `04/08/26` và
`28/07`, SĐT `0912 345 678` **kiêm hai vai** (số hiển thị trên thẻ **và** khoá định tuyến "hội viên đã
biết" ở dòng 522), "24 người đã tới", "gói /24 buổi", tên "Nguyễn Thị Hồng Nhung", "Boxing". Tất cả là
chuỗi cứng để xem; giá trị thật do backend cấp.

### 4.4 Logic định tuyến khi "đang gửi"

`digits === '0912345678' → ok`, còn lại `→ trial` (dòng 522–523), độ trễ giả 1100ms. Sản phẩm thật:
server tra SĐT trong roster của `scan_point` đó. Đây là mô phỏng, không phải luật.

### 4.5 Meta trên thẻ ghép "Quận 7 · Boxing" — chi nhánh + bộ môn

Thẻ hội viên hiện `Quận 7 · Boxing` (dòng 85). Nếu một hội viên học **nhiều bộ môn** (`D7`
`member_program` nhiều–nhiều) thì hiện bộ môn nào? Bản dựng chọn một. `DESIGN.md`/backend cần luật:
bộ môn chính? tất cả? bỏ hẳn khỏi thẻ?

### 4.6 Chip bộ môn ở màn học thử là **chọn một** (toggle)

`sports` (dòng 480–484) cho chọn **một** bộ môn, bấm lại thì bỏ. `01 § B3` nói "bộ môn (tuỳ chọn)"
nhưng không nói một hay nhiều. Với một lead học thử, chọn-một là hợp lý; ghi lại vì đó là quyết định.

### 4.7 Nút "Đổi số" / "Về màn đầu" thêm ở nhiều màn done

`01 § B2 #9` chỉ định đích danh link "Không phải bạn? Đổi số" ở màn thiết bị đã nhớ. Bản dựng thêm nút
`goB1` ("Đổi số"/"Về màn đầu") vào cả `ok`/`expired`/`trialok`/`again` (dòng 116, 161, 250, 331). Là
lối thoát điều hướng hợp lý cho bản dựng nhiều-màn-một-file; sản phẩm server-render thì mỗi màn là một
lần tải trang, cần xem lại có nút này không.

### 4.8 Ngưỡng cảnh báo hardcode

"Còn 3 ngày" (chip), "còn 2 buổi" (`oklow` ép `sessionsLeft=2`) là số cứng; ngưỡng thật (mấy ngày thì
gọi "sắp hết hạn", mấy buổi thì "sắp hết buổi") chưa có ở đâu — backend/`DESIGN.md` cần định.

### 4.9 Scaffold ngoài sản phẩm

Khung máy iOS (thanh giờ + 4G + pin, dòng 25–30), bảng nhảy trạng thái + nút EN/VI + dòng gợi ý (367–380)
là **công cụ xem bản dựng**, không phải UI sản phẩm. `DESIGN.md` phải loại chúng khi đặc tả.

### 4.10 Màn ngoài giờ chọn khung "chưa tới giờ mở" (buổi sáng)

`closed` đặt đồng hồ `05:12` và giọng "hẹn bạn sáng mai" (dòng 406/443) — tức khung **trước giờ mở**.
`§ B2 #6` chỉ nói chung "ngoài giờ". Sau giờ đóng (23:00) thì câu "hẹn sáng mai" hơi lệch; bản dựng chọn
một trường hợp. Nhỏ, nhưng ghi để `DESIGN.md` biết có hai chiều ngoài-giờ.

---

## Việc còn lại sau lượt `/q` (tổng hợp cho `DESIGN.md`)

| Việc | Cho ai |
|---|---|
| **`§ A1` còn thiếu hai câu**: (1.1) vật mô phỏng thật ngoài hệ bốn tầng; (1.2) ô icon 42px lấy màu theo khối cha | khi viết `docs/DESIGN.md` — trùng ghi nhận `admin-A8`/`staff-A8` |
| **Header avatar** chứa gì, một nguồn cho cả ba bề mặt (§ 4.2) | khi viết `docs/DESIGN.md` |
| **Tím cho "người đã tới"** — nghĩa "sĩ số" hay "con số đáng đếm" (§ 2 nuance) | khi viết `docs/DESIGN.md` |
| **`design/tokens.css`, `styleguide.html`, skill `/design-screen`** | sau khi đủ bốn báo cáo (giờ đã đủ 4/4) |

## Quyết định sản phẩm cần chốt (lượt `/q`)

- **Song ngữ vi/en** (§ 4.1) — chủ dự án đã chọn "ý định thật" → viết mục `DECISIONS.md` (phạm vi, lý
  do, ai dịch, cơ chế chọn ngôn ngữ ở `/q` server-render + Thymeleaf). Ripple tới `tokens.css`/`DESIGN.md`.
- **Bộ môn hiển thị trên thẻ** khi hội viên nhiều bộ môn (§ 4.5, liên quan `D7`) — luật backend.
- **Ngưỡng "sắp hết hạn"/"sắp hết buổi"** (§ 4.8) — con số cụ thể, backend.
