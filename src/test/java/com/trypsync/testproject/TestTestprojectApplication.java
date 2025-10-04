package com.trypsync.testproject;

import org.springframework.boot.SpringApplication;

public class TestTestprojectApplication {

	public static void main(String[] args) {
		SpringApplication.from(TestprojectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
