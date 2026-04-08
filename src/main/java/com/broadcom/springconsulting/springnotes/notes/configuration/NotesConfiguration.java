package com.broadcom.springconsulting.springnotes.notes.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@ComponentScan( basePackages = "com.broadcom.springconsulting.springnotes.notes" )
@EnableJdbcRepositories( basePackages = "com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence" )
public class NotesConfiguration {

}