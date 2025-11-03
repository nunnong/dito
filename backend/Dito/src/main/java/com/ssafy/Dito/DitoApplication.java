package com.ssafy.Dito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DitoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DitoApplication.class, args);
	}

}
