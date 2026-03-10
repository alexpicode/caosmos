package com.caosmos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "com.caosmos")
public class CaosmosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaosmosApplication.class, args);
	}

}
