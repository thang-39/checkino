package com.checkino.shared.tenant;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Pins the {@code @Transactional} advisor to order 100 so {@link OrgRlsAspect} (order 200) runs
 * INSIDE the transaction — see the ordering trap documented there. Cơ chế 2, lớp 1.
 *
 * <p>{@code proxyTargetClass = true} matches Spring Boot's default ({@code
 * spring.aop.proxy-target-class=true}), so declaring our own {@code @EnableTransactionManagement}
 * (which makes Boot back off its auto-configured one) does not change proxying behaviour.
 */
@Configuration
@EnableTransactionManagement(order = 100, proxyTargetClass = true)
public class TenantConfig {}
