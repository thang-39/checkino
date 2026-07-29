package com.checkino.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Deep link vào route client không được 404 — AC của M1-S01, và là lý do miếng keo tồn tại.
 */
@WebMvcTest(SpaFallbackController.class)
class SpaFallbackControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void deepLinkVaoAdminForwardVeIndexHtml() throws Exception {
    mockMvc
        .perform(get("/admin/members"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }

  /** Gõ domain trần phải ra app, không ra 404 — xem javadoc của controller. */
  @Test
  void gocForwardVeIndexHtml() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
  }

  @Test
  void deepLinkVaoStaffForwardVeIndexHtml() throws Exception {
    mockMvc
        .perform(get("/staff/today"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }

  /**
   * /q/{code} KHÔNG được fallback dính vào — nó là trang Thymeleaf riêng (PLAN.md § 1.2). Ở
   * story này chưa có controller cho /q, nên nó phải rơi ra ngoài SPA fallback chứ không được
   * trả index.html.
   */
  @Test
  void fallbackKhongNuotRouteCuaQ() throws Exception {
    mockMvc.perform(get("/q/ABC123")).andExpect(status().isNotFound());
  }
}
