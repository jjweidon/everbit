package com.everbit.everbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EverbitServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EverbitServerApplication.class, args);
	}

}
