package com.divyam.advent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdventbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdventbackendApplication.class, args);
	}

}
