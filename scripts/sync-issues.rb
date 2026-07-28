#!/usr/bin/env ruby
# Render docs/STORIES.yml → GitHub issues. Một chiều, idempotent.
#
#   ruby scripts/sync-issues.rb --dry-run   # in ra sẽ làm gì, không gọi API
#   ruby scripts/sync-issues.rb             # thực thi
#
# Idempotent bằng cách ghi số issue trở lại field `issue:` trong STORIES.yml.
# Ghi lại bằng phép sửa theo dòng (không dùng YAML.dump) để GIỮ NGUYÊN comment trong file.
#
# STORIES.yml là nguồn. Sửa body issue trên GitHub sẽ bị ghi đè ở lần sync sau — cố ý.

require 'yaml'
require 'shellwords'
require 'json'

DRY   = ARGV.include?('--dry-run')
FILE  = File.join(__dir__, '..', 'docs', 'STORIES.yml')
doc   = YAML.load_file(FILE)
REPO  = doc.dig('meta', 'repo') or abort 'meta.repo thiếu trong STORIES.yml'

EXTRA_LABELS = %w[epic story]

def sh(cmd)
  puts "  $ #{cmd}" if DRY
  return '' if DRY
  out = `#{cmd} 2>&1`
  abort "LỖI: #{cmd}\n#{out}" unless $?.success?
  out.strip
end

def gh_json(path)
  # Quote bắt buộc: `&` trong query string mà không quote thì shell đem đi background.
  out = `gh api #{Shellwords.escape(path)} 2>/dev/null`
  return nil unless $?.success?
  v = JSON.parse(out) rescue nil
  v.is_a?(Array) ? v : nil
end

epics   = doc['epics']
stories = doc['stories']
by_id   = stories.map { |s| [s['id'], s] }.to_h

# ── validate trước khi chạm vào GitHub ───────────────────────────────
errs = []
ids = stories.map { |s| s['id'] }
ids.group_by(&:itself).select { |_, v| v.size > 1 }.each_key { |d| errs << "id trùng: #{d}" }
stories.each do |s|
  (s['needs'] || []).each { |n| errs << "#{s['id']} needs #{n} không tồn tại" unless by_id[n] }
  errs << "#{s['id']} thiếu ac" if (s['ac'] || []).empty?
  errs << "#{s['id']} thiếu refs" if (s['refs'] || []).empty?
  errs << "#{s['id']} epic #{s['epic']} không tồn tại" unless epics.any? { |e| e['id'] == s['epic'] }
end
seen = {}
walk = lambda do |id, stack|
  return if seen[id]
  errs << "vòng lặp: #{(stack + [id]).join(' > ')}" and return if stack.include?(id)
  (by_id[id]['needs'] || []).each { |n| walk.call(n, stack + [id]) if by_id[n] }
  seen[id] = true
end
ids.each { |i| walk.call(i, []) }
abort "Validate fail:\n - #{errs.join("\n - ")}" if errs.any?
puts "✓ validate: #{epics.size} epic, #{stories.size} story, không lỗi"

# ── labels ───────────────────────────────────────────────────────────
all_labels = (stories.flat_map { |s| s['labels'] || [] } + EXTRA_LABELS).uniq.sort
existing = (gh_json("repos/#{REPO}/labels?per_page=100") || []).map { |l| l['name'] }
(all_labels - existing).each do |l|
  puts "+ label #{l}"
  sh "gh label create #{Shellwords.escape(l)} --repo #{REPO} --force"
end

# ── milestones (một milestone cho mỗi epic) ──────────────────────────
ms = (gh_json("repos/#{REPO}/milestones?state=all&per_page=100") || [])
      .map { |m| [m['title'], m['number']] }.to_h
epics.each do |e|
  next if ms[e['id']]
  puts "+ milestone #{e['id']}"
  sh "gh api repos/#{REPO}/milestones -f title=#{Shellwords.escape(e['id'])} " \
     "-f description=#{Shellwords.escape(e['title'])} --silent"
end

# ── đối chiếu với issue đã có trên GitHub ────────────────────────────
# Nguồn idempotency chính là field `issue:` trong YAML. Nhưng nếu lần chạy trước lỗi giữa
# đường (issue đã tạo, YAML chưa kịp ghi) thì phải nhận lại được, không thì tạo trùng.
# Nhận lại bằng tiền tố `[<id>]` trong title.
existing_issues = {}
page = 1
loop do
  batch = gh_json("repos/#{REPO}/issues?state=all&per_page=100&page=#{page}") or break
  break if batch.empty?
  batch.each do |i|
    next if i['pull_request']
    if (m = i['title'].match(/\A\[(?:EPIC )?([A-Z0-9-]+)\]/))
      existing_issues[m[1]] = i['number']
    end
  end
  page += 1
end
recovered = 0
(epics + stories).each do |x|
  n = existing_issues[x['id']]
  if n && x['issue'] != n
    x['issue'] = n
    recovered += 1
  end
end
puts "✓ nhận lại #{recovered} issue đã tồn tại trên GitHub theo title" if recovered > 0

# ── render body ──────────────────────────────────────────────────────
HEADER = lambda do |id|
  "<!-- Sinh ra từ docs/STORIES.yml (#{id}). Sửa ở YAML rồi chạy /sync-issues — " \
  "sửa tay ở đây sẽ bị ghi đè. -->"
end

def story_body(s, by_id)
  out = [HEADER.call(s['id']), '']
  out << "**Epic** `#{s['epic']}` · **Ước lượng** #{s['estimate']}" \
         "#{(s['labels'] || []).empty? ? '' : " · #{s['labels'].map { |l| "`#{l}`" }.join(' ')}"}"
  out << ''
  out << '### Acceptance criteria'
  s['ac'].each { |a| out << "- [ ] #{a}" }
  out << ''
  out << '### Tra cứu — nguồn sự thật, đọc ở đó, đừng copy về đây'
  s['refs'].each { |r| out << "- #{r}" }
  needs = s['needs'] || []
  unless needs.empty?
    out << ''
    out << '### Cần xong trước'
    needs.each do |n|
      d = by_id[n]
      num = d && d['issue']
      out << (num ? "- [ ] ##{num} — `#{n}` #{d['title']}" : "- [ ] `#{n}` #{d ? d['title'] : '?'}")
    end
  end
  out << ''
  out << "_Ước lượng #{s['estimate']} là ngày-người part-time, không phải ngày lịch._"
  out.join("\n")
end

def epic_body(e, stories)
  kids = stories.select { |s| s['epic'] == e['id'] }
  eff  = kids.sum { |s| s['estimate'].to_s.sub('d', '').to_f }
  out = [HEADER.call(e['id']), '']
  out << "**#{kids.size} story · ~#{eff.round(1)} ngày-người part-time**"
  out << ''
  out << '### Exit criteria'
  e['exit'].to_s.strip.each_line { |l| out << "- #{l.strip.sub(/\A\d+\.\s*/, '')}" }
  out << ''
  out << '### Story'
  kids.each do |s|
    num = s['issue']
    out << (num ? "- [ ] ##{num} — `#{s['id']}` #{s['title']}" : "- [ ] `#{s['id']}` #{s['title']}")
  end
  out << ''
  out << '### Tra cứu'
  (e['refs'] || []).each { |r| out << "- #{r}" }
  out.join("\n")
end

def upsert(repo, item, title, body, labels, milestone)
  require 'tempfile'
  f = Tempfile.new('body'); f.write(body); f.flush
  if item['issue']
    # `gh issue edit` dùng --add-label, KHÔNG phải --label (khác `gh issue create`).
    lbl = labels.map { |l| "--add-label #{Shellwords.escape(l)}" }.join(' ')
    puts "~ ##{item['issue']} #{title}"
    sh "gh issue edit #{item['issue']} --repo #{repo} --title #{Shellwords.escape(title)} " \
       "--body-file #{f.path} #{lbl} --milestone #{Shellwords.escape(milestone)}"
    item['issue']
  else
    lbl = labels.map { |l| "--label #{Shellwords.escape(l)}" }.join(' ')
    puts "+ #{title}"
    return nil if DRY
    url = sh "gh issue create --repo #{repo} --title #{Shellwords.escape(title)} " \
             "--body-file #{f.path} #{lbl} --milestone #{Shellwords.escape(milestone)}"
    url.split('/').last.to_i
  end
ensure
  f&.close!
end

# ── pass 1: tạo/cập nhật story issue ────────────────────────────────
stories.each do |s|
  n = upsert(REPO, s, "[#{s['id']}] #{s['title']}", story_body(s, by_id),
             (s['labels'] || []) + ['story'], s['epic'])
  s['issue'] = n if n
end

# ── pass 2: epic issue (đã biết số issue con) ───────────────────────
epics.each do |e|
  n = upsert(REPO, e, "[EPIC #{e['id']}] #{e['title']}", epic_body(e, stories), ['epic'], e['id'])
  e['issue'] = n if n
end

# ── pass 3: render lại body story để link ## trong "Cần xong trước" ─
stories.select { |s| !(s['needs'] || []).empty? }.each do |s|
  upsert(REPO, s, "[#{s['id']}] #{s['title']}", story_body(s, by_id),
         (s['labels'] || []) + ['story'], s['epic'])
end

# ── ghi số issue về YAML, giữ nguyên comment ─────────────────────────
if DRY
  puts "\n(dry-run: không ghi file, không gọi API)"
  exit
end

lines = File.readlines(FILE)
nums  = (epics + stories).map { |x| [x['id'], x['issue']] }.to_h
cur = nil
lines.each_with_index do |l, i|
  if (m = l.match(/^  - id: (\S+)\s*$/)) then cur = m[1]
  elsif l =~ /^    issue: / && cur && nums[cur]
    lines[i] = "    issue: #{nums[cur]}\n"
    cur = nil
  end
end
File.write(FILE, lines.join)
puts "\n✓ đã ghi số issue về #{FILE}"
puts "✓ #{stories.size} story + #{epics.size} epic trên https://github.com/#{REPO}/issues"
