# Hệ thiết kế Checkino — `DESIGN.md`

Spec thiết kế **giữ lâu dài**, tổng hợp từ bốn lượt dựng UI (`/q` 11 màn, `/staff` 15 màn,
`/admin` 27 màn) và bốn báo cáo tự kiểm `docs/design/*-A8.md`.

## §0 · Vai trò và phạm vi

**Đây là nguồn sự thật của hệ thiết kế Checkino cho giai đoạn code sản phẩm.** Khi màu, tầng
hộp, slot đầu màn, hay hình dáng một màn lệch nhau giữa các nơi, file này thắng.

Ranh giới với tài liệu khác:

| File | Vai trò | Quan hệ với DESIGN.md |
|---|---|---|
| `DECISIONS.md` | *Tại sao* — quyết định + lý do | Thắng về lý do. DESIGN.md thi hành các quyết định D12–D15. |
| `docs/STORIES.yml` | *Việc phải làm* — acceptance criteria | Chi tiết mức việc **chỉ** ở đây. DESIGN.md **không** phải checklist. |
| `docs/design/prompts/00-he-thong.md` + `01/02/03*` | Prompt build-time cho bốn lượt dựng | **Lịch sử.** Đã đóng vai trò; DESIGN.md hấp thụ luật chung của chúng ([D15](../DECISIONS.md)). |
| `designs/{q,staff,admin}.dc.html` | Ba bản dựng Claude Design | Prototype để xem, **không** phải code sản phẩm. Tra khi cần chi tiết một màn. |

DESIGN.md mô tả **hình dáng và ý nghĩa**; nó không liệt kê việc và không lặp lại lý do đã ghi
ở `DECISIONS.md`. Ba tài liệu ràng buộc sản phẩm (không mã 6 số ở `/q`, một SĐT một hội viên,
bộ môn tuỳ chọn, cap 50, nhập không xoá ai, thông báo từ chối phải chỉ đường…) sống ở
`00-he-thong.md § A5` và `PRD.md` — không chép lại đây.

---

## §1 · Bốn tầng hộp — luật quan trọng nhất

Mỗi phần tử thuộc **đúng một tầng**, và tầng quyết định màu nền, bo góc, padding. Vi phạm luật
này là lý do một giao diện "đẹp nhưng không đồng nhất".

| Tầng | Là gì | Nền | Bo góc | Padding |
|---|---|---|---|---|
| **0 · Màn hình** | nền toàn màn | `--c-ink` `#131413` | — | 16px hai bên |
| **1 · Khối** | một nhóm nội dung có tiêu đề | một màu khối ở §2 — **trừ `--c-rust`**, màu đó chỉ làm nền màn | 24px | 16–17px |
| **1b · Hàng danh sách / dải đơn** | vật **lặp** (hàng tên) hoặc **dải đơn** (ô tìm, dải trạng thái) đặt **trực tiếp trên tầng 0**, không tiêu đề | sắc nhạt của `--c-ink` (`#1F211F`) — hoặc một màu bão hoà §2 nếu dải đó **mang nghĩa** | 18px, hoặc 999px nếu cao ≤ 56px | 8–16px |
| **2 · Thẻ con** | ô số / ô nhập nằm **trong** khối tầng 1 | sắc **nhạt** của **chính** màu khối cha | 18px | 13–14px |
| **3 · Nhãn nhỏ** | badge, chip, pill trong thẻ con | `rgba(255,255,255,.55)` hoặc `rgba(0,0,0,.20)` | 999px | 3px 9px |
| **3b · Ô hình 42–44px** | vật vuông/tròn chứa **đúng một** icon hoặc một chữ (logo header, tick hàng danh sách, ô icon bước hướng dẫn) | màu bão hoà bất kỳ theo nghĩa §2, **được phép khác màu cha** | 13px (vuông) hoặc 999px (tròn) | — |

**Vì sao có `1b`:** một danh sách 12 hàng mà mỗi hàng là khối tầng 1 sẽ đọc ra như 12 khối rời
rạc, mất cảm giác "một danh sách". Nhưng hàng danh sách cũng không phải thẻ con (tầng 2 nằm
*trong* khối tầng 1). Hai ràng buộc để `1b` không thành cửa sau phá luật: (a) vật tầng 1b
**không chứa** khối tầng 1 — con nó chỉ được là chữ, nhãn tầng 3, hoặc ô hình tầng 3b; (b) dải
màu bão hoà ở tầng 1b **vẫn tính** vào hạn hai khối bão hoà.

**Vì sao có `3b`:** nó là **nhãn**, không phải mảng — nên được mượn màu khác cha (một tick vàng
trong hàng nền than là đúng) và **không tính** khi đếm hạn hai khối bão hoà. Đừng nhét vào tầng
3 (tầng 3 buộc bo 999px + nền mờ; ô hình dùng màu đặc).

### Bốn điều cấm — không ngoại lệ

1. **Khối tầng 1 không chứa khối tầng 1 khác màu.** Muốn hai màu thì đặt hai khối cạnh nhau ở
   tầng 0, không lồng.
2. **Thẻ con chỉ dùng sắc nhạt của màu cha.** Thẻ con trong khối san hô dùng `#F98872`, không
   mượn tím hay vàng.
3. **Không lồng quá 3 tầng màu.** Hết tầng 3 thì dùng chữ, không thêm nền.
4. **Màu bão hoà không bao giờ tràn kín màn hình.** Luôn phải có nền than bao quanh — hoặc là
   một khối, hoặc là mảng chiếm phần trên rồi nhường lại phần dưới. Tràn viền là hỏng.

### Hai câu bổ sung (chốt từ bốn báo cáo A8)

`00-he-thong.md § A1` thiếu hai câu; cả ba lượt dựng đều tự quyết giống nhau, nay chốt thành luật:

- **(1a) Vật mô phỏng đồ vật thật được miễn luật bốn tầng.** Thẻ hội viên (thẻ nhựa), poster A4
  in ra, khung máy iOS là **ảnh của một đồ vật thế giới thật**, không phải hộp UI. Chúng theo
  hình dáng của vật thật (thẻ ID-1 aspect 1.586, giấy A4 aspect 1/1.414) và **không tính** vào
  hạn "tối đa hai khối bão hoà một màn". Xem §Phụ lục cho đặc tả từng vật.
- **(1b) Ô hình tầng 3b lấy màu theo *nghĩa*, không theo màu cha.** Ô 42–44px được mượn màu ông
  nội / màu của ngữ cảnh khối bao ngoài, chọn theo nghĩa §2 (ô cảnh báo `!` nền vàng trong hàng
  nền than là đúng). Đây là hệ quả trực tiếp của việc 3b là *nhãn*, không phải mảng.

---

## §2 · Màu — nghĩa cố định của từng token

Màu **không** được chọn cho đẹp. Mỗi màu mang **một** nghĩa; dùng sai nghĩa là lỗi. Tám token
(bảy màu + `--c-dim` cho chữ phụ):

| Token | Mã | Sắc nhạt (thẻ con) | Chữ trên nền | Nghĩa cố định |
|---|---|---|---|---|
| `--c-ink` | `#131413` | `#1F211F` / `#2A2D2A` | `#F2F2EC` | nền mặc định, khối trung tính, **và vật đã vô hiệu** |
| `--c-coral` | `#F4573F` | `#F98872` | `#2A0C06` | **số chính · nhịp/trạng thái đang diễn ra · không bao giờ là lỗi** |
| `--c-rust` | `#8E2C1B` | — | `#F2F2EC` | **bị chặn, hôm nay không vào được. CHỈ làm nền màn** |
| `--c-purple` | `#6F66F0` | `#9E98F6` | `#0D0838` | **con số về người: sĩ số + sức chứa/hạn mức** |
| `--c-yellow` | `#F3C24A` | `#F8DC96` | `#2C1F03` | **cần xử lý và CÒN KỊP** |
| `--c-sage` | `#C4D4C1` | `#DCE7DA` | `#14180F` | **xong xuôi / đã hoàn tất, mảng dịu** |
| `--c-sky` | `#A9E5F1` | — | `#052430` | **đã chọn / đã tick** |
| `--c-dim` | `#9AA096` | — | — | chữ phụ trên nền than, và chữ trên vật đã vô hiệu |

> **Vàng = vẫn vào được, nhưng phải xử lý sớm. Rust = hôm nay không vào được.
> San hô không bao giờ là lỗi — nó là nhịp của hiện tại.**

### Ba chỗ nghĩa màu bị nhập nhằng — đã chốt

Bốn báo cáo A8 dồn lại ba token mang hai nghĩa. `00 § A2` từng tự nới nghĩa (thêm "và thành
tích" cho vàng, "sĩ số" mơ hồ cho tím) và các bản dựng theo đó. Chốt lại như sau — **bảng trên
đã là nghĩa đúng**, ba mục dưới giải thích vì sao và ghi ripple cần sửa:

- **Vàng chỉ một nghĩa: "cần xử lý và còn kịp".** Bỏ nghĩa "thành tích". **Quán quân / xếp hạng
  không dùng khối vàng** — quán quân không phải việc "cần xử lý". Đánh dấu quán quân bằng
  **icon huy chương/vương miện + số hạng** trên nền than (vận dụng luật "màu không bao giờ là
  tín hiệu duy nhất" — dùng chữ/icon thay vì một khối màu). *Ripple:* `admin.dc.html` màn xếp
  hạng (#17) còn dùng khối vàng cho quán quân, cần sửa ở lượt sửa bản dựng.
- **Sage chỉ một nghĩa: "xong xuôi / đã hoàn tất".** Bác nghĩa phụ "đang diễn ra". Cái *đang
  diễn ra / là hôm nay* thuộc **coral** (nhịp hiện tại). *Ripple:* cột "hôm nay" trong biểu đồ
  7 ngày ở trang chủ `/admin` (`admin.dc.html:892`) đang tô sage — phải đổi sang **coral**.
- **Tím = "con số về người".** Phủ **cả** tally trong ngày ("24 người đã tới") **lẫn** con số có
  trần (`memberCount/50`, số buổi `X/24`, sĩ số roster). Phân biệt với coral: coral là *nhịp/
  trạng thái* của khoảnh khắc; tím là *con số đếm người*. Bản dựng giữ nguyên, không phải sửa.

### Sáu luật màu thêm

1. **Màu không bao giờ là tín hiệu duy nhất.** Mọi ô màu phải có chữ đi kèm (mù màu, nắng gắt,
   in đen trắng). Hệ quả ngược: **khi chữ đã nói rồi thì đừng thêm tín hiệu thứ hai** — thẻ hết
   hạn có chip `HẾT HẠN · 28/07` là đủ, không gạch ngang số buổi nữa.
2. **Vàng làm nền thì chữ phải đậm màu** (`#2C1F03`), không dùng chữ trắng — tương phản không đạt.
3. **Một màn tối đa hai khối màu bão hoà.** Nhiều hơn thì không còn gì nổi bật. (Ô hình 3b và vật
   mô phỏng đồ thật không tính — xem §1.)
4. **Nền màn chỉ có đúng ba giá trị**: than (mặc định) · sage (xong xuôi) · rust (bị chặn). Không
   màu nào khác được làm nền màn. Rust **chỉ** làm nền màn, không bao giờ làm khối.
5. **Gradient chỉ đi trong một họ màu** (sắc nhạt → chuẩn), ví dụ `#9E98F6 → #6F66F0`. Gradient
   bắc cầu hai họ là trộn hai nghĩa, vật đó hết đọc được.
6. **Nút chính:** nền sáng → nút than `#131413`; nền than hoặc rust → nút kem `#F2F2EC`. Nút phụ
   = chữ gạch chân, không nền. **Xanh băng không bao giờ là nút** — chỉ mang nghĩa "đã chọn/tick".
   **Liên kết (`<a>`) dùng kem `#F2F2EC` + gạch chân**, không dùng xanh băng.

---

## §3 · Ba slot đầu màn và luật hero

Phần đầu mỗi màn có ba slot, **mỗi slot một nghĩa**:

| Slot | Nội dung | Đổi theo trạng thái? |
|---|---|---|
| **Header bar** | logo tròn 42px + **tên tổ chức** 17px + pill cơ sở | **Không.** Giống hệt mọi màn |
| **Nhãn nhỏ (eyebrow)** | **tên người**; chip bên phải = mốc giờ | Có |
| **Hero tầng 0** | **câu trạng thái**, 36px | Có |

### Ba điều cấm

1. **Tên tổ chức không bao giờ nằm ở eyebrow.** Nó chỉ sống ở header. Eyebrow là chỗ của *người*.
2. **Header không mang chữ hành động** (`ĐIỂM DANH`, `XONG RỒI`) hay icon trạng thái (✓, 👋).
   Header trả lời "đây là đâu", hero trả lời "đang xảy ra chuyện gì". Header đứng yên tuyệt đối
   qua mọi màn.
3. **Hàng eyebrow giữ `min-height` cố định kể cả khi trống**, để hero không nhảy khi chuyển màn.

### Hai luật treo từ lượt `/staff` — đã chốt

- **Eyebrow trống chỉ một nghĩa: "chưa biết là ai", và nghĩa đó chỉ tồn tại ở `/q`.** Ở `/staff`
  và `/admin` eyebrow **luôn có chữ** (tên bộ môn, tên khu vực, tên hội viên) — không được để
  trống rồi mượn khoảng trống nói "màn này không có gì để mô tả". `min-height` cố định vẫn giữ,
  nhưng đó là lý do **bố cục**, không phải nghĩa.
- **Hero không đổi theo bộ lọc.** Tách theo *nguyên nhân làm con số đổi*:

  | Con số đổi vì | Sống ở đâu |
  |---|---|
  | **Dữ liệu đổi** — thêm người quét, thẻ hết hạn, nhập xong danh sách | **Hero 36px** (đúng "câu trạng thái") |
  | **Người dùng vừa bấm lọc / gõ tìm** | **Dòng kết quả cạnh bộ lọc.** Không bao giờ leo lên hero |

  Ở màn có bộ lọc, hero là **hằng số của màn** (`22 HỘI VIÊN TRONG DANH SÁCH`); dòng kết quả mang
  cả nguyên nhân lẫn mẫu số: `Sắp hết hạn — 4 trong 22 hội viên`. Lý do loại cách cũ: hero 36px
  nhảy mà thứ duy nhất giải thích là eyebrow 10.5px `opacity:.62` — thành phần mờ nhất đỡ nghĩa
  cho thành phần to nhất. **Ripple:** `staff.dc.html` còn làm theo cách cũ (`2/12` → `2/5` trên
  hero khi lọc), cần một lượt sửa riêng.

**Ngoại lệ duy nhất cho hero mang tên người:** màn "đã nhận ra bạn" (thiết bị đã nhớ) ở `/q` —
ở đó việc nhận ra tên *chính là* nội dung của trạng thái.

### Thang chữ (font Archivo)

Font **Archivo** (Google Fonts, có khối `vietnamese`, trục width 75–125). Chặn thì lùi về
`system-ui` — **đừng** thay bằng Poppins/Outfit (không có dấu tiếng Việt, "ế ộ ữ" sẽ vỡ).

| Vai trò | Cỡ | Đậm | Ghi chú |
|---|---|---|---|
| **Hero (câu trạng thái)** | **36px** | 800 | VIẾT HOA, `wdth 82`, `ls -0.035em`, `lh 0.95`, tối đa 2 dòng. **Cố định, không đổi theo màn** |
| Tên tổ chức (header) | 17px | 800 | VIẾT HOA, `wdth 88`, `lh 1.05`, xuống được 2 dòng |
| Tiêu đề khối | 20px | 800 | VIẾT HOA, `wdth 88`, xuống dòng được |
| Số lớn | 33px | 800 | `ls -0.035em`. **Không được to hơn hero** |
| Thân | 15px | 400–600 | bề rộng thường (`wdth 100`) |
| Nhãn nhỏ | 10.5px | 700 | VIẾT HOA, `ls 0.13em`, opacity .62 |

**Hero cùng một cỡ ở mọi màn** — dài quá thì rút chữ, đừng thu cỡ. Tiếng Việt dài hơn tiếng Anh
~15%; nhãn/nút phải chịu chữ dài; hàng danh sách phải chứa "Nguyễn Thị Hồng Nhung" không vỡ.

---

## §4 · Kích thước và bố cục

- **Bo góc:** khối 24px · thẻ con 18px · nút và chip 999px (viên thuốc).
- **Vùng chạm tối thiểu 52px** cho **hành động chính** và **mọi thứ ở `/staff`** (tay ướt mồ hôi
  ở cửa phòng tập), kể cả điều hướng. **44px là sàn tuyệt đối cho nút phụ trên `/admin`** (link
  chữ gạch chân "Chọn file khác", "Quay lại hồ sơ") — dưới 44px là sai.
- **Chữ trong ô nhập tối thiểu 16px.** Dưới ngưỡng đó Safari iOS tự phóng to trang khi chạm ô.
- Mọi màn phải đọc được ở bề rộng **360px**.
- **Một cột 440px:** màn rộng ≥ 720px → một cột `max-width:440px` căn giữa trên nền than, **không
  bố cục lại**. Không breakpoint nào khác, không hai cột, không bảng ngang. Quyết định sản phẩm
  ([D13](../DECISIONS.md)) — áp cho cả `/admin` và `/staff`; `/q` không cần vì là trang một màn
  quét từ điện thoại.
- Bảng dữ liệu trên điện thoại: dùng **hàng thẻ**, không dùng bảng cuộn ngang.
- Nút tròn trắng 42px cho nút quay lại; pill dropdown cho bộ lọc; thanh nav nổi hình viên thuốc,
  mục đang chọn nền `--c-sky`.

---

## §5 · Header avatar — một nguồn cho cả ba bề mặt

Header = **nơi chốn = trung tâm**. Ô avatar tròn 42px trong header vì thế mang **logo/monogram
của trung tâm**, giống nhau ở cả `/q`, `/staff`, `/admin`:

- Có logo trung tâm (ảnh) → dùng logo.
- Không có logo → **monogram = chữ cái đầu tên tổ chức** (như "C" cho *Checkino* ở `/admin`),
  đặt `wdth 88`.

**Bỏ emoji 🏋️** đang dùng ở `/q` và `/staff`: nó đọc nhầm thành *bộ môn* (gym/võ thuật), trong
khi header phải nói *nơi chốn*, không phải bộ môn. Đây là chỗ giữ chỗ của bản dựng; sản phẩm
dùng logo/monogram trung tâm. *Ripple:* `q.dc.html` và `staff.dc.html` cần đổi avatar ở lượt
sửa bản dựng.

---

## §6 · Đặc tả từng màn

Ký hiệu mỗi màn: **nền màn** · eyebrow · hero · khối nội dung chính · token bão hoà (đếm ≤ 2).
Nguồn: `docs/design/{q,staff,admin}-A8.md` + ba bản dựng.

### 6.1 · `/q` — màn hội viên tự quét (11 màn, Thymeleaf server-render)

Header đứng yên: logo/monogram trung tâm + tên trung tâm + pill cơ sở. Nửa dưới nhiều màn là
**tấm sáng sage** bo `28px 28px 0 0` (phương án 1B của D12 — dễ đọc ngoài nắng, ô nhập đúng tầm
ngón cái).

| # | Trạng thái | Nền | Eyebrow / Hero | Khối chính | Token (≤2) |
|---|---|---|---|---|---|
| 1 | Nhập SĐT / đang gửi | than | trống / "BẠN LÀ / AI NHỈ?" | 2 tile: coral "18:41 · đang mở cửa" + tím "24 · người đã tới"; tấm sáng sage chứa ô nhập SĐT + nút than | coral + tím |
| 2 | Xong — bình thường | **sage** | tên hội viên + chip giờ / "ĐÃ ĐIỂM / DANH" | thẻ hội viên (mock, gradient tím) | tím (thẻ) |
| 3 | Xong — sắp hết hạn | sage | như #2 | thẻ hội viên + chip "còn 3 ngày" + ô cảnh báo icon `!` vàng "nhắc lễ tân gia hạn" | tím + (ô 3b vàng) |
| 4 | Xong — sắp hết buổi | sage | như #2 | thẻ (`sessionsLeft=2`) + ô cảnh báo vàng "sắp hết buổi" | tím + (ô 3b vàng) |
| 5 | Thẻ hết hạn | **rust** | tên / "THẺ ĐÃ / HẾT HẠN" | thẻ xám vô hiệu (`#2A2D2A`) + chip vàng `HẾT HẠN · 28/07` + nút kem | 0 (thẻ xám không tính) |
| 6 | Ngoài giờ mở cửa | than | trống / "CHƯA TỚI / GIỜ MỞ CỬA" | khối `#1F211F` "giờ mở cửa 06:00–22:00" | 0 (cố ý không cảnh báo) |
| 7 | SĐT lạ → học thử | than | trống / "BẠN MỚI / TỚI À?" | tấm sáng sage: form tên + SĐT + chip bộ môn (chọn nền sky) + checkbox | 0 (không được như lỗi) |
| 8 | Gửi học thử xong | **sage** | trống / "CẢM ƠN / BẠN NHÉ!" | khối trắng "bước tiếp theo" + nút | 0 |
| 9 | Thiết bị đã nhớ | than | "CHÀO BẠN" / **tên hội viên** (ngoại lệ hero) | 2 tile coral/tím + tấm sáng sage + nút than lớn "điểm danh" | coral + tím |
| 10 | Quét lại quá nhanh | **sage** | tên / "ĐIỂM DANH / RỒI MÀ" | thẻ hội viên tím + khối trắng "đã điểm danh lúc 18:42" | tím (cố ý không đỏ) |
| 11 | Mất mạng | than | trống / "MẤT MẠNG / RỒI" | khối `#1F211F` + nút kem "thử lại" + ô cảnh báo "nhờ nhân viên điểm danh hộ" (D9) | 0 (không phải lỗi hội viên) |

Ba màn cố tình **không** dùng màu cảnh báo: 6 (ngoài giờ), 10 (quét lại), 11 (mất mạng).

### 6.2 · `/staff` — màn nhân viên điểm danh (15 màn, PWA, một-nền-duy-nhất)

**Cả 15 màn đều nền than.** Rust ("một hội viên không vào được") và sage ("xong xuôi") đều sai
nghiệp vụ cho một *danh sách* mở suốt buổi. Header giữ nguyên như `/q`. Eyebrow ở `/staff` mang
**bộ môn + ngày** (không phải tên người — luật §3 cho phép vì `/staff` luôn có chữ). Vùng chạm
mọi thứ ≥ 52px. Hàng tên = tầng 1b (`#1F211F`, bo 18px, cao ≥ 56px); tick tròn 44px cuối hàng.

| # | Màn | Eyebrow / Hero | Đặc điểm | Token (≤2) |
|---|---|---|---|---|
| 1 | Danh sách hôm nay | "HÔM NAY · 04/08" + chip "DANH SÁCH 17:02" / `2/12 ĐÃ TỚI` | dãy chip bộ môn cuộn ngang (chip chọn nền sky) + ô tìm + danh sách hàng tên | 0 |
| 2 | Lọc theo bộ môn | "BOXING · HÔM NAY" / … | chip Boxing active (sky) | 0 (sky là 3) |
| 3 | Không có bộ môn nào | … | ẩn hẳn dãy chip (bộ môn tuỳ chọn) | 0 |
| 4 | Vừa tap một tên | … / đếm `2/12 → 3/12` | hàng thành pending (viền đứt vàng + đồng hồ) | (tick 3b vàng) |
| 5 | Tap lại để bỏ tick | … | hàng về trạng thái đã lưu | 0 |
| 6 | N lượt chờ gửi | … | dải đáy vàng "2 lượt đang chờ gửi / Gửi ngay →" | vàng (1) |
| 7 | Đang gửi | … | dải vàng + spinner "đang gửi 2 lượt…" | vàng (1) |
| 8 | Gửi xong | … | dải toast **sage** "đã lưu tất cả" (tự ẩn ~1,9s) | sage (1) |
| 9 | Mất mạng | … | dải offline `#1F211F` "mất mạng — vẫn điểm danh được" | 0 |
| 10 | Rỗng — chưa nhập ai | / "CHƯA CÓ / HỘI VIÊN NÀO" | khối hướng dẫn nhờ chủ nhập từ file | 0 |
| 11 | Rỗng — chưa ai cần điểm danh | / "HÔM NAY / CHƯA CÓ AI" | — | 0 |
| 12 | Tìm nhanh theo tên | … | lọc thật khi gõ, `norm()` bỏ dấu ("hong nhung" ra) | 0 |
| 13 | Có bản mới | … | dải cảnh báo vàng trên + dải hàng chờ vàng đáy | **vàng + vàng (2)** cùng nghĩa |
| 14 | Cài trên iPhone | / "CÀI LÊN / MÀN HÌNH CHÍNH" | khối `#1F211F` 2 bước Safari + mock thanh dưới Safari (SVG) + ô cảnh báo vàng | (ô 3b coral/vàng) |
| 15 | Bộ môn hôm nay không ai học | "KICKFIT · HÔM NAY" / `0/0` | gợi ý rỗng | 0 |

Sky `#A9E5F1` chỉ mang "đã chọn/tick": tick đã lưu, chip bộ môn đang chọn, nav đang chọn, nút
VI/EN đang chọn — không bao giờ là nút.

### 6.3 · `/admin` — màn chủ trung tâm (27 màn, PWA manifest-only)

Header avatar monogram trung tâm + tên + pill. Eyebrow **không bao giờ trống**: tên khu vực, hoặc
tên người ở màn nói về một hội viên (`member`/`phoneEdit`/`pause`/`memberHist`/`trialConvert`).
Có thanh nav nổi đáy (Home/Members/Reports/Settings, ẩn trong wizard) và thanh tiến trình 5 bước
khi ở wizard. Nền màn động ba giá trị: than (mặc định) · sage (`onLight`) · rust (`onRust`).

**Lượt 03a — nền tảng (màn 1–15):**

| # | Màn | Nền | Điểm chính | Token |
|---|---|---|---|---|
| 1 | Trang chủ | than | khối coral "lượt điểm danh" + biểu đồ cột 7 ngày (**cột hôm nay coral** — xem §2) + khối tím `count/50` + feed "đang vào · trực tiếp" + ô cảnh báo vàng "sắp hết hạn" | coral + tím |
| 2 | Wizard B1 · tên trung tâm | than | input tên + địa chỉ | 0 |
| 3 | Wizard B2 · điểm quét | than | tên điểm quét, giờ mở–đóng, xem trước QR | 0 |
| 4 | Wizard B3 · bộ môn | than | input thêm bộ môn + chip (sky, nút X); nút Bỏ qua (D7) | 0 |
| 5 | Wizard B4 · chọn nguồn | than | ô cảnh báo vàng "bước bắt buộc"; chọn file / dán Excel | (ô 3b vàng) |
| 6 | Xem trước nhập | than | khối coral "sẽ ghi vào danh sách" 3 tile; "không xoá ai cả" | coral |
| 7 | Xem trước có lỗi | than | coral + khối vàng liệt kê dòng lỗi | **coral + vàng (2)** |
| 8 | File hỏng | than | khối vàng "sửa theo 3 bước" (ô icon số) | vàng |
| 9 | Nhập xong | **sage** | chip tổng kết + "bước tiếp theo: poster QR" | 0 (nền sage = xong) |
| 10 | Wizard B5 · poster QR | than | **mock tờ A4 kem** (ngoài bốn tầng) + ô hướng dẫn "dán ở đâu" (icon box sky) | (ô 3b sky) |
| 11 | Danh sách hội viên | than | ô tìm + 2 pill lọc → **dòng kết quả cạnh lọc** (không leo hero); hàng có tag trạng thái | (tag 3) |
| 12 | Danh sách rỗng | than | "CHƯA CÓ / HỘI VIÊN NÀO" + nút nhập | 0 |
| 13 | Chi tiết hội viên | than | thẻ hội viên (mock gradient tím) + lịch sử + lưới 7 nút thao tác + ghi chú audit (D10) | tím (thẻ) |
| 14 | Sửa SĐT trùng | than | input số mới + cảnh báo vàng "số này đã có hội viên" (D8) + audit | vàng |
| 15 | Tạm dừng thẻ | **rust** | input từ ngày–đến ngày + nút "tạm dừng thẻ" | 0 (nền rust) |

**Lượt 03b — báo cáo/tiền (màn 16–27):**

| # | Màn | Điểm chính | Token |
|---|---|---|---|
| 16 | Báo cáo — trang gom (glue) | điều hướng vào tab Báo cáo | 0 |
| 17 | Xếp hạng tháng | **quán quân = icon huy chương + số hạng trên nền than** (KHÔNG khối vàng — xem §2) | 0 |
| 18 | Thẻ sắp hết hạn | ngưỡng 7/14/30 ngày; tag vàng "còn kịp" | vàng |
| 19 | Lịch sử một hội viên | eyebrow mang tên người | 0 |
| 20 | Danh sách học thử | hàng đổi trạng thái: đã liên hệ (vàng) / đã chuyển đổi (sage) / đã bỏ | vàng hoặc sage |
| 21 | Chuyển học thử → hội viên | cảnh báo vàng "số này đã có hội viên" (D8) | vàng |
| 22 | Tỉ lệ chuyển đổi | thẻ kết quả **sage** (kết quả tốt / đã xong) theo tháng | sage |
| 23 | Bất thường chờ duyệt | **vàng = cần xử lý**; duyệt/bỏ lượt đáng ngờ trước khi chốt xếp hạng (D2) | vàng |
| 24 | Nhật ký thao tác | câu tiếng Việt (audit_log, D10); lọc theo loại/người | 0 |
| 25 | Chạm trần 50 hội viên | "50 chỗ · đã dùng hết" + nút nâng gói (D6) | tím |
| 26 | Nâng gói | "Gói Pro 199k/tháng"; **VietQR + mã trung tâm, KHÔNG cổng thanh toán**; thẻ Pro tím | tím |
| 27 | Trạng thái chung | tải / rỗng / lỗi mạng; khối mất mạng dùng **coral** (không phải lỗi, cần chú ý) | coral |

---

## §7 · Song ngữ vi/en — hành vi thật của sản phẩm ([D14](../DECISIONS.md))

Toggle EN/VI trong ba bản dựng **là hành vi thật của sản phẩm**, KHÁC scaffold (§8). Luật:

- **Song ngữ Việt/Anh ở cả ba bề mặt**, **mặc định tiếng Việt** ở cả ba.
- Người dùng đổi được, lựa chọn được **nhớ lại**:
  - `/admin`, `/staff` (Angular SPA): `localStorage`.
  - `/q` (Thymeleaf server-render): **cookie ngôn ngữ** (khác cookie device token của D3), đọc
    lúc render. Không có cookie → mặc định `vi`. **Không suy từ `Accept-Language`.**
- **Chỉ hai ngôn ngữ, không khung i18n nhiều-locale** (ICU, số nhiều, RTL). Copy sống thành hai
  bộ khoá song song, như `DICT` trong bản dựng.
- Nhãn/nút phải chịu **bản dài hơn trong hai ngôn ngữ** (thường là tiếng Việt, `00 § A3`).

*Lưu ý bản dựng:* ba mặc định hiện lệch nhau (`q.dc.html`=en, `staff.dc.html`=vi, `admin.dc.html`
mới thêm) — sản phẩm phải **mặc định vi đồng nhất** cả ba. Bản EN trong bản dựng là tự dịch, không
phải nguồn dịch chính thức; chủ dự án tự dịch bản en (D14).

---

## §8 · Scaffold vs sản phẩm — đâu là dụng cụ xem bản dựng

Ba bản dựng `.dc.html` chứa cả **dụng cụ xem** lẫn **UI sản phẩm**. Phân biệt rõ để không code
nhầm scaffold thành tính năng. Scaffold **F3 bỏ hẳn**, không tồn tại trong sản phẩm:

| Vật | Loại | Ghi chú |
|---|---|---|
| Bảng nhảy trạng thái (panel phải 248px) | **Scaffold** | Lái Chromium headless / xem nhanh mọi màn. Không có trong sản phẩm. |
| Khung máy iOS 390×844 (viền, thanh giờ/pin) | **Scaffold** | Mặt bàn để xem trên desktop; iOS vẽ thật khi cài PWA. |
| Đồng hồ giả (`CLOCK`), số hội viên demo `0912345678` | **Scaffold** | Dữ liệu diễn. |
| **Toggle EN/VI** | **Sản phẩm thật** | Hành vi D14 — xem §7. |
| Thẻ hội viên (thẻ nhựa), poster A4 | **Vật mô phỏng đồ thật** | Ngoài hệ bốn tầng (§1a), nhưng **là** nội dung sản phẩm (thẻ hiển thị, poster in ra). |

---

## Phụ lục · Giá trị chính xác và component tái dùng

**Hex đã xác nhận** (khớp `00 § A2` + ba bản dựng, khớp từng ký tự):
than `#131413` · than desktop (mặt bàn) `#0B0C0B` · sắc nhạt than `#1F211F` / `#2A2D2A` ·
coral `#F4573F` (nhạt `#F98872`, fg `#2A0C06`) · rust `#8E2C1B` · tím `#6F66F0` (nhạt `#9E98F6`,
fg `#0D0838`) · vàng `#F3C24A` (nhạt `#F8DC96`, fg `#2C1F03`) · sage `#C4D4C1` (nhạt `#DCE7DA`,
fg `#14180F`) · sky `#A9E5F1` (fg `#052430`) · dim `#9AA096` · kem `#F2F2EC`.

**Thẻ hội viên (mock):** aspect-ratio **1.586** (chuẩn ISO/ID-1), gradient tím một họ
`linear-gradient(135deg, #9E98F6 0%, #6F66F0 100%)`, chip EMV vẽ SVG, số buổi lớn kiểu dập nổi.
**Cùng một component** dùng lại ở `/q` (màn 2/9/10) và `/admin` (màn 13). Biến thể hết hạn: nền
xám `#2A2D2A`, opacity .45 ở số buổi.

**Poster A4 (mock):** aspect-ratio **1/1.414**, nền kem `#F2F2EC`, tên trung tâm + "quét để điểm
danh" + QR + dòng điểm quét. Là ảnh của tờ giấy in, ngoài hệ bốn tầng.

**Ô hình 42–44px (tầng 3b):** tròn 999px cho logo/avatar/tick; bo 13px cho ô cảnh báo `!` (nền
vàng) và ô icon action. Nút tròn trắng 42px cho nút quay lại.

---

*Nguồn build: bốn báo cáo `docs/design/{q,staff,admin}-A8.md`, ba bản dựng `designs/*.dc.html`,
và prompt build-time `docs/design/prompts/` (đã đóng vai trò — [D15](../DECISIONS.md)).*
