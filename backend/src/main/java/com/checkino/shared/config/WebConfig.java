package com.checkino.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the Angular bundle from {@code classpath:/static/app/} but at the root URL.
 *
 * <p>Two constraints pull against each other: D5 pins the bundle at {@code static/app/}
 * <em>on disk</em>, while Angular builds with {@code <base href="/">} so the browser requests
 * assets at {@code /main-*.js}. Mapping {@code /app/**} to the on-disk path would 404 the assets;
 * switching to {@code <base href="/app/">} would make the router emit {@code /app/admin/members}
 * and break deep links. The fix: keep the on-disk path per D5, map it to the root URL here.
 *
 * <p>Mapping {@code /**} does not shadow controllers: {@code RequestMappingHandlerMapping} has
 * order 0 while the resource handler sits near {@code Integer.MAX_VALUE} — so {@code /q/**}
 * (Thymeleaf) and {@code /api/**} still win.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/app/");
  }
}
