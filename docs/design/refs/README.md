# Ảnh tham chiếu thiết kế

Ảnh gốc dùng làm mốc cho hai bản trong `docs/design/`. Giữ lại để sau này còn đối chiếu —
không có chúng thì ba tháng nữa không ai nhớ bản thiết kế đang bắt chước cái gì, và mọi
tranh luận "sao chỗ này lại thế" đều thành cãi theo cảm giác.

| File | Thuộc bản | Nguồn | Trạng thái |
|---|---|---|---|
| `ban-1-ref-1.jpg` | Bản 1 · Pastel thân thiện | [Self-care Mobile App Design Concept](https://dribbble.com/shots/23278774-Self-care-Mobile-App-Design-Concept) — lưới màn hình | ❌ thiếu |
| `ban-1-ref-2.jpg` | Bản 1 | cùng shot — ba màn chính phóng to | ❌ thiếu |
| `ban-1-ref-3.jpg` | Bản 1 | cùng shot — bảng font Zona Pro + 4 mã màu | ❌ thiếu |
| `ban-2-ref-1.jpg` | Bản 2 · Bảng điều khiển | Real-Time Operational Intelligence Dashboard — lưới màn hình | ✅ |
| `ban-2-ref-2.jpg` | Bản 2 | cùng shot — trên nền đen, có WORKERS và POINTS | ✅ |
| `ban-2-ref-3.jpg` | Bản 2 | cùng shot — bản desktop | ✅ |

Ba ảnh của Bản 1 **thiếu** vì cache ảnh của Claude Code chỉ giữ ảnh của tin nhắn gần nhất
(`~/.claude/image-cache/<session>/`), các tin trước bị ghi đè. Dán lại ba ảnh đó vào phiên chat
là copy được ngay, hoặc tự lưu tay đúng tên trên.

Hai trang `ban-1-*.html` / `ban-2-*.html` tự dò: ảnh nào chưa có thì hiện ô gạch đứt kèm tên file
cần lưu, không hiện icon ảnh vỡ.

## Ảnh đã mất hẳn, không cần tìm lại

Trong quá trình chọn hướng còn hai ảnh nữa đã bị ghi đè và **không** ảnh hưởng tới hai bản
đang có — chỉ ghi ra đây cho đủ dấu vết:

- Lưới 4 shot Dribbble (Ronas IT, Nixtio) — mốc đầu tiên, đã bị hai vòng sau thay thế.
- Shot **xefag** (chai thực phẩm chức năng, nền đỏ mận / xanh khói / vàng) — nguồn của các bản
  v3 và v4 đã loại, không còn nằm trong hai bản cuối.

## Giấy phép

Ảnh của người khác, dùng làm **tham chiếu nội bộ**. Không đưa vào sản phẩm, không phát hành lại,
không dùng trong trang marketing. Không có tài sản hình ảnh nào của họ được copy vào code —
mặt vẽ ở Bản 1 là SVG tự vẽ, Bản 2 không dùng minh hoạ.

Kích thước đã hạ xuống tối đa 1600px, JPEG chất lượng 82 (4,7MB → 936KB) để repo không phình.
