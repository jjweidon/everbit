package com.everbit.everbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EverbitApplication {

	public static void main(String[] args) {
		SpringApplication.run(EverbitApplication.class, args);
	}
}
