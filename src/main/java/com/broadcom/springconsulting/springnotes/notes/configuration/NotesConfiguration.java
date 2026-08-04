package com.broadcom.springconsulting.springnotes.notes.configuration;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan( basePackages = "com.broadcom.springconsulting.springnotes.notes" )
@EnableJdbcRepositories( basePackages = "com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence" )
@EnableScheduling
public class NotesConfiguration {

    public static final String NOTES_EVENTS_EXCHANGE = "notes.events";

    @Bean
    TopicExchange notesEventsExchange() {
        return new TopicExchange( NOTES_EVENTS_EXCHANGE );
    }

}
