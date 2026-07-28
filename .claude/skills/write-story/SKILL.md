---
name: write-story
description: Use when adding, splitting, or rewriting stories in the Checkino backlog (docs/STORIES.yml) — turning knowledge from DECISIONS.md / PRD.md / PLAN.md into executable stories with acceptance criteria. Triggers on "thêm story", "viết story", "chia story", "tách story", "backlog", or when a new feature/decision needs to become work.
---

# Viết story cho Checkino

## Nguyên tắc số một: TRỎ, không SAO LẠI

`docs/STORIES.yml` không phải chỗ để tóm tắt lại tài liệu. Nếu một story chép lại nội dung
D7, thì D7 có hai bản, và khi D7 đổi thì không có gì bắt bản thứ hai đổi theo.

Story chỉ được chứa hai loại nội dung **chưa tồn tại ở đâu cả**:

1. **`ac`** — acceptance criteria: làm thế nào biết là xong.
2. **`needs`** — thứ tự phụ thuộc.

Mọi thứ khác (tại sao, spec hành vi, SQL mẫu, bảng giá) thì để trong `refs`.

Ngoại lệ duy nhất được phép nhắc lại nội dung docs: **cái bẫy**. Ví dụ "TUYỆT ĐỐI không kiểm
bằng `if (!exists)`", "KHÔNG có mã 6 số fallback". Nhắc lại một câu cấm ngắn để người thi hành
không phải mở docs mới biết mình đang sắp làm sai — đó là cảnh báo, không phải bản sao spec.

## Phân tầng — đọc trước khi viết

| Tầng | File | Thắng khi lệch |
|---|---|---|
| Tại sao | `DECISIONS.md` (D1–D11 + Ba cơ chế) | **Luôn thắng** |
| Sản phẩm làm gì | `PRD.md` (F1–F11, §6 NFR, §7 tier, §8 metric) | Hành vi sản phẩm |
| Stack + milestone | `PLAN.md` (§2 stack, §3 schema, §4 M0–M4, §6 risk, §7 DoD) | Ý định milestone |
| **Việc phải làm** | **`docs/STORIES.yml`** | **Chia việc** |
| Trạng thái | GitHub issues | Không phải nguồn |

## Schema

```yaml
- id: M2-S07              # <epic>-S<2 chữ số>. Không tái dùng id đã xoá.
  title: "..."            # Tiếng Việt, mở đầu bằng mã feature nếu có: "F5 — ..."
  epic: M2                # phải khớp một id trong `epics:`
  refs: ["DECISIONS.md § D7", "PRD.md § 4 F5"]
  labels: [backend, frontend]
  needs: [M2-S05]         # id story phải xong trước
  estimate: 0.5d          # 0.5d | 1d | 1.5d | 2d — không quá 2d
  issue: null             # /sync-issues điền, đừng sửa tay
  ac:
    - "..."
```

### Cách viết `refs`

- Định dạng: `"<file> § <tên mục>"`. **Trỏ bằng TÊN MỤC, không bằng số dòng** — số dòng rữa
  ngay lần sửa docs kế tiếp.
- Tên mục phải khớp một heading thật. Kiểm bằng
  `rg --color=never --no-heading -n '^#{2,3} ' DECISIONS.md PRD.md PLAN.md`.
- Feature không có heading riêng (F1–F11 nằm chung trong `PRD.md § 4`) thì viết
  `"PRD.md § 4 F5"` — đủ để `rg 'F5\.'` tìm ra.
- 2–4 ref là đủ. Nhiều hơn nghĩa là story quá rộng, tách ra.

### Cách viết `ac` — đây là phần khó, và là phần duy nhất có giá trị mới

Một AC tốt trả lời được: *"chạy cái gì / xem cái gì thì biết là đúng?"*

| Tệ | Tốt |
|---|---|
| "Import hoạt động đúng" | "Import lại chính file cũ → 0 mới, 0 cập nhật, N không đổi" |
| "Đảm bảo cô lập đa tenant" | "Query khi chưa set app.org_id trả về rỗng, không phải trả toàn bộ" |
| "Hiệu năng tốt" | "p95 round-trip check-in < 2s trên 4G, đo thật một lần" |
| "Xử lý lỗi" | "SĐT không parse được → liệt kê ra và bỏ qua, không im lặng bỏ" |

Quy tắc:
- Mỗi AC là **một** điều kiện kiểm được. Có chữ "và" thì tách thành hai.
- Ưu tiên câu có **số** (ngưỡng, số lượng, thời gian) hoặc **tên test**.
- 3–8 AC mỗi story. Ít hơn 3 → chưa nghĩ đủ. Nhiều hơn 8 → là hai story.
- Có AC dạng **cấm** khi story dễ làm sai: viết chữ HOA cho phần cấm.
- Story không-code (spike, mua domain, pilot) vẫn phải có AC kiểm được — "đã ghi kết luận
  vào DECISIONS.md" là một AC hợp lệ, "đã nghiên cứu" thì không.

### `needs` — chỉ phụ thuộc THẬT

Chỉ ghi khi story A **không thể bắt đầu** nếu chưa có B. Không ghi vì "làm sau thì hợp lý hơn".
Phụ thuộc giả biến backlog thành một chuỗi thẳng và `/next` sẽ chỉ ra được một việc mỗi lúc.

Không được có vòng lặp. Kiểm bằng script ở cuối file này.

### `estimate`

Chỉ 4 mức: `0.5d` `1d` `1.5d` `2d`. Quá 2 ngày part-time thì **tách**, không nâng số.
Đây là ngày-người part-time, không phải ngày lịch.

### `labels`

Chọn từ tập đang dùng: `backend` `frontend` `db` `infra` `test` `auth` `non-code` `spike`
`legal` `pro` `blocked` `mechanism-1` `mechanism-2` `mechanism-3`.
Thêm nhãn mới thì phải tạo trên GitHub luôn (`gh label create`), không thì `/sync-issues` fail.

## Quy trình thêm story

1. **Đọc `docs/STORIES.yml` trước.** Dò trùng: story mới có nằm trong `ac` của story nào rồi
   chưa? Nếu có thì sửa story cũ, đừng thêm cái mới.
2. Xác định epic. Không có epic phù hợp → đừng tự tạo epic mới, hỏi người dùng.
3. Đọc các `refs` mình định trỏ tới. **Đọc thật**, không trỏ theo trí nhớ.
4. Viết story theo schema, chèn vào đúng nhóm epic, giữ thứ tự id tăng dần.
5. Chạy script validate ở dưới.
6. Chạy `/sync-issues` để đẩy lên GitHub.

## Quy tắc: quyết định thiếu thì GHI NGƯỢC vào DECISIONS.md

Trong lúc viết story mà phát hiện tài liệu chưa quyết một điều gì (định dạng, timezone, hành vi
biên), **đừng tự quyết trong `ac`**. Làm thế là chôn một quyết định kiến trúc vào backlog.

Thay vào đó thêm một AC dạng:

```yaml
- "QUYẾT ĐỊNH CẦN CHỐT: <câu hỏi> — chốt trong story này và ghi vào DECISIONS.md"
```

và thêm dòng đó vào `note_gaps` ở cuối `STORIES.yml`.

Khi làm story đó xong thì DECISIONS.md phải có mục mới. Đây là cách docs lớn lên đúng chỗ.

## Khi PLAN/PRD lệch nhau

Story là chỗ lệch bị lộ ra. Xử lý theo thứ tự:

1. `DECISIONS.md` có nói không → nó thắng, xong.
2. Không → viết story theo cách an toàn hơn, và ghi lý do vào `ac` bằng một câu bắt đầu bằng
   `"LÝ DO ..."` hoặc `"LÀM RÕ: ..."` để sau này đọc lại biết đây là chỗ đã cân nhắc.
   Ví dụ `M2-S14` giải thích tại sao consent PDPL bị kéo từ M4 về M2.
3. Lệch lớn tới mức đổi scope milestone → dừng, hỏi người dùng, đừng tự quyết.

## Validate

Không có `yq` trên máy này. Dùng ruby hệ thống (`/usr/bin/ruby`):

```bash
ruby -ryaml -e '
d=YAML.load_file("docs/STORIES.yml"); st=d["stories"]; ids=st.map{|s| s["id"]}
c={}; ids.each{|i| c[i]=(c[i]||0)+1}
puts "stories=#{st.size} dup=#{c.select{|k,v| v>1}.keys.inspect}"
puts "dangling=#{st.flat_map{|s| (s["needs"]||[]).reject{|n| ids.include?(n)}.map{|n| "#{s["id"]}->#{n}"}}.inspect}"
eids=d["epics"].map{|e| e["id"]}
puts "bad_epic=#{st.reject{|s| eids.include?(s["epic"])}.map{|s| s["id"]}.inspect}"
puts "thin=#{st.select{|s| (s["ac"]||[]).size<3 || (s["refs"]||[]).empty?}.map{|s| s["id"]}.inspect}"
puts "too_big=#{st.select{|s| s["estimate"].to_s.sub("d","").to_f>2}.map{|s| s["id"]}.inspect}"
seen={}; r=lambda{|i,st2| next if seen[i]; raise "CYCLE #{(st2+[i]).join(">")}" if st2.include?(i); (st.find{|x| x["id"]==i}["needs"]||[]).each{|n| r.call(n,st2+[i])}; seen[i]=true}
ids.each{|i| r.call(i,[])}; puts "cycles=none"
'
```

Tất cả phải rỗng/none trước khi commit.
