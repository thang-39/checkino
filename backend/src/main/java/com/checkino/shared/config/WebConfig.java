package com.checkino.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serve bundle Angular từ {@code classpath:/static/app/} nhưng ở URL gốc.
 *
 * <p>Hai ràng buộc kéo ngược nhau: D5 chốt bundle nằm ở {@code static/app/} <em>trên đĩa</em>,
 * còn Angular build ra {@code <base href="/">} nên trình duyệt xin asset ở {@code /main-*.js}.
 * Nếu map {@code /app/**} đúng theo đường dẫn đĩa thì asset 404; nếu đổi thành
 * {@code <base href="/app/">} thì router đẻ ra {@code /app/admin/members} và phá luôn deep
 * link. Cách giải: giữ đường dẫn đĩa theo D5, map về URL gốc ở đây.
 *
 * <p>Map {@code /**} không che controller: {@code RequestMappingHandlerMapping} có order 0,
 * còn resource handler nằm gần {@code Integer.MAX_VALUE} — nên {@code /q/**} (Thymeleaf) và
 * {@code /api/**} vẫn thắng.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/app/");
  }
}
