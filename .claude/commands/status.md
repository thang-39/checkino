---
description: Tiến độ dự án theo epic, story đang mở, exit criteria còn thiếu
allowed-tools: Bash(ruby:*), Bash(gh issue list:*), Bash(git:*), Read
---

Bảng tình hình dự án.

## Bước 1 — lấy số

```bash
ruby -ryaml -e '
require "json"
d=YAML.load_file("docs/STORIES.yml"); st=d["stories"]; by=st.map{|s| [s["id"],s]}.to_h
all=JSON.parse(`gh issue list --state all --limit 300 --json number,state,title,assignees`)
state=all.map{|i| [i["number"], i["state"]]}.to_h
done=lambda{|s| s["issue"] && state[s["issue"]]=="CLOSED"}
puts "EPIC\tdone/total\tngày còn\texit"
d["epics"].each do |e|
  k=st.select{|s| s["epic"]==e["id"]}
  dn=k.count{|s| done.call(s)}
  left=k.reject{|s| done.call(s)}.sum{|s| s["estimate"].to_s.sub("d","").to_f}
  puts "#{e["id"]}\t#{dn}/#{k.size}\t#{left.round(1)}d\t#{e["exit"].to_s.lines.first.strip[0,60]}"
end
puts "---UNSYNCED---"; puts st.select{|s| s["issue"].nil?}.map{|s| s["id"]}.join(" ")
puts "---READY---"
st.reject{|s| done.call(s)}.select{|s| (s["needs"]||[]).all?{|n| done.call(by[n])}}.each{|s| puts "#{s["id"]}\t##{s["issue"]}\t#{s["estimate"]}\t#{s["title"]}"}
puts "---GAPS---"
st.reject{|s| done.call(s)}.each{|s| (s["ac"]||[]).each{|a| puts "#{s["id"]}: #{a}" if a.include?("QUYẾT ĐỊNH CẦN CHỐT")}}
'
git log --oneline -5
```

## Bước 2 — trình bày

Một bảng, rồi ba dòng dưới. Đừng viết thành bài.

```
        done   còn lại   exit criteria
M0      2/4    1.0d      design partner + GPS + domain + email
M1      0/16   17.0d     demo ở cửa thật; re-import không mất ai
M2      0/14   16.0d     workflow gig chạy đủ trừ báo cáo tháng
M3      0/11   7.5d      airplane mode + reload vẫn chạy
M4      0/13   18.5d     có khách trả tiền đầu tiên

Sẵn sàng làm ngay:  #5 M0-S04 (0.5d) · #7 M1-S01 (1.5d)
Quyết định treo:    5 chỗ (M1-S09 CSV/paste, M2-S10 timezone, ...)
Còn tới hết M3:     43d part-time ≈ 17–21 tuần ở 15–20h/tuần
```

## Lưu ý khi báo tiến độ

- Con số "còn lại" là **ngày-người part-time**, không phải ngày lịch. Ở 15–20h/tuần thì một
  tuần ≈ 2–2.5 ngày-người. Luôn quy đổi ra tuần khi báo, đừng để người đọc tự nhân.
- `PLAN.md § 4` nói ~8 tuần tới hết M3. Backlog tính ra 17–21 tuần. **Đừng lặp lại con số
  8 tuần như thể nó đúng** — nếu người dùng hỏi, nói rõ chênh lệch và hai lựa chọn: nhận mốc
  dài hơn, hoặc dùng danh sách cắt ở `PLAN.md § 6` (① Sheet mirror ② GPS layer — cắt cả hai
  chỉ giảm ~3 ngày).
- Có story `issue: null` → nhắc chạy `/sync-issues`.
- Epic xong hết story nhưng **exit criteria chưa xác minh** thì chưa xong. M1/M3 có story
  xác minh riêng (`M1-S16`, `M3-S11`) — chính là chỗ đó.
