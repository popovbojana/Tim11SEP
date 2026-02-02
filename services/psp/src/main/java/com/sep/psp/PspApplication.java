package com.sep.psp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PspApplication {

	public static void main(String[] args) {
		SpringApplication.run(PspApplication.class, args);
	}

}
