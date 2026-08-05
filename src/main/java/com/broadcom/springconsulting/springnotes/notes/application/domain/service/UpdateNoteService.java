package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.UpdateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class UpdateNoteService implements UpdateNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( UpdateNoteService.class );

    private final AppendNoteEventPort appendNoteEventPort;
    private final ApplicationEventPublisher eventPublisher;
    private final ObservationRegistry observationRegistry;

    UpdateNoteService( AppendNoteEventPort appendNoteEventPort, ApplicationEventPublisher eventPublisher, ObservationRegistry observationRegistry ) {
        this.appendNoteEventPort = appendNoteEventPort;
        this.eventPublisher = eventPublisher;
        this.observationRegistry = observationRegistry;
    }

    @Override
    @Transactional
    public Note execute( UpdateNoteCommand command ) {
        log.debug( "Updating note {}", command.id() );

        return Observation.createNotStarted( "notes.update", observationRegistry )
                .observe( () -> {
                    var event = new NoteUpdated( command.id(), command.title(), command.content(), Instant.now() );

                    appendNoteEventPort.append( event, command.owner() );
                    eventPublisher.publishEvent( new NoteEventPublished( event, command.owner() ) );

                    return new Note( command.id(), command.title(), command.content() );
                } );
    }

}
