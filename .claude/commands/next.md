---
description: Nên làm story nào tiếp theo, kèm lý do
allowed-tools: Bash(ruby:*), Bash(gh issue list:*), Bash(gh issue view:*), Read
---

Trả lời câu "giờ làm gì tiếp".

## Bước 1 — tính danh sách sẵn sàng

```bash
ruby -ryaml -e '
d=YAML.load_file("docs/STORIES.yml"); st=d["stories"]; by=st.map{|s| [s["id"],s]}.to_h
open=`gh issue list --state open --limit 200 --json number`.scan(/\d+/).map(&:to_i)
closed=lambda{|s| s["issue"] && !open.include?(s["issue"])}
ready=st.reject{|s| closed.call(s)}.select{|s| (s["needs"]||[]).all?{|n| closed.call(by[n]) }}
ready.sort_by{|s| s["id"]}.each{|s| puts "#{s["id"]}\t##{s["issue"]}\t#{s["estimate"]}\t#{s["epic"]}\t#{s["title"]}"}
puts "---"
blocked=st.reject{|s| closed.call(s)} - ready
puts "bị chặn: #{blocked.size} story"
'
```

Story "xong" = issue của nó đã closed. Story chưa sync (`issue: null`) coi như chưa xong.

## Bước 2 — chọn một

Ưu tiên theo thứ tự:

1. **Epic thấp nhất trước** — M0 xong hết mới sang M1. Ngoại lệ: story M0 không-code
   (mua domain, chốt design partner) không chặn `M1-S01`, cứ scaffold song song được.
2. **Trong cùng epic, story mở đường cho nhiều story khác nhất** — đếm số story có `needs`
   trỏ tới nó.
3. **`mechanism-2` đi trước mọi feature.** DECISIONS.md nói rõ: bộ test cô lập đa tenant làm
   trước, không làm sau.
4. Cùng hạng thì chọn `estimate` nhỏ hơn.

## Bước 3 — trả lời

Ngắn. Đúng dạng này, không dài dòng:

```
→ Làm #12  [M1-S02] Migration V1: schema lõi + audit_log   (1.5d)

Vì: M1-S01 đã xong nên đây là story duy nhất trong M1 sẵn sàng, và 4 story khác
    (M1-S03, M2-S03, ...) đang chờ nó.

Đọc trước: PLAN.md § 3.1, DECISIONS.md § D7/D8/D10
Bắt đầu:   /plan 12
```

Nếu có 2–3 lựa chọn hợp lý ngang nhau thì nêu cả, xếp hạng, và nói rõ khác nhau ở đâu —
đừng bắt người dùng tự cân.

Nếu `ready` rỗng: nói story nào đang chặn tất cả, và nó cần gì.

Nếu chưa sync issue lần nào (`issue: null` khắp nơi): nói chạy `/sync-issues` trước.
