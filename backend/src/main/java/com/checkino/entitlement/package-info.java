/**
 * Gói thẻ / thẻ hội viên và chính sách trừ buổi.
 *
 * <p>Trừ buổi phải nằm cùng transaction với insert check-in (cơ chế 1). Đây là lý do cứng
 * nhất khiến microservices bị loại (D5).
 */
package com.checkino.entitlement;
