package com.checkino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A single process for /q, /staff, /admin and the API.
 *
 * <p>Modular monolith: code is split by business domain (see the sub-packages), but runs in one
 * JVM and talks to one database. All three mechanisms in {@code DECISIONS.md} rely on "one
 * database, one transaction" — do not split into separate services (D5).
 */
@SpringBootApplication
public class CheckinoApplication {

  public static void main(String[] args) {
    SpringApplication.run(CheckinoApplication.class, args);
  }
}
