package com.checkino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Một tiến trình duy nhất cho cả /q, /staff, /admin và API.
 *
 * <p>Modular monolith: code chia theo miền nghiệp vụ (xem các package con), nhưng chạy trong
 * một JVM và nói chuyện với một database. Cả ba cơ chế của {@code DECISIONS.md} đều dựa vào
 * "một database, một transaction" — đừng tách thành service riêng (D5).
 */
@SpringBootApplication
public class CheckinoApplication {

  public static void main(String[] args) {
    SpringApplication.run(CheckinoApplication.class, args);
  }
}
