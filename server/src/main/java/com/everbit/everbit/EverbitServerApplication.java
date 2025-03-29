package com.everbit.everbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EverbitServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EverbitServerApplication.class, args);
	}

}
