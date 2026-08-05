package com.broadcom.springconsulting.springnotes.notes.adapter.out.messaging;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.PublishNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
class NoteEventPublisherAdapter implements PublishNoteEventPort {

    private static final Logger log = LoggerFactory.getLogger( NoteEventPublisherAdapter.class );

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    NoteEventPublisherAdapter( RabbitTemplate rabbitTemplate, ObjectMapper objectMapper ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish( UUID eventId, NoteEvent event ) {
        log.debug( "Publishing {} for note {}", event.getClass().getSimpleName(), event.noteId() );

        var body = objectMapper.writeValueAsString( event ).getBytes( StandardCharsets.UTF_8 );
        var message = MessageBuilder.withBody( body )
                .setContentType( MessageProperties.CONTENT_TYPE_JSON )
                .setMessageId( eventId.toString() )
                .build();

        rabbitTemplate.send( NotesConfiguration.NOTES_EVENTS_EXCHANGE, routingKeyFor( event ), message );
    }

    private static String routingKeyFor( NoteEvent event ) {
        return switch ( event ) {
            case NoteCreated ignored -> "note.created";
            case NoteUpdated ignored -> "note.updated";
            case NoteDeleted ignored -> "note.deleted";
        };
    }

}
