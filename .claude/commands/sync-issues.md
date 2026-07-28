---
description: Đẩy docs/STORIES.yml lên GitHub issues (một chiều, idempotent)
allowed-tools: Bash(ruby:*), Bash(gh:*), Bash(git:*), Read
---

Render `docs/STORIES.yml` → GitHub issues.

## Chạy

```bash
ruby scripts/sync-issues.rb --dry-run   # xem sẽ làm gì
ruby scripts/sync-issues.rb             # thực thi
```

Script tự làm: validate → tạo label thiếu → tạo milestone thiếu → tạo/cập nhật issue story →
tạo/cập nhật issue epic (có checklist con) → render lại story có `needs` để link `#số` →
ghi số issue về YAML (sửa theo dòng nên **giữ nguyên comment** trong file).

Luôn chạy `--dry-run` trước và đọc output. Nếu validate fail thì sửa `docs/STORIES.yml`,
đừng sửa script.

## Sau khi chạy

```bash
git add docs/STORIES.yml && git commit -m "chore: sync số issue về backlog"
```

Số issue phải được commit — nếu không, lần sync sau sẽ tạo issue trùng.

## Kiểm

```bash
gh issue list --limit 100 | wc -l                       # phải bằng số story + số epic
ruby -ryaml -e 'st=YAML.load_file("docs/STORIES.yml")["stories"]; puts "chưa sync: #{st.select{|s| s["issue"].nil?}.map{|s| s["id"]}.inspect}"'
```

## Một chiều — đây là điểm quan trọng

`docs/STORIES.yml` là nguồn. **Sửa title hay body của issue trên GitHub sẽ bị ghi đè** ở lần
sync sau. Muốn đổi nội dung story thì sửa YAML (dùng skill `write-story`) rồi sync lại.

Thứ **không** bị ghi đè, và đó là việc của GitHub: trạng thái open/closed, comment, assignee,
PR liên kết. Trạng thái nằm ở GitHub, định nghĩa nằm ở YAML.

## Khi bị 404 / 403

- 404 trên mọi lệnh → sai account. Account work đang là mặc định:
  `gh auth switch -u thang-39`.
- Label mới chưa tạo → script tự tạo, nhưng chỉ khi label đó xuất hiện trong `labels:` của
  một story.
- Không dùng GitHub Projects: token thiếu scope `read:project`. Milestone + label là đủ.
