package com.broadcom.springconsulting.springnotes;

import org.springframework.boot.SpringApplication;

public class TestSpringNotesApplication {

	static void main(String[] args) {
		SpringApplication.from(SpringNotesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
