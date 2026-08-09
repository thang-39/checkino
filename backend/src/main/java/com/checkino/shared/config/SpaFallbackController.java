package com.checkino.shared.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Returns {@code index.html} for client-side routes so deep links and F5 do not 404.
 *
 * <p>This is the "small piece of glue" that {@code PLAN.md § 2.1} refers to — the cost of not
 * using SSR.
 *
 * <p>The prefixes are listed <strong>explicitly</strong>. Do NOT use a catch-all like
 * {@code /{path:[^\.]*}}: it would swallow {@code /q/{code}}, and {@code /q} is a server-rendered
 * Thymeleaf page, not an SPA route ({@code PLAN.md § 1.2}). When adding a new SPA area, add it to
 * the list below — do not widen the pattern.
 *
 * <p>{@code "/"} must be present too: Spring's {@code WelcomePageHandlerMapping} only looks for
 * {@code index.html} at the root of {@code static/}, not inside {@code static/app/}, so without it
 * the bare domain would 404.
 */
@Controller
public class SpaFallbackController {

  @GetMapping({"/", "/admin", "/admin/**", "/staff", "/staff/**"})
  public String spa() {
    return "forward:/index.html";
  }
}
