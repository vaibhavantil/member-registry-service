package com.hedvig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication()
@EnableFeignClients({"com.hedvig.integration", "com.hedvig.external"})
public class MemberRegistryApplication {

  public static void main(String[] args) {
    SpringApplication.run(MemberRegistryApplication.class, args);
  }
}
