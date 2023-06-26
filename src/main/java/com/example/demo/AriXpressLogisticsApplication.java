package com.example.demo;

import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class AriXpressLogisticsApplication {

	@Autowired
	private final AppUtil appUtil;

	public static void main(String[] args) {
		SpringApplication.run(AriXpressLogisticsApplication.class, args);
		AppUtil appUtil1 = new AppUtil();
		System.out.println(appUtil1.generateSerialNumber("AXL-"));

		System.out.println("Generated Reference: " + appUtil1.generateReference());

		UUID uuid = UUID.randomUUID();
		System.out.println("UUID: " + uuid);

		System.out.println("Gotten Reference: " + appUtil1.getReference());

	}

}
