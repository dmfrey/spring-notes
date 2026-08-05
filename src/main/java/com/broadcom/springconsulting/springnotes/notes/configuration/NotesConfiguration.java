package com.broadcom.springconsulting.springnotes.notes.configuration;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// Narrow test slices (@DataJdbcTest, @WebMvcTest) @Import this class specifically to backfill
// the beans they need beyond their slice - they don't autoconfigure AMQP, so the messaging
// adapter and outbox poller (which need a RabbitTemplate) are excluded here. This doesn't
// affect the real app: SpringNotesApplication's own component scan already covers this
// package tree independently, so those beans still get registered in production.
// (@DataJdbcTest also lacks Jackson autoconfiguration, needed by NoteEventStoreAdapter's
// ObjectMapper - see TestcontainersConfiguration's fallback bean for that half of the fix.)
@Configuration
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
