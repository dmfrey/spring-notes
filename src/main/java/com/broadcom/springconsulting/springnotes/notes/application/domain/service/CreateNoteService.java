package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class CreateNoteService implements CreateNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( CreateNoteService.class );

    private final SaveNotePort saveNotePort;
    private final ObservationRegistry observationRegistry;

    CreateNoteService( SaveNotePort saveNotePort, ObservationRegistry observationRegistry ) {
        this.saveNotePort = saveNotePort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Note execute( CreateNoteCommand command ) {
        log.debug( "Creating note for owner {}", command.owner() );

        return Observation.createNotStarted( "notes.create", observationRegistry )
                .observe( () -> saveNotePort.saveNote( command.owner(), command.title(), command.content() ) );
    }

}