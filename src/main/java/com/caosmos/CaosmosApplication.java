package com.caosmos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@Modulith(sharedModules = "common")
@SpringBootApplication
public class CaosmosApplication {

  public static void main(String[] args) {
    SpringApplication.run(CaosmosApplication.class, args);
  }

}
