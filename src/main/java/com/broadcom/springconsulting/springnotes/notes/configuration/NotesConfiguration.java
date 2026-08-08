package com.broadcom.springconsulting.springnotes.notes.configuration;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportRuntimeHints;
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
@ImportRuntimeHints( NotesConfiguration.NoteEventRuntimeHints.class )
public class NotesConfiguration {

    public static final String NOTES_EVENTS_EXCHANGE = "notes.events";

    @Bean
    TopicExchange notesEventsExchange() {
        return new TopicExchange( NOTES_EVENTS_EXCHANGE );
    }

    /**
     * NoteEvent records are serialized/deserialized reflectively by Jackson - both for event
     * store persistence (NoteEventStoreAdapter) and for RabbitMQ publishing
     * (NoteEventPublisherAdapter) - via a plain ObjectMapper call inside a @Component, not an
     * MVC controller signature. Spring AOT's binding-hint inference only traces JSON types
     * reachable from @RequestMapping/@ResponseBody signatures, so it never discovers these.
     * GraalVM's default reachability metadata doesn't cover record component accessors for
     * arbitrary application records, so an unregistered one crashes the native image with
     * "Record components not available ... must be included in the reflection configuration"
     * the first time it's actually serialized (e.g. the first note created after a fresh
     * deploy) - a boot-only smoke test won't catch this, an end-to-end create-a-note test would.
     */
    static class NoteEventRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints( RuntimeHints hints, ClassLoader classLoader ) {
            var binding = new BindingReflectionHintsRegistrar();
            binding.registerReflectionHints( hints.reflection(), NoteCreated.class, NoteUpdated.class, NoteDeleted.class );
        }

    }

}
