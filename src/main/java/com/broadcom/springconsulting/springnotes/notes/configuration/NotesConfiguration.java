package com.broadcom.springconsulting.springnotes.notes.configuration;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// Narrow test slices (@DataJdbcTest, @WebMvcTest) @Import this class specifically to backfill
// the beans they need beyond their slice. @DataJdbcTest doesn't autoconfigure Jackson (needed
// by NoteEventStoreAdapter's ObjectMapper) or AMQP (needed by the messaging adapter and outbox
// poller, excluded below) - JacksonAutoConfiguration is imported explicitly so those slices get
// it too; it's a no-op in the real app, which already has it via spring-boot-starter-jackson.
@Configuration
@ImportAutoConfiguration( JacksonAutoConfiguration.class )
@ComponentScan(
        basePackages = "com.broadcom.springconsulting.springnotes.notes",
        excludeFilters = @Filter(
                type = FilterType.REGEX,
                pattern = {
                        "com\\.broadcom\\.springconsulting\\.springnotes\\.notes\\.adapter\\.out\\.messaging\\..+",
                        "com\\.broadcom\\.springconsulting\\.springnotes\\.notes\\.application\\.domain\\.service\\.NoteEventOutboxPoller"
                }
        )
)
@EnableJdbcRepositories( basePackages = "com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence" )
@EnableScheduling
public class NotesConfiguration {

    public static final String NOTES_EVENTS_EXCHANGE = "notes.events";

    @Bean
    TopicExchange notesEventsExchange() {
        return new TopicExchange( NOTES_EVENTS_EXCHANGE );
    }

}
