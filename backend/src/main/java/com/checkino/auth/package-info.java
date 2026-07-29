/**
 * Hai nhu cầu auth tách rời: magic link (chủ/nhân viên) và device token (hội viên).
 *
 * <p>Chủ/nhân viên dùng email magic link; hội viên dùng device token trong cookie httpOnly
 * TTL 1 năm. Email cho hội viên là sai thị trường (D3). v1 KHÔNG gửi OTP cho hội viên (D2).
 */
package com.checkino.auth;
