package com.everbit.everbit.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;

/**
 * Clock 제공. 테스트에서 Clock.fixed() 주입 가능.
 * SoT: docs/prompt-pack/server/00_source_map_and_decisions.md §D.
 */
@Configuration
public class ClockConfig {

	@Bean
	@Primary
	public Clock clock() {
		return Clock.systemUTC();
	}
}
