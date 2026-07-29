package com.checkino.shared.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trả {@code index.html} cho route phía client, để deep link và F5 không 404.
 *
 * <p>Đây là "một miếng keo nhỏ" mà {@code PLAN.md § 2.1} nói tới — cái giá của việc không dùng
 * SSR.
 *
 * <p>Các prefix được liệt kê <strong>tường minh</strong>. KHÔNG dùng catch-all kiểu
 * {@code /{path:[^\.]*}}: nó sẽ nuốt luôn {@code /q/{code}}, mà {@code /q} là trang Thymeleaf
 * server-render chứ không phải route SPA ({@code PLAN.md § 1.2}). Thêm khu vực SPA mới thì
 * thêm vào danh sách dưới đây, đừng nới rộng pattern.
 *
 * <p>{@code "/"} cũng phải có mặt: {@code WelcomePageHandlerMapping} của Spring chỉ tìm
 * {@code index.html} ở gốc {@code static/}, không tìm trong {@code static/app/}, nên nếu thiếu
 * thì gõ domain trần sẽ ra 404.
 */
@Controller
public class SpaFallbackController {

  @GetMapping({"/", "/admin", "/admin/**", "/staff", "/staff/**"})
  public String spa() {
    return "forward:/index.html";
  }
}
