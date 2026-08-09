package com.checkino.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Deep links into client routes must not 404 — the M1-S01 AC, and the reason the glue exists.
 */
@WebMvcTest(SpaFallbackController.class)
class SpaFallbackControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void deepLinkIntoAdminForwardsToIndexHtml() throws Exception {
    mockMvc
        .perform(get("/admin/members"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }

  /** Typing the bare domain must reach the app, not a 404 — see the controller javadoc. */
  @Test
  void rootForwardsToIndexHtml() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
  }

  @Test
  void deepLinkIntoStaffForwardsToIndexHtml() throws Exception {
    mockMvc
        .perform(get("/staff/today"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }

  /**
   * /q/{code} must NOT be caught by the fallback — it is a separate Thymeleaf page
   * (PLAN.md § 1.2). This story has no controller for /q yet, so it must fall outside the SPA
   * fallback rather than returning index.html.
   */
  @Test
  void fallbackDoesNotSwallowQRoute() throws Exception {
    mockMvc.perform(get("/q/ABC123")).andExpect(status().isNotFound());
  }
}
