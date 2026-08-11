package com.broadcom.springconsulting.springnotes.chat.configuration;

import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan( basePackages = "com.broadcom.springconsulting.springnotes.chat" )
public class ChatConfiguration {

    public static final String NOTE_INDEX_QUEUE = "chat.note-index";

    @Bean
    Queue noteIndexQueue() {
        return new Queue( NOTE_INDEX_QUEUE, true );
    }

    // Binds to notes.events by name (NotesConfiguration.NOTES_EVENTS_EXCHANGE) rather than
    // wiring NotesConfiguration's exchange bean directly - same cross-feature-by-name
    // convention NoteEventPublisherAdapter already uses, keeps chat from depending on notes'
    // configuration class. The TopicExchange here isn't itself a @Bean, so RabbitAdmin doesn't
    // declare it a second time - it only needs the exchange's name to build the binding.
    @Bean
    Binding noteIndexBinding( Queue noteIndexQueue ) {
        return BindingBuilder.bind( noteIndexQueue )
                .to( new TopicExchange( NotesConfiguration.NOTES_EVENTS_EXCHANGE ) )
                .with( "note.*" );
    }

}
