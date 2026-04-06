package com.broadcom.springconsulting.spring_notes;

import org.springframework.boot.SpringApplication;

public class TestSpringNotesApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringNotesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
